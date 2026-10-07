package br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosCancelamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.Instrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@Order(2)
public class ValidadorAntecedenciaCancelamento implements ValidadorCancelamento {
    private static final long ANTECEDENCIA_MINIMA_HORAS = 24;

    @Override
    public void validar(Instrucao instrucao, DadosCancelamentoInstrucao dados) {
        LocalDateTime agora = LocalDateTime.now();
        long antecedenciaMinutos = Duration.between(agora, instrucao.getDataHora()).toMinutes();

        if (antecedenciaMinutos < ANTECEDENCIA_MINIMA_HORAS * 60) {
            throw new ValidacaoException("A instrução somente pode ser cancelada com antecedência mínima de 24 horas!");
        }
    }
}
