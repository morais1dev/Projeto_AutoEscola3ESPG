package br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosCancelamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.Instrucao;

public interface ValidadorCancelamento {
    void validar(Instrucao instrucao, DadosCancelamentoInstrucao dados);
}
