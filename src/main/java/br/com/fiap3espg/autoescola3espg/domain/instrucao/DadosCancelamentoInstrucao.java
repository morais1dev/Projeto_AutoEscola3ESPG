package br.com.fiap3espg.autoescola3espg.domain.instrucao;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record DadosCancelamentoInstrucao(
        @NotNull
        @JsonProperty("id_instrucao")
        Long idInstrucao,

        @NotNull
        MotivoCancelamento motivo) {
}
