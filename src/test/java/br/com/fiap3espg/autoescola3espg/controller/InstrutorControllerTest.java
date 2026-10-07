package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosEndereco;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.DadosCadastroInstrutor;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.DadosDetalhamentoInstrutor;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.Especialidade;
import br.com.fiap3espg.autoescola3espg.service.InstrutorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InstrutorControllerTest {
    private static final String JSON_CADASTRO_VALIDO = """
            {
                "nome": "Instrutor Teste",
                "email": "instrutor.teste@email.com",
                "telefone": "(11) 91234-5678",
                "cnh": "01234567890",
                "especialidade": "CARROS",
                "endereco": {
                    "logradouro": "Rua Teste",
                    "numero": "100",
                    "complemento": "Casa",
                    "bairro": "Centro",
                    "cidade": "São Paulo",
                    "uf": "SP",
                    "cep": "01001-000"
                }
            }
            """;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    InstrutorService service;

    @Test
    @DisplayName("Expectativa: retornar código 400 ao cadastrar instrutor com informações inválidas")
    @WithMockUser(roles = "ADMIN")
    void cadastrarInstrutorCenario1() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(post("/instrutores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(service, never()).cadastrarInstrutor(any());
    }

    @Test
    @DisplayName("Expectativa: retornar código 201 e o header Location ao cadastrar instrutor válido como ADMIN")
    @WithMockUser(roles = "ADMIN")
    void cadastrarInstrutorCenario2() throws Exception {
        DadosDetalhamentoInstrutor detalhamento = new DadosDetalhamentoInstrutor(
                1L, "Instrutor Teste", "instrutor.teste@email.com", "(11) 91234-5678",
                "01234567890", Especialidade.CARROS,
                new DadosEndereco("Rua Teste", "100", "Casa", "Centro", "São Paulo", "SP", "01001-000"),
                true);
        when(service.cadastrarInstrutor(any(DadosCadastroInstrutor.class))).thenReturn(detalhamento);

        MockHttpServletResponse response = mockMvc
                .perform(post("/instrutores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_CADASTRO_VALIDO))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getHeader("Location")).endsWith("/instrutores/1");
    }

    @Test
    @DisplayName("Expectativa: retornar código 403 quando um usuário USER tenta cadastrar instrutor")
    @WithMockUser(roles = "USER")
    void cadastrarInstrutorCenario3() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(post("/instrutores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_CADASTRO_VALIDO))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        verify(service, never()).cadastrarInstrutor(any());
    }

    @Test
    @DisplayName("Expectativa: retornar código 401 ao listar instrutores sem token")
    void listarInstrutoresCenario1() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(get("/instrutores"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
