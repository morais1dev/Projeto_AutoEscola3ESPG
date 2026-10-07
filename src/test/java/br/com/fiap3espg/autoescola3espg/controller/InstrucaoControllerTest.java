package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosCancelamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosDetalhamentoAgendamento;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.MotivoCancelamento;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.Especialidade;
import br.com.fiap3espg.autoescola3espg.service.InstrucaoService;
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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

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
public class InstrucaoControllerTest {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    InstrucaoService agenda;

    @Test
    @DisplayName("Expectativa: retornar código 400 para informações inválidas")
    @WithMockUser
    void agendarInstrucaoCenario1() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(post("/instrucoes"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Expectativa: retornar código 200 quando as informações do agendamento são válidas")
    @WithMockUser
    void agendarInstrucaoCenario2() throws Exception {
        LocalDateTime proximaSegundaAs10 = proximaSegundaAs10();
        DadosDetalhamentoAgendamento detalhamento = new DadosDetalhamentoAgendamento(
                1L, 1L, "Aluno Teste", 1L, "Instrutor Teste",
                Especialidade.CARROS, proximaSegundaAs10, false, null);
        when(agenda.agendarInstrucao(any(DadosAgendamentoInstrucao.class))).thenReturn(detalhamento);

        String json = """
                {
                    "id_aluno": 1,
                    "id_instrutor": 1,
                    "data_hora": "%s"
                }
                """.formatted(proximaSegundaAs10.format(FORMATO));

        MockHttpServletResponse response = mockMvc
                .perform(post("/instrucoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).contains("Aluno Teste", "Instrutor Teste");
    }

    @Test
    @DisplayName("Expectativa: retornar código 401 quando a requisição não possui token")
    void agendarInstrucaoCenario3() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(post("/instrucoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("Expectativa: retornar código 400 ao cancelar instrução sem informar o motivo")
    @WithMockUser
    void cancelarInstrucaoCenario1() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(delete("/instrucoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id_instrucao\": 1}"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(agenda, never()).cancelarInstrucao(any());
    }

    @Test
    @DisplayName("Expectativa: retornar código 400 ao cancelar instrução com motivo fora das opções permitidas")
    @WithMockUser
    void cancelarInstrucaoCenario2() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(delete("/instrucoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id_instrucao\": 1, \"motivo\": \"MOTIVO_INVALIDO\"}"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Expectativa: retornar código 200 ao cancelar instrução com dados válidos")
    @WithMockUser
    void cancelarInstrucaoCenario3() throws Exception {
        DadosDetalhamentoAgendamento cancelada = new DadosDetalhamentoAgendamento(
                1L, 1L, "Aluno Teste", 1L, "Instrutor Teste",
                Especialidade.CARROS, proximaSegundaAs10(), true, MotivoCancelamento.ALUNO_DESISTIU);
        when(agenda.cancelarInstrucao(any(DadosCancelamentoInstrucao.class))).thenReturn(cancelada);

        MockHttpServletResponse response = mockMvc
                .perform(delete("/instrucoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id_instrucao\": 1, \"motivo\": \"ALUNO_DESISTIU\"}"))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).contains("ALUNO_DESISTIU");
    }

    private LocalDateTime proximaSegundaAs10() {
        return LocalDateTime
                .now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }
}
