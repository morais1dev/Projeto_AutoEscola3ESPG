package br.com.fiap3espg.autoescola3espg.domain.aluno;

import br.com.fiap3espg.autoescola3espg.domain.endereco.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AlunoRepositoryTest {
    @Autowired
    AlunoRepository repository;

    @Autowired
    TestEntityManager testEntity;

    @Test
    @DisplayName("Expectativa: a listagem traz apenas alunos ativos")
    void findAllByAtivoTrueCenario1() {
        Aluno ativo = cadastrarAluno("Aluno Ativo", "aluno.ativo@email.com", "11111111111", true);
        Aluno inativo = cadastrarAluno("Aluno Inativo", "aluno.inativo@email.com", "22222222222", false);

        Page<Aluno> pagina = repository.findAllByAtivoTrue(PageRequest.of(0, 100));

        assertThat(pagina.getContent()).contains(ativo);
        assertThat(pagina.getContent()).doesNotContain(inativo);
    }

    @Test
    @DisplayName("Expectativa: identificar corretamente aluno inativo")
    void existsByIdAndAtivoFalseCenario1() {
        Aluno ativo = cadastrarAluno("Aluno Ativo", "aluno.ativo@email.com", "11111111111", true);
        Aluno inativo = cadastrarAluno("Aluno Inativo", "aluno.inativo@email.com", "22222222222", false);

        assertThat(repository.existsByIdAndAtivoFalse(inativo.getId())).isTrue();
        assertThat(repository.existsByIdAndAtivoFalse(ativo.getId())).isFalse();
    }

    @Test
    @DisplayName("Expectativa: identificar CPF e e-mail já cadastrados")
    void existsByCpfEEmailCenario1() {
        cadastrarAluno("Aluno Teste", "aluno.cpf@email.com", "33333333333", true);

        assertThat(repository.existsByCpf("33333333333")).isTrue();
        assertThat(repository.existsByEmail("aluno.cpf@email.com")).isTrue();
        assertThat(repository.existsByCpf("99999999999")).isFalse();
    }

    private Aluno cadastrarAluno(String nome, String email, String cpf, boolean ativo) {
        Aluno aluno = new Aluno(
                null,
                nome,
                email,
                "(11) 98765-4321",
                cpf,
                new Endereco("Rua Teste", "10", null, "Centro", "São Paulo", "SP", "01001-000"),
                ativo
        );
        testEntity.persist(aluno);
        return aluno;
    }
}
