package br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ValidadorHoraInteira implements ValidadorAgendamento {
    @Override
    public void validar(DadosAgendamentoInstrucao dados) {
        LocalDateTime dataEscolhida = dados.dataHora();

        if (dataEscolhida.getMinute() != 0 /*&& dataEscolhida.getSecond() != 0 &&
                        dataEscolhida.getNano() != 0*/) {
            throw new ValidacaoException("O horário deve ser preenchido em horas inteiras (ex: 09:00, 13:00, ...)!");
        }
    }
}