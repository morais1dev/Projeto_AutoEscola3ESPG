package br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.InstrucaoRepository;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidadorConflitoHorarioInstrutor implements ValidadorAgendamento {
    private final InstrucaoRepository repository;

    @Override
    public void validar(DadosAgendamentoInstrucao dados) {
        //Instrutor não informado: o sistema escolhe um instrutor livre automaticamente
        if (dados.idInstrutor() == null) {
            return;
        }

        boolean instrutorOcupado = repository.existsByInstrutorIdAndDataHoraAndMotivoCancelamentoIsNull(
                dados.idInstrutor(),
                dados.dataHora()
        );

        if (instrutorOcupado) {
            throw new ValidacaoException("Instrutor ocupado na data / hora escolhida!");
        }
    }
}
