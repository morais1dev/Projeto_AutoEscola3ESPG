package br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosCancelamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.Instrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ValidadorInstrucaoJaCancelada implements ValidadorCancelamento {
    @Override
    public void validar(Instrucao instrucao, DadosCancelamentoInstrucao dados) {
        if (instrucao.isCancelada()) {
            throw new ValidacaoException("Esta instrução já foi cancelada!");
        }
    }
}
