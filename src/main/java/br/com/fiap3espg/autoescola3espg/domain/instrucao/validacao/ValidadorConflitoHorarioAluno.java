package br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.InstrucaoRepository;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidadorConflitoHorarioAluno implements ValidadorAgendamento {
    private final InstrucaoRepository repository;

    @Override
    public void validar(DadosAgendamentoInstrucao dados) {
        boolean alunoOcupado = repository.existsByAlunoIdAndDataHoraAndMotivoCancelamentoIsNull(
                dados.idAluno(),
                dados.dataHora()
        );

        if (alunoOcupado) {
            throw new ValidacaoException("O aluno já possui uma instrução agendada nessa data / hora!");
        }
    }
}
