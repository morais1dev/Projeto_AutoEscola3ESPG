package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.domain.aluno.DadosCadastroAluno;
import br.com.fiap3espg.autoescola3espg.domain.aluno.DadosDetalhamentoAluno;
import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosEndereco;
import br.com.fiap3espg.autoescola3espg.service.AlunoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlunoControllerTest {
    private static final String JSON_CADASTRO_VALIDO = """
            {
                "nome": "Aluno Teste",
                "email": "aluno.teste@email.com",
                "telefone": "(11) 98765-4321",
                "cpf": "12345678901",
                "endereco": {
                    "logradouro": "Rua Teste",
                    "numero": "200",
                    "complemento": "Apto 1",
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
    AlunoService service;

    @Test
    @DisplayName("Expectativa: retornar código 400 ao cadastrar aluno com CPF inválido")
    @WithMockUser(roles = "ADMIN")
    void cadastrarAlunoCenario1() throws Exception {
        String jsonCpfInvalido = JSON_CADASTRO_VALIDO.replace("12345678901", "123");

        MockHttpServletResponse response = mockMvc
                .perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCpfInvalido))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("cpf");
        verify(service, never()).cadastrarAluno(any());
    }

    @Test
    @DisplayName("Expectativa: retornar código 201 e o header Location ao cadastrar aluno válido como ADMIN")
    @WithMockUser(roles = "ADMIN")
    void cadastrarAlunoCenario2() throws Exception {
        DadosDetalhamentoAluno detalhamento = new DadosDetalhamentoAluno(
                1L, "Aluno Teste", "aluno.teste@email.com", "(11) 98765-4321", "12345678901",
                new DadosEndereco("Rua Teste", "200", "Apto 1", "Centro", "São Paulo", "SP", "01001-000"),
                true);
        when(service.cadastrarAluno(any(DadosCadastroAluno.class))).thenReturn(detalhamento);

        MockHttpServletResponse response = mockMvc
                .perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_CADASTRO_VALIDO))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getHeader("Location")).endsWith("/alunos/1");
        assertThat(response.getContentAsString()).contains("Aluno Teste");
    }

    @Test
    @DisplayName("Expectativa: retornar código 403 quando um usuário USER tenta excluir aluno")
    @WithMockUser(roles = "USER")
    void excluirAlunoCenario1() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(delete("/alunos/1"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        verify(service, never()).excluirAluno(any());
    }

    @Test
    @DisplayName("Expectativa: retornar código 204 quando um ADMIN exclui (inativa) aluno")
    @WithMockUser(roles = "ADMIN")
    void excluirAlunoCenario2() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(delete("/alunos/1"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        verify(service).excluirAluno(1L);
    }
}
