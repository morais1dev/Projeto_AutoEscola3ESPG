package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.aluno.Aluno;
import br.com.fiap3espg.autoescola3espg.domain.aluno.AlunoNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.aluno.AlunoRepository;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.*;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao.ValidadorAgendamento;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao.ValidadorCancelamento;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.Instrutor;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.InstrutorNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.InstrutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstrucaoService {
    private final InstrucaoRepository repository;
    private final AlunoRepository alunoRepository;
    private final InstrutorRepository instrutorRepository;
    private final List<ValidadorAgendamento> validadoresAgendamento;
    private final List<ValidadorCancelamento> validadoresCancelamento;

    @Transactional
    public DadosDetalhamentoAgendamento agendarInstrucao(DadosAgendamentoInstrucao dados) {
        if (!alunoRepository.existsById(dados.idAluno())) {
            throw new AlunoNotFoundException("ID do aluno informado não existe!");
        }
        if (dados.idInstrutor() != null && !instrutorRepository.existsById(dados.idInstrutor())) {
            throw new InstrutorNotFoundException("ID do instrutor informado não existe!");
        }

        //Validações das regras de negócio de agendamento
        validadoresAgendamento.forEach(validador -> validador.validar(dados));

        Instrutor instrutor = escolherInstrutor(dados);
        if (instrutor == null) {
            throw new ValidacaoException("Nenhum instrutor disponível para a data/hora informada!");
        }
        Aluno aluno = alunoRepository.getReferenceById(dados.idAluno());

        Instrucao instrucao = new Instrucao(aluno, instrutor, dados.dataHora());
        Instrucao salvo = repository.save(instrucao);
        return new DadosDetalhamentoAgendamento(salvo);
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoAgendamento> listarInstrucoes(Pageable paginacao) {
        return repository
                .findAll(paginacao)
                .map(DadosDetalhamentoAgendamento::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoAgendamento detalharInstrucao(Long id) {
        return new DadosDetalhamentoAgendamento(buscarInstrucao(id));
    }

    @Transactional
    public DadosDetalhamentoAgendamento cancelarInstrucao(DadosCancelamentoInstrucao dados) {
        Instrucao instrucao = buscarInstrucao(dados.idInstrucao());

        //Validações das regras de negócio de cancelamento
        validadoresCancelamento.forEach(validador -> validador.validar(instrucao, dados));

        instrucao.cancelar(dados.motivo());
        Instrucao salvo = repository.save(instrucao);
        return new DadosDetalhamentoAgendamento(salvo);
    }

    private Instrucao buscarInstrucao(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new InstrucaoNotFoundException("ID da instrução informada não existe!"));
    }

    private Instrutor escolherInstrutor(DadosAgendamentoInstrucao dados) {
        if (dados.idInstrutor() != null) {
            return instrutorRepository.getReferenceById(dados.idInstrutor());
        }
        if (dados.especialidade() == null) {
            throw new ValidacaoException("Especialidade é obrigatória se o instrutor não for informado!");
        }
        //Instrutor não informado: escolha aleatória de um instrutor ativo e livre na data/hora
        return instrutorRepository.escolherInstrutorAleatorioDisponivel(
                dados.especialidade(),
                dados.dataHora()
        );
    }
}
