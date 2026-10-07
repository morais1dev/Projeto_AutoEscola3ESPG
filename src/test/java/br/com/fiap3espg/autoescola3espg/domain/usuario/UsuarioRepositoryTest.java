package br.com.fiap3espg.autoescola3espg.domain.usuario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UsuarioRepositoryTest {
    @Autowired
    UsuarioRepository repository;

    @Autowired
    TestEntityManager testEntity;

    @Test
    @DisplayName("Expectativa: encontrar usuário pelo login, com perfil e senha criptografada")
    void findByLoginCenario1() {
        String senhaCriptografada = new BCryptPasswordEncoder().encode("senha123");
        testEntity.persist(new Usuario("usuario.teste", senhaCriptografada, Role.USER));

        UserDetails usuario = repository.findByLogin("usuario.teste");

        assertThat(usuario).isNotNull();
        List<String> perfis = usuario.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        assertThat(perfis).containsExactly("ROLE_USER");
        assertThat(usuario.getPassword()).isNotEqualTo("senha123");
        assertThat(new BCryptPasswordEncoder().matches("senha123", usuario.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Expectativa: identificar logins existentes e inexistentes")
    void existsByLoginCenario1() {
        testEntity.persist(new Usuario("usuario.existente", new BCryptPasswordEncoder().encode("senha123"), Role.ADMIN));

        assertThat(repository.existsByLogin("usuario.existente")).isTrue();
        assertThat(repository.existsByLogin("usuario.inexistente")).isFalse();
        assertThat(repository.findUsuarioByLogin("usuario.existente")).isPresent();
    }
}
