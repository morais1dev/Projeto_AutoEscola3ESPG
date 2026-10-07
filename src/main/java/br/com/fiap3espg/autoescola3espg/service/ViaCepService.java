package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.endereco.CepNaoEncontradoException;
import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosEndereco;
import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosViaCep;
import br.com.fiap3espg.autoescola3espg.domain.endereco.ServicoExternoIndisponivelException;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Consumo de WebService externo: consulta de endereço pelo CEP na API pública ViaCEP.
 */
@Service
public class ViaCepService {
    private final RestClient restClient;

    @Autowired
    public ViaCepService(@Value("${api.viacep.url:https://viacep.com.br/ws}") String baseUrl) {
        this(RestClient.builder().baseUrl(baseUrl));
    }

    //Construtor usado nos testes automatizados (permite simular o servidor do ViaCEP)
    ViaCepService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public DadosEndereco buscarEndereco(String cep) {
        String cepNumerico = cep == null ? "" : cep.replaceAll("\\D", "");
        if (cepNumerico.length() != 8) {
            throw new ValidacaoException("CEP inválido! Informe 8 dígitos (ex: 01001-000 ou 01001000).");
        }

        DadosViaCep resposta;
        try {
            resposta = restClient
                    .get()
                    .uri("/{cep}/json/", cepNumerico)
                    .retrieve()
                    .body(DadosViaCep.class);
        } catch (RestClientException ex) {
            throw new ServicoExternoIndisponivelException("Não foi possível consultar o ViaCEP no momento!", ex);
        }

        if (resposta == null || resposta.cepInexistente()) {
            throw new CepNaoEncontradoException("CEP " + cep + " não encontrado!");
        }

        return new DadosEndereco(
                resposta.logradouro(),
                null,
                resposta.complemento(),
                resposta.bairro(),
                resposta.localidade(),
                resposta.uf(),
                resposta.cep()
        );
    }
}
