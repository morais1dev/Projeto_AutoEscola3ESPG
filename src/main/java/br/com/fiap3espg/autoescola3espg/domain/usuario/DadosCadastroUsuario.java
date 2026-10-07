package br.com.fiap3espg.autoescola3espg.domain.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosCadastroUsuario(
        @NotBlank
        @Size(max = 100)
        String login,

        @NotBlank
        @Size(min = 6, max = 100, message = "deve ter entre 6 e 100 caracteres")
        String senha,

        @Schema(description = "Perfil do usuário. Se não informado, será USER.")
        Role perfil) {
}
