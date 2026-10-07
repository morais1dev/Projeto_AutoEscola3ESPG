package br.com.fiap3espg.autoescola3espg.domain.usuario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity(name = "Usuario")
@Table(name = "usuarios")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String login;
    private String senha;

    @Enumerated(EnumType.STRING)
    private Role perfil;

    /**
     * @param senhaCriptografada senha JÁ criptografada (BCrypt) - nunca a senha em texto puro
     */
    public Usuario(String login, String senhaCriptografada, Role perfil) {
        this.login = login;
        this.senha = senhaCriptografada;
        this.perfil = perfil != null ? perfil : Role.USER;
    }

    public void atualizarPerfil(Role perfil) {
        this.perfil = perfil;
    }

    /**
     * @param senhaCriptografada nova senha JÁ criptografada (BCrypt)
     */
    public void alterarSenha(String senhaCriptografada) {
        this.senha = senhaCriptografada;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role role = perfil != null ? perfil : Role.USER;
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
