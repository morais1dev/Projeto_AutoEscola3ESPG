package br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao;

import br.com.fiap3espg.autoescola3espg.domain.aluno.AlunoRepository;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidadorAlunoAtivo implements ValidadorAgendamento {
    private final AlunoRepository alunoRepository;

    @Override
    public void validar(DadosAgendamentoInstrucao dados) {
        if (alunoRepository.existsByIdAndAtivoFalse(dados.idAluno())) {
            throw new ValidacaoException("Não é possível agendar instrução para aluno inativo!");
        }
    }
}