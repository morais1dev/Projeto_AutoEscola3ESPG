package br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidadoresInstrucaoTest {
    @Mock
    InstrucaoRepository repository;

    @Test
    @DisplayName("Expectativa: rejeitar agendamento no domingo")
    void horarioFuncionamentoCenario1() {
        var validador = new ValidadorHorarioFuncionamento();
        assertThrows(ValidacaoException.class, () -> validador.validar(agendamento(proximo(DayOfWeek.SUNDAY, 10))));
    }

    @Test
    @DisplayName("Expectativa: rejeitar agendamento antes das 06:00 e com início às 21:00 ou depois")
    void horarioFuncionamentoCenario2() {
        var validador = new ValidadorHorarioFuncionamento();
        assertThrows(ValidacaoException.class, () -> validador.validar(agendamento(proximo(DayOfWeek.MONDAY, 5))));
        assertThrows(ValidacaoException.class, () -> validador.validar(agendamento(proximo(DayOfWeek.MONDAY, 21))));
    }

    @Test
    @DisplayName("Expectativa: aceitar agendamento de segunda a sábado entre 06:00 e 20:00 (término às 21:00)")
    void horarioFuncionamentoCenario3() {
        var validador = new ValidadorHorarioFuncionamento();
        assertDoesNotThrow(() -> validador.validar(agendamento(proximo(DayOfWeek.MONDAY, 6))));
        assertDoesNotThrow(() -> validador.validar(agendamento(proximo(DayOfWeek.SATURDAY, 20))));
    }

    @Test
    @DisplayName("Expectativa: rejeitar agendamento com menos de 30 minutos de antecedência")
    void antecedenciaAgendamentoCenario1() {
        var validador = new ValidadorHorarioAntecedencia();
        assertThrows(ValidacaoException.class,
                () -> validador.validar(agendamento(LocalDateTime.now().plusMinutes(10))));
        assertDoesNotThrow(() -> validador.validar(agendamento(LocalDateTime.now().plusDays(2))));
    }

    @Test
    @DisplayName("Expectativa: rejeitar a terceira instrução do aluno no mesmo dia")
    void limiteDiarioAlunoCenario1() {
        var validador = new ValidadorLimiteDiarioAluno(repository);
        when(repository.countByAlunoIdAndDataHoraBetweenAndMotivoCancelamentoIsNull(eq(1L), any(), any()))
                .thenReturn(2L);

        assertThrows(ValidacaoException.class, () -> validador.validar(agendamento(proximo(DayOfWeek.MONDAY, 10))));
    }

    @Test
    @DisplayName("Expectativa: permitir a segunda instrução do aluno no mesmo dia")
    void limiteDiarioAlunoCenario2() {
        var validador = new ValidadorLimiteDiarioAluno(repository);
        when(repository.countByAlunoIdAndDataHoraBetweenAndMotivoCancelamentoIsNull(eq(1L), any(), any()))
                .thenReturn(1L);

        assertDoesNotThrow(() -> validador.validar(agendamento(proximo(DayOfWeek.MONDAY, 10))));
    }

    @Test
    @DisplayName("Expectativa: rejeitar instrutor já ocupado no mesmo horário")
    void conflitoHorarioInstrutorCenario1() {
        var validador = new ValidadorConflitoHorarioInstrutor(repository);
        LocalDateTime data = proximo(DayOfWeek.MONDAY, 10);
        when(repository.existsByInstrutorIdAndDataHoraAndMotivoCancelamentoIsNull(1L, data)).thenReturn(true);

        assertThrows(ValidacaoException.class,
                () -> validador.validar(new DadosAgendamentoInstrucao(1L, 1L, null, data)));
    }

    @Test
    @DisplayName("Expectativa: rejeitar cancelamento com menos de 24 horas de antecedência")
    void antecedenciaCancelamentoCenario1() {
        var validador = new ValidadorAntecedenciaCancelamento();
        var dados = new DadosCancelamentoInstrucao(1L, MotivoCancelamento.OUTROS);

        Instrucao amanhaCedo = new Instrucao(null, null, LocalDateTime.now().plusHours(23));
        Instrucao daquiTresDias = new Instrucao(null, null, LocalDateTime.now().plusDays(3));

        assertThrows(ValidacaoException.class, () -> validador.validar(amanhaCedo, dados));
        assertDoesNotThrow(() -> validador.validar(daquiTresDias, dados));
    }

    private DadosAgendamentoInstrucao agendamento(LocalDateTime dataHora) {
        return new DadosAgendamentoInstrucao(1L, null, null, dataHora);
    }

    private LocalDateTime proximo(DayOfWeek dia, int hora) {
        return LocalDateTime
                .now()
                .with(TemporalAdjusters.next(dia))
                .withHour(hora)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }
}
