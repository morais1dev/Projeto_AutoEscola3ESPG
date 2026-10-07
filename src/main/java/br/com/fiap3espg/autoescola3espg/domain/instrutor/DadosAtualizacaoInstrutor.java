package br.com.fiap3espg.autoescola3espg.domain.instrutor;

import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosEndereco;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Dados permitidos na atualização de um instrutor: nome, telefone e endereço.
 * Os campos e-mail, CNH e especialidade existem apenas para que o sistema possa
 * identificar e REJEITAR uma tentativa de alteração (regra de negócio).
 */
public record DadosAtualizacaoInstrutor(
        @NotNull
        Long id,
        String nome,
        String telefone,
        DadosEndereco endereco,

        @Schema(hidden = true)
        String email,

        @Schema(hidden = true)
        String cnh,

        @Schema(hidden = true)
        Especialidade especialidade) {
}
