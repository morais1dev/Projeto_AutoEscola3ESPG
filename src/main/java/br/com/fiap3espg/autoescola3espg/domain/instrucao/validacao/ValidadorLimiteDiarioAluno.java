package br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.InstrucaoRepository;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ValidadorLimiteDiarioAluno implements ValidadorAgendamento {
    public static final int LIMITE_DIARIO_INSTRUCOES = 2;

    private final InstrucaoRepository repository;

    @Override
    public void validar(DadosAgendamentoInstrucao dados) {
        LocalDate dia = dados.dataHora().toLocalDate();
        LocalDateTime inicioDia = dia.atStartOfDay();
        LocalDateTime fimDia = dia.atTime(23, 59, 59);

        long instrucoesNoDia = repository.countByAlunoIdAndDataHoraBetweenAndMotivoCancelamentoIsNull(
                dados.idAluno(),
                inicioDia,
                fimDia
        );

        //Regra: não permitir MAIS de duas instruções no mesmo dia para um mesmo aluno
        if (instrucoesNoDia >= LIMITE_DIARIO_INSTRUCOES) {
            throw new ValidacaoException("Permitido o agendamento de no máximo duas instruções por dia para o mesmo aluno!");
        }
    }
}
