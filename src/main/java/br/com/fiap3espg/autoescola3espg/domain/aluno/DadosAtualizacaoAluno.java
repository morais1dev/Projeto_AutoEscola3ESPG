package br.com.fiap3espg.autoescola3espg.domain.aluno;

import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosEndereco;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Dados permitidos na atualização de um aluno: nome, telefone e endereço.
 * Os campos e-mail e CPF existem apenas para que o sistema possa
 * identificar e REJEITAR uma tentativa de alteração (regra de negócio).
 */
public record DadosAtualizacaoAluno(
        @NotNull
        Long id,
        String nome,
        String telefone,
        DadosEndereco endereco,

        @Schema(hidden = true)
        String email,

        @Schema(hidden = true)
        String cpf) {
}
