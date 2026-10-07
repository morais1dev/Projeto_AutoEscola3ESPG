package br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.InstrutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidadorInstrutorAtivo implements ValidadorAgendamento {
    private final InstrutorRepository instrutorRepository;

    @Override
    public void validar(DadosAgendamentoInstrucao dados) {
        //Instrutor não informado: a escolha automática já considera apenas instrutores ativos
        if (dados.idInstrutor() == null) {
            return;
        }

        if (instrutorRepository.existsByIdAndAtivoFalse(dados.idInstrutor())) {
            throw new ValidacaoException("Não é possível agendar instrução com instrutor inativo!");
        }
    }
}
