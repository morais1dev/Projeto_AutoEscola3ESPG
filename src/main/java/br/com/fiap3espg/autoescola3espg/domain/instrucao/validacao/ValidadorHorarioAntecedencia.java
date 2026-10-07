package br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class ValidadorHorarioAntecedencia implements ValidadorAgendamento {
    @Override
    public void validar(DadosAgendamentoInstrucao dados) {
        LocalDateTime dataEscolhida = dados.dataHora();
        LocalDateTime agora = LocalDateTime.now();

        long antecedencia = Duration.between(agora, dataEscolhida).toMinutes();

        if (antecedencia < 30) {
            throw new ValidacaoException("Instrução deve ser agendada com antecedência mínima de 30 minutos!");
        }
    }
}