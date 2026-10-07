package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.aluno.*;
import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosEndereco;
import br.com.fiap3espg.autoescola3espg.domain.endereco.Endereco;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {
    @Mock
    AlunoRepository repository;

    @InjectMocks
    AlunoService service;

    @Test
    @DisplayName("Expectativa: cadastrar aluno válido como ativo")
    void cadastrarAlunoCenario1() {
        when(repository.save(any(Aluno.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        DadosDetalhamentoAluno resultado = service.cadastrarAluno(dadosCadastro());

        assertThat(resultado.nome()).isEqualTo("Aluno Teste");
        assertThat(resultado.cpf()).isEqualTo("12345678901");
        assertThat(resultado.ativo()).isTrue();
    }

    @Test
    @DisplayName("Expectativa: não permitir cadastrar aluno com CPF já existente")
    void cadastrarAlunoCenario2() {
        when(repository.existsByCpf("12345678901")).thenReturn(true);

        assertThrows(ValidacaoException.class, () -> service.cadastrarAluno(dadosCadastro()));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: não permitir alterar o e-mail do aluno")
    void atualizarAlunoCenario1() {
        var dados = new DadosAtualizacaoAluno(1L, null, null, null, "novo@email.com", null);

        assertThrows(ValidacaoException.class, () -> service.atualizarAluno(dados));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: não permitir alterar o CPF do aluno")
    void atualizarAlunoCenario2() {
        var dados = new DadosAtualizacaoAluno(1L, null, null, null, null, "99999999999");

        assertThrows(ValidacaoException.class, () -> service.atualizarAluno(dados));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: atualizar nome, telefone e endereço do aluno")
    void atualizarAlunoCenario3() {
        Aluno aluno = alunoExistente();
        when(repository.findById(1L)).thenReturn(Optional.of(aluno));
        when(repository.save(aluno)).thenReturn(aluno);

        var novoEndereco = new DadosEndereco("Avenida Nova", "500", null, "Bela Vista", "São Paulo", "SP", "01310-100");
        var dados = new DadosAtualizacaoAluno(1L, "Nome Atualizado", "(11) 90000-0000", novoEndereco, null, null);
        DadosDetalhamentoAluno resultado = service.atualizarAluno(dados);

        assertThat(resultado.nome()).isEqualTo("Nome Atualizado");
        assertThat(resultado.telefone()).isEqualTo("(11) 90000-0000");
        assertThat(resultado.endereco().logradouro()).isEqualTo("Avenida Nova");
        assertThat(resultado.email()).isEqualTo("aluno@email.com");
        assertThat(resultado.cpf()).isEqualTo("12345678901");
    }

    @Test
    @DisplayName("Expectativa: a exclusão apenas inativa o aluno")
    void excluirAlunoCenario1() {
        Aluno aluno = alunoExistente();
        when(repository.findById(1L)).thenReturn(Optional.of(aluno));

        service.excluirAluno(1L);

        assertThat(aluno.isAtivo()).isFalse();
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Expectativa: lançar exceção ao detalhar aluno inexistente")
    void detalharAlunoCenario1() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AlunoNotFoundException.class, () -> service.detalharAluno(99L));
    }

    private DadosCadastroAluno dadosCadastro() {
        return new DadosCadastroAluno(
                "Aluno Teste",
                "aluno@email.com",
                "(11) 98765-4321",
                "12345678901",
                new DadosEndereco("Rua Teste", "1", null, "Centro", "São Paulo", "SP", "01001-000")
        );
    }

    private Aluno alunoExistente() {
        return new Aluno(
                1L,
                "Aluno Teste",
                "aluno@email.com",
                "(11) 98765-4321",
                "12345678901",
                new Endereco("Rua Teste", "1", null, "Centro", "São Paulo", "SP", "01001-000"),
                true
        );
    }
}
