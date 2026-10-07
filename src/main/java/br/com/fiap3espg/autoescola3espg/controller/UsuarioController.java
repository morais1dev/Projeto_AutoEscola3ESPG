package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.domain.usuario.DadosAlteracaoSenha;
import br.com.fiap3espg.autoescola3espg.domain.usuario.DadosAtualizacaoPerfil;
import br.com.fiap3espg.autoescola3espg.domain.usuario.DadosCadastroUsuario;
import br.com.fiap3espg.autoescola3espg.domain.usuario.DadosDetalhamentoUsuario;
import br.com.fiap3espg.autoescola3espg.service.UsuarioService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/usuarios")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Usuários", description = "Gerenciamento de usuários da API (cadastro, listagem, perfil e exclusão somente para ADMIN)")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastrar usuário com senha criptografada (somente ADMIN)")
    public ResponseEntity<DadosDetalhamentoUsuario> cadastrarUsuario(
            @RequestBody @Valid DadosCadastroUsuario dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoUsuario dto = service.cadastrarUsuario(dados);
        URI uri = uriBuilder
                .path("/usuarios/{id}")
                .buildAndExpand(dto.id())
                .toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuários (somente ADMIN)")
    public ResponseEntity<Page<DadosDetalhamentoUsuario>> listarUsuarios(
            @ParameterObject @PageableDefault(size = 10, sort = "login") Pageable paginacao) {
        return ResponseEntity.ok(service.listarUsuarios(paginacao));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Detalhar usuário (somente ADMIN)")
    public ResponseEntity<DadosDetalhamentoUsuario> detalharUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalharUsuario(id));
    }

    @PutMapping("/{id}/perfil")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar o perfil (USER/ADMIN) de um usuário (somente ADMIN)")
    public ResponseEntity<DadosDetalhamentoUsuario> atualizarPerfil(
            @PathVariable Long id,
            @RequestBody @Valid DadosAtualizacaoPerfil dados,
            @AuthenticationPrincipal UserDetails usuarioLogado) {
        return ResponseEntity.ok(service.atualizarPerfil(id, dados, usuarioLogado.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir usuário (somente ADMIN)")
    public ResponseEntity<Void> excluirUsuario(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails usuarioLogado) {
        service.excluirUsuario(id, usuarioLogado.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/senha")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alterar a própria senha (qualquer usuário autenticado)")
    public ResponseEntity<Void> alterarPropriaSenha(
            @RequestBody @Valid DadosAlteracaoSenha dados,
            @AuthenticationPrincipal UserDetails usuarioLogado) {
        service.alterarPropriaSenha(usuarioLogado.getUsername(), dados);
        return ResponseEntity.noContent().build();
    }
}
