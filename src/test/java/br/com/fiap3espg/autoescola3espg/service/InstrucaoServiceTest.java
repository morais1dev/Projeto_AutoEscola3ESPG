package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.aluno.Aluno;
import br.com.fiap3espg.autoescola3espg.domain.aluno.AlunoNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.aluno.AlunoRepository;
import br.com.fiap3espg.autoescola3espg.domain.endereco.Endereco;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.*;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao.ValidadorAgendamento;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao.ValidadorAntecedenciaCancelamento;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.validacao.ValidadorInstrucaoJaCancelada;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.Especialidade;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.Instrutor;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.InstrutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstrucaoServiceTest {
    @Mock
    InstrucaoRepository repository;

    @Mock
    AlunoRepository alunoRepository;

    @Mock
    InstrutorRepository instrutorRepository;

    @Mock
    ValidadorAgendamento validadorAgendamento;

    InstrucaoService service;

    @BeforeEach
    void setUp() {
        service = new InstrucaoService(
                repository,
                alunoRepository,
                instrutorRepository,
                List.of(validadorAgendamento),
                List.of(new ValidadorInstrucaoJaCancelada(), new ValidadorAntecedenciaCancelamento())
        );
    }

    @Test
    @DisplayName("Expectativa: agendar instrução quando aluno e instrutor existem e as validações passam")
    void agendarInstrucaoCenario1() {
        LocalDateTime data = proximaSegundaAs10();
        var dados = new DadosAgendamentoInstrucao(1L, 1L, null, data);
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(instrutorRepository.existsById(1L)).thenReturn(true);
        when(instrutorRepository.getReferenceById(1L)).thenReturn(instrutor());
        when(alunoRepository.getReferenceById(1L)).thenReturn(aluno());
        when(repository.save(any(Instrucao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        DadosDetalhamentoAgendamento resultado = service.agendarInstrucao(dados);

        verify(validadorAgendamento).validar(dados);
        assertThat(resultado.nomeAluno()).isEqualTo("Aluno Teste");
        assertThat(resultado.nomeInstrutor()).isEqualTo("Instrutor Teste");
        assertThat(resultado.dataHora()).isEqualTo(data);
        assertThat(resultado.cancelada()).isFalse();
    }

    @Test
    @DisplayName("Expectativa: não agendar instrução para aluno inexistente")
    void agendarInstrucaoCenario2() {
        var dados = new DadosAgendamentoInstrucao(99L, null, Especialidade.CARROS, proximaSegundaAs10());

        assertThrows(AlunoNotFoundException.class, () -> service.agendarInstrucao(dados));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: não agendar quando nenhum instrutor da especialidade está disponível")
    void agendarInstrucaoCenario3() {
        LocalDateTime data = proximaSegundaAs10();
        var dados = new DadosAgendamentoInstrucao(1L, null, Especialidade.CARROS, data);
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(instrutorRepository.escolherInstrutorAleatorioDisponivel(Especialidade.CARROS, data)).thenReturn(null);

        assertThrows(ValidacaoException.class, () -> service.agendarInstrucao(dados));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: exigir especialidade quando o instrutor não é informado")
    void agendarInstrucaoCenario4() {
        var dados = new DadosAgendamentoInstrucao(1L, null, null, proximaSegundaAs10());
        when(alunoRepository.existsById(1L)).thenReturn(true);

        assertThrows(ValidacaoException.class, () -> service.agendarInstrucao(dados));
    }

    @Test
    @DisplayName("Expectativa: cancelar instrução com antecedência maior que 24 horas")
    void cancelarInstrucaoCenario1() {
        Instrucao instrucao = new Instrucao(aluno(), instrutor(), LocalDateTime.now().plusDays(3));
        when(repository.findById(1L)).thenReturn(Optional.of(instrucao));
        when(repository.save(instrucao)).thenReturn(instrucao);

        DadosDetalhamentoAgendamento resultado = service.cancelarInstrucao(
                new DadosCancelamentoInstrucao(1L, MotivoCancelamento.ALUNO_DESISTIU));

        assertThat(instrucao.isCancelada()).isTrue();
        assertThat(resultado.motivoCancelamento()).isEqualTo(MotivoCancelamento.ALUNO_DESISTIU);
    }

    @Test
    @DisplayName("Expectativa: não cancelar instrução com menos de 24 horas de antecedência")
    void cancelarInstrucaoCenario2() {
        Instrucao instrucao = new Instrucao(aluno(), instrutor(), LocalDateTime.now().plusHours(5));
        when(repository.findById(1L)).thenReturn(Optional.of(instrucao));

        assertThrows(ValidacaoException.class, () -> service.cancelarInstrucao(
                new DadosCancelamentoInstrucao(1L, MotivoCancelamento.INSTRUTOR_CANCELOU)));
        assertThat(instrucao.isCancelada()).isFalse();
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: não cancelar novamente uma instrução já cancelada")
    void cancelarInstrucaoCenario3() {
        Instrucao instrucao = new Instrucao(aluno(), instrutor(), LocalDateTime.now().plusDays(3));
        instrucao.cancelar(MotivoCancelamento.OUTROS);
        when(repository.findById(1L)).thenReturn(Optional.of(instrucao));

        assertThrows(ValidacaoException.class, () -> service.cancelarInstrucao(
                new DadosCancelamentoInstrucao(1L, MotivoCancelamento.ALUNO_DESISTIU)));
        assertThat(instrucao.getMotivoCancelamento()).isEqualTo(MotivoCancelamento.OUTROS);
    }

    @Test
    @DisplayName("Expectativa: lançar exceção ao cancelar instrução inexistente")
    void cancelarInstrucaoCenario4() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(InstrucaoNotFoundException.class, () -> service.cancelarInstrucao(
                new DadosCancelamentoInstrucao(99L, MotivoCancelamento.OUTROS)));
    }

    private LocalDateTime proximaSegundaAs10() {
        return LocalDateTime
                .now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    private Endereco endereco() {
        return new Endereco("Rua Teste", "1", null, "Centro", "São Paulo", "SP", "01001-000");
    }

    private Aluno aluno() {
        return new Aluno(1L, "Aluno Teste", "aluno@email.com", "(11) 98765-4321", "12345678901", endereco(), true);
    }

    private Instrutor instrutor() {
        return new Instrutor(1L, "Instrutor Teste", "instrutor@email.com", "(11) 91234-5678",
                "01234567890", Especialidade.CARROS, endereco(), true);
    }
}
