package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.endereco.CepNaoEncontradoException;
import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosEndereco;
import br.com.fiap3espg.autoescola3espg.domain.endereco.ServicoExternoIndisponivelException;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Teste do consumo do WebService externo ViaCEP, simulando o servidor externo
 * (o teste não depende de acesso à internet).
 */
class ViaCepServiceTest {
    private static final String URL_BASE = "https://viacep.com.br/ws";

    MockRestServiceServer servidorViaCep;
    ViaCepService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(URL_BASE);
        servidorViaCep = MockRestServiceServer.bindTo(builder).build();
        service = new ViaCepService(builder);
    }

    @Test
    @DisplayName("Expectativa: converter a resposta do ViaCEP em endereço")
    void buscarEnderecoCenario1() {
        String respostaViaCep = """
                {
                  "cep": "01001-000",
                  "logradouro": "Praça da Sé",
                  "complemento": "lado ímpar",
                  "unidade": "",
                  "bairro": "Sé",
                  "localidade": "São Paulo",
                  "uf": "SP",
                  "estado": "São Paulo",
                  "regiao": "Sudeste",
                  "ibge": "3550308",
                  "gia": "1004",
                  "ddd": "11",
                  "siafi": "7107"
                }
                """;
        servidorViaCep
                .expect(requestTo(URL_BASE + "/01001000/json/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(respostaViaCep, MediaType.APPLICATION_JSON));

        DadosEndereco endereco = service.buscarEndereco("01001-000");

        servidorViaCep.verify();
        assertThat(endereco.logradouro()).isEqualTo("Praça da Sé");
        assertThat(endereco.bairro()).isEqualTo("Sé");
        assertThat(endereco.cidade()).isEqualTo("São Paulo");
        assertThat(endereco.uf()).isEqualTo("SP");
        assertThat(endereco.cep()).isEqualTo("01001-000");
    }

    @Test
    @DisplayName("Expectativa: lançar exceção quando o ViaCEP informa que o CEP não existe")
    void buscarEnderecoCenario2() {
        servidorViaCep
                .expect(requestTo(URL_BASE + "/99999999/json/"))
                .andRespond(withSuccess("{\"erro\": \"true\"}", MediaType.APPLICATION_JSON));

        assertThrows(CepNaoEncontradoException.class, () -> service.buscarEndereco("99999-999"));
    }

    @Test
    @DisplayName("Expectativa: rejeitar CEP com formato inválido sem chamar o ViaCEP")
    void buscarEnderecoCenario3() {
        assertThrows(ValidacaoException.class, () -> service.buscarEndereco("123"));
        servidorViaCep.verify();
    }

    @Test
    @DisplayName("Expectativa: lançar exceção quando o ViaCEP está fora do ar")
    void buscarEnderecoCenario4() {
        servidorViaCep
                .expect(requestTo(URL_BASE + "/01001000/json/"))
                .andRespond(withServerError());

        assertThrows(ServicoExternoIndisponivelException.class, () -> service.buscarEndereco("01001000"));
    }
}
