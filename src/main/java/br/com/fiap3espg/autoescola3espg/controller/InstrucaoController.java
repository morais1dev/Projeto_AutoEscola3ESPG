package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosAgendamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosCancelamentoInstrucao;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.DadosDetalhamentoAgendamento;
import br.com.fiap3espg.autoescola3espg.service.InstrucaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/instrucoes")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Instruções", description = "Agendamento, consulta e cancelamento de instruções")
@RequiredArgsConstructor
public class InstrucaoController {
    private final InstrucaoService service;

    @PostMapping
    @Operation(summary = "Agendar instrução (se o instrutor não for informado, um instrutor disponível da especialidade é escolhido aleatoriamente)")
    public ResponseEntity<DadosDetalhamentoAgendamento> agendarInstrucao(@RequestBody @Valid DadosAgendamentoInstrucao dados) {
        return ResponseEntity.ok(service.agendarInstrucao(dados));
    }

    @GetMapping
    @Operation(summary = "Listar instruções (paginado, ordenado por data/hora)")
    public ResponseEntity<Page<DadosDetalhamentoAgendamento>> listarInstrucoes(
            @ParameterObject @PageableDefault(size = 10, sort = "dataHora", direction = Sort.Direction.ASC) Pageable paginacao) {
        return ResponseEntity.ok(service.listarInstrucoes(paginacao));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar instrução")
    public ResponseEntity<DadosDetalhamentoAgendamento> detalharInstrucao(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalharInstrucao(id));
    }

    @DeleteMapping
    @Operation(summary = "Cancelar instrução (motivo obrigatório; antecedência mínima de 24 horas)")
    public ResponseEntity<DadosDetalhamentoAgendamento> cancelarInstrucao(@RequestBody @Valid DadosCancelamentoInstrucao dados) {
        return ResponseEntity.ok(service.cancelarInstrucao(dados));
    }
}
