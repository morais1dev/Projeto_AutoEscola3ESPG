package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.domain.usuario.DadosAlteracaoSenha;
import br.com.fiap3espg.autoescola3espg.domain.usuario.DadosCadastroUsuario;
import br.com.fiap3espg.autoescola3espg.domain.usuario.DadosDetalhamentoUsuario;
import br.com.fiap3espg.autoescola3espg.domain.usuario.Role;
import br.com.fiap3espg.autoescola3espg.service.UsuarioService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UsuarioService service;

    @Test
    @DisplayName("Expectativa: ADMIN cadastra usuário (201) e a senha não é devolvida na resposta")
    @WithMockUser(roles = "ADMIN")
    void cadastrarUsuarioCenario1() throws Exception {
        when(service.cadastrarUsuario(any(DadosCadastroUsuario.class)))
                .thenReturn(new DadosDetalhamentoUsuario(2L, "novo.usuario", Role.USER));

        MockHttpServletResponse response = mockMvc
                .perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"novo.usuario\", \"senha\": \"senha123\", \"perfil\": \"USER\"}"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getHeader("Location")).endsWith("/usuarios/2");
        assertThat(response.getContentAsString()).doesNotContain("senha");
    }

    @Test
    @DisplayName("Expectativa: retornar código 403 quando um usuário USER tenta cadastrar usuário")
    @WithMockUser(roles = "USER")
    void cadastrarUsuarioCenario2() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"novo.usuario\", \"senha\": \"senha123\"}"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        verify(service, never()).cadastrarUsuario(any());
    }

    @Test
    @DisplayName("Expectativa: retornar código 403 quando um usuário USER tenta listar usuários")
    @WithMockUser(roles = "USER")
    void listarUsuariosCenario1() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(get("/usuarios"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Expectativa: usuário autenticado altera a própria senha (204)")
    @WithMockUser(username = "joao", roles = "USER")
    void alterarPropriaSenhaCenario1() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(put("/usuarios/senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\": \"senha123\", \"novaSenha\": \"novaSenha456\"}"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        verify(service).alterarPropriaSenha(eq("joao"), any(DadosAlteracaoSenha.class));
    }

    @Test
    @DisplayName("Expectativa: retornar código 400 quando a nova senha é muito curta")
    @WithMockUser(username = "joao", roles = "USER")
    void alterarPropriaSenhaCenario2() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(put("/usuarios/senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\": \"senha123\", \"novaSenha\": \"123\"}"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(service, never()).alterarPropriaSenha(any(), any());
    }
}
