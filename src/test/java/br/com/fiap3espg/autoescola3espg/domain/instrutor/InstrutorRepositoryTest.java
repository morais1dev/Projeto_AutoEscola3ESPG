package br.com.fiap3espg.autoescola3espg.domain.instrutor;

import br.com.fiap3espg.autoescola3espg.domain.aluno.Aluno;
import br.com.fiap3espg.autoescola3espg.domain.endereco.Endereco;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.Instrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.MotivoCancelamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class InstrutorRepositoryTest {
    @Autowired
    InstrutorRepository repository;

    @Autowired
    TestEntityManager testEntity;

    @Test
    @DisplayName("Expectativa: retornar null quando instrutor cadastrado não está disponível")
    void escolherInstrutorAleatorioDisponivelCenario1() {
        LocalDateTime proximaSegundaAs10 = proximaSegundaAs10();

        //Cadastrar aluno
        Aluno aluno = cadastrarAluno();

        //Cadastrar instrutor
        Instrutor instrutor = cadastrarInstrutor();

        //Agendar instrução
        agendarInstrucao(aluno, instrutor, proximaSegundaAs10, null);

        //when or act
        Instrutor instrutorDisponivel = repository.escolherInstrutorAleatorioDisponivel(
                Especialidade.MOTOS,
                proximaSegundaAs10
        );

        //then or assert
        assertThat(instrutorDisponivel).isNull();
    }

    @Test
    @DisplayName("Expectativa: retornar instrutor quando instrutor cadastrado está disponível")
    void escolherInstrutorAleatorioDisponivelCenario2() {
        LocalDateTime proximaSegundaAs10 = proximaSegundaAs10();

        //Cadastrar instrutor
        Instrutor instrutor = cadastrarInstrutor();

        //when or act
        Instrutor instrutorDisponivel = repository.escolherInstrutorAleatorioDisponivel(
                Especialidade.MOTOS,
                proximaSegundaAs10
        );

        //then or assert
        assertThat(instrutorDisponivel).isEqualTo(instrutor);
    }

    @Test
    @DisplayName("Expectativa: retornar instrutor quando a única instrução no horário foi cancelada")
    void escolherInstrutorAleatorioDisponivelCenario3() {
        LocalDateTime proximaSegundaAs10 = proximaSegundaAs10();
        Aluno aluno = cadastrarAluno();
        Instrutor instrutor = cadastrarInstrutor();
        agendarInstrucao(aluno, instrutor, proximaSegundaAs10, MotivoCancelamento.ALUNO_DESISTIU);

        Instrutor instrutorDisponivel = repository.escolherInstrutorAleatorioDisponivel(
                Especialidade.MOTOS,
                proximaSegundaAs10
        );

        assertThat(instrutorDisponivel).isEqualTo(instrutor);
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

    private Endereco dadosEndereco() {
        return new Endereco(
                "Rua Teste",
                "000",
                "Casa dos Fundos",
                "Vila Teste",
                "Test City",
                "TS",
                "01234-000"
        );
    }

    private Aluno cadastrarAluno() {
        Aluno aluno = new Aluno(
                null,
                "Aluno Teste",
                "alunoteste@email.com.br",
                "(11) 98765-4321",
                "12345678901",
                dadosEndereco(),
                true
        );
        testEntity.persist(aluno);
        return aluno;
    }

    private Instrutor cadastrarInstrutor() {
        Instrutor instrutor = new Instrutor(
                null,
                "Instrutor Teste",
                "instrutorteste@email.com",
                "(11) 91234-5678",
                "01234567890",
                Especialidade.MOTOS,
                dadosEndereco(),
                true
        );
        testEntity.persist(instrutor);
        return instrutor;
    }

    private void agendarInstrucao(
            Aluno aluno,
            Instrutor instrutor,
            LocalDateTime dataHora,
            MotivoCancelamento motivoCancelamento) {
        Instrucao instrucao = new Instrucao(
                aluno,
                instrutor,
                dataHora
        );
        if (motivoCancelamento != null) {
            instrucao.cancelar(motivoCancelamento);
        }
        testEntity.persist(instrucao);
    }
}
