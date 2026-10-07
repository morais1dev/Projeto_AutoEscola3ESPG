package br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosAgendamentoInstrucao;

public interface ValidadorAgendamento {
    void validar(DadosAgendamentoInstrucao dados);
}