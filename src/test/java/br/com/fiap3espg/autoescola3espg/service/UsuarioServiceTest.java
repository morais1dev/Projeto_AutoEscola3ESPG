package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import br.com.fiap3espg.autoescola3espg.domain.usuario.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock
    UsuarioRepository repository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository, passwordEncoder);
    }

    @Test
    @DisplayName("Expectativa: a senha é armazenada criptografada (BCrypt) no cadastro")
    void cadastrarUsuarioCenario1() {
        when(repository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        DadosDetalhamentoUsuario resultado = service.cadastrarUsuario(
                new DadosCadastroUsuario("novo.usuario", "senha123", null));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(captor.capture());
        Usuario salvo = captor.getValue();

        assertThat(salvo.getSenha()).isNotEqualTo("senha123");
        assertThat(passwordEncoder.matches("senha123", salvo.getSenha())).isTrue();
        assertThat(resultado.perfil()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("Expectativa: não permitir cadastrar dois usuários com o mesmo login")
    void cadastrarUsuarioCenario2() {
        when(repository.existsByLogin("admin")).thenReturn(true);

        assertThrows(ValidacaoException.class,
                () -> service.cadastrarUsuario(new DadosCadastroUsuario("admin", "senha123", Role.ADMIN)));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: alterar a própria senha quando a senha atual está correta")
    void alterarPropriaSenhaCenario1() {
        Usuario usuario = new Usuario(1L, "joao", passwordEncoder.encode("senha123"), Role.USER);
        when(repository.findUsuarioByLogin("joao")).thenReturn(Optional.of(usuario));

        service.alterarPropriaSenha("joao", new DadosAlteracaoSenha("senha123", "novaSenha456"));

        assertThat(passwordEncoder.matches("novaSenha456", usuario.getSenha())).isTrue();
        verify(repository).save(usuario);
    }

    @Test
    @DisplayName("Expectativa: não alterar a senha quando a senha atual está incorreta")
    void alterarPropriaSenhaCenario2() {
        Usuario usuario = new Usuario(1L, "joao", passwordEncoder.encode("senha123"), Role.USER);
        when(repository.findUsuarioByLogin("joao")).thenReturn(Optional.of(usuario));

        assertThrows(ValidacaoException.class,
                () -> service.alterarPropriaSenha("joao", new DadosAlteracaoSenha("senhaErrada", "novaSenha456")));
        assertThat(passwordEncoder.matches("senha123", usuario.getSenha())).isTrue();
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Expectativa: ADMIN atualiza o perfil de outro usuário")
    void atualizarPerfilCenario1() {
        Usuario usuario = new Usuario(2L, "maria", passwordEncoder.encode("senha123"), Role.USER);
        when(repository.findById(2L)).thenReturn(Optional.of(usuario));
        when(repository.save(usuario)).thenReturn(usuario);

        DadosDetalhamentoUsuario resultado = service.atualizarPerfil(2L, new DadosAtualizacaoPerfil(Role.ADMIN), "admin");

        assertThat(resultado.perfil()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Expectativa: ADMIN não pode excluir o próprio usuário")
    void excluirUsuarioCenario1() {
        Usuario admin = new Usuario(1L, "admin", passwordEncoder.encode("admin"), Role.ADMIN);
        when(repository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(ValidacaoException.class, () -> service.excluirUsuario(1L, "admin"));
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Expectativa: ADMIN exclui outro usuário")
    void excluirUsuarioCenario2() {
        Usuario usuario = new Usuario(2L, "maria", passwordEncoder.encode("senha123"), Role.USER);
        when(repository.findById(2L)).thenReturn(Optional.of(usuario));

        service.excluirUsuario(2L, "admin");

        verify(repository).delete(usuario);
    }
}
