package br.com.fiap3espg.autoescola3espg.controller;

import br.com.fiap3espg.autoescola3espg.domain.endereco.DadosEndereco;
import br.com.fiap3espg.autoescola3espg.service.ViaCepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enderecos")
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Endereços (ViaCEP)", description = "Consulta de endereço por CEP consumindo o WebService externo ViaCEP")
@RequiredArgsConstructor
public class EnderecoController {
    private final ViaCepService viaCepService;

    @GetMapping("/{cep}")
    @Operation(summary = "Buscar endereço pelo CEP (API externa ViaCEP). O retorno pode ser usado no cadastro de alunos e instrutores.")
    public ResponseEntity<DadosEndereco> buscarEnderecoPorCep(@PathVariable String cep) {
        return ResponseEntity.ok(viaCepService.buscarEndereco(cep));
    }
}
