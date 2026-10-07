package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosEndereco;
import br.com.fiap3espg.autoescola3espg.domain.endereco.Endereco;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.*;
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
class InstrutorServiceTest {
    @Mock
    InstrutorRepository repository;

    @InjectMocks
    InstrutorService service;

    @Test
    @DisplayName("Expectativa: cadastrar instrutor válido como ativo")
    void cadastrarInstrutorCenario1() {
        when(repository.save(any(Instrutor.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        DadosDetalhamentoInstrutor resultado = service.cadastrarInstrutor(dadosCadastro());

        assertThat(resultado.nome()).isEqualTo("Instrutor Teste");
        assertThat(resultado.ativo()).isTrue();
    }

    @Test
    @DisplayName("Expectativa: não permitir cadastrar instrutor com e-mail já existente")
    void cadastrarInstrutorCenario2() {
        when(repository.existsByEmail("instrutor@email.com")).thenReturn(true);

        assertThrows(ValidacaoException.class, () -> service.cadastrarInstrutor(dadosCadastro()));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: não permitir alterar o e-mail do instrutor")
    void atualizarInstrutorCenario1() {
        var dados = new DadosAtualizacaoInstrutor(1L, null, null, null, "novo@email.com", null, null);

        assertThrows(ValidacaoException.class, () -> service.atualizarInstrutor(dados));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: não permitir alterar a CNH do instrutor")
    void atualizarInstrutorCenario2() {
        var dados = new DadosAtualizacaoInstrutor(1L, null, null, null, null, "99999999999", null);

        assertThrows(ValidacaoException.class, () -> service.atualizarInstrutor(dados));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: não permitir alterar a especialidade do instrutor")
    void atualizarInstrutorCenario3() {
        var dados = new DadosAtualizacaoInstrutor(1L, null, null, null, null, null, Especialidade.CAMINHOES);

        assertThrows(ValidacaoException.class, () -> service.atualizarInstrutor(dados));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: atualizar nome e telefone, mantendo e-mail, CNH e especialidade")
    void atualizarInstrutorCenario4() {
        Instrutor instrutor = instrutorExistente();
        when(repository.findById(1L)).thenReturn(Optional.of(instrutor));
        when(repository.save(instrutor)).thenReturn(instrutor);

        var dados = new DadosAtualizacaoInstrutor(1L, "Nome Atualizado", "(11) 90000-0000", null, null, null, null);
        DadosDetalhamentoInstrutor resultado = service.atualizarInstrutor(dados);

        assertThat(resultado.nome()).isEqualTo("Nome Atualizado");
        assertThat(resultado.telefone()).isEqualTo("(11) 90000-0000");
        assertThat(resultado.email()).isEqualTo("instrutor@email.com");
        assertThat(resultado.cnh()).isEqualTo("01234567890");
        assertThat(resultado.especialidade()).isEqualTo(Especialidade.MOTOS);
    }

    @Test
    @DisplayName("Expectativa: a exclusão apenas inativa o instrutor")
    void excluirInstrutorCenario1() {
        Instrutor instrutor = instrutorExistente();
        when(repository.findById(1L)).thenReturn(Optional.of(instrutor));

        service.excluirInstrutor(1L);

        assertThat(instrutor.isAtivo()).isFalse();
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Expectativa: lançar exceção ao excluir instrutor inexistente")
    void excluirInstrutorCenario2() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(InstrutorNotFoundException.class, () -> service.excluirInstrutor(99L));
    }

    private DadosCadastroInstrutor dadosCadastro() {
        return new DadosCadastroInstrutor(
                "Instrutor Teste",
                "instrutor@email.com",
                "(11) 91234-5678",
                "01234567890",
                Especialidade.MOTOS,
                new DadosEndereco("Rua Teste", "1", null, "Centro", "São Paulo", "SP", "01001-000")
        );
    }

    private Instrutor instrutorExistente() {
        return new Instrutor(
                1L,
                "Instrutor Teste",
                "instrutor@email.com",
                "(11) 91234-5678",
                "01234567890",
                Especialidade.MOTOS,
                new Endereco("Rua Teste", "1", null, "Centro", "São Paulo", "SP", "01001-000"),
                true
        );
    }
}
