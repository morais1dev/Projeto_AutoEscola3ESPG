package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.domain.instrutor.*;
import br.com.fiap3espg.autoescola3espg.service.InstrutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/instrutores")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Instrutores", description = "CRUD de instrutores da auto-escola")
@RequiredArgsConstructor
public class InstrutorController {
    private final InstrutorService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") //Decisão de gerenciar autorizações pelo controller
    @Operation(summary = "Cadastrar instrutor (somente ADMIN)")
    public ResponseEntity<DadosDetalhamentoInstrutor> cadastrarInstrutor(
            @RequestBody @Valid DadosCadastroInstrutor dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoInstrutor dto = service.cadastrarInstrutor(dados);
        URI uri = uriBuilder
                .path("/instrutores/{id}")
                .buildAndExpand(dto.id())
                .toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") //Decisão de gerenciar autorizações pelo controller
    @Operation(summary = "Listar instrutores ativos (paginado, 10 por página, ordenado por nome)")
    public ResponseEntity<Page<DadosListagemInstrutor>> listarInstrutores(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(service.listarInstrutores(paginacao));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") //Decisão de gerenciar autorizações pelo controller
    @Operation(summary = "Detalhar instrutor (somente ADMIN)")
    public ResponseEntity<DadosDetalhamentoInstrutor> detalharInstrutor(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.detalharInstrutor(id));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')") //Decisão de gerenciar autorizações pelo controller
    @Operation(summary = "Atualizar nome, telefone e/ou endereço do instrutor (somente ADMIN)")
    public ResponseEntity<DadosDetalhamentoInstrutor> atualizarInstrutor(
            @RequestBody @Valid DadosAtualizacaoInstrutor dados) {
        return ResponseEntity.ok(service.atualizarInstrutor(dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") //Decisão de gerenciar autorizações pelo controller
    @Operation(summary = "Excluir (inativar) instrutor (somente ADMIN)")
    public ResponseEntity<Void> excluirInstrutor(
            @PathVariable Long id) {
        service.excluirInstrutor(id);
        return ResponseEntity.noContent().build();
    }
}
