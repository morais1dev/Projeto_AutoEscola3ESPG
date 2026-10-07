package br.com.fiap3espg.autoescola3espg.domain.usuario;

import jakarta.validation.constraints.NotNull;

public record DadosAtualizacaoPerfil(
        @NotNull
        Role perfil) {
}
