package br.com.fiap3espg.autoescola3espg.domain.endereco;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Resposta do WebService externo ViaCEP (https://viacep.com.br).
 * Quando o CEP não existe, o ViaCEP devolve apenas {"erro": true} (ou "true").
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DadosViaCep(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        Object erro) {

    public boolean cepInexistente() {
        return erro != null && Boolean.parseBoolean(erro.toString());
    }
}
