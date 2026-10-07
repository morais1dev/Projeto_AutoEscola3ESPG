package br.com.fiap3espg.autoescola3espg.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health-check")
public class HealthCheckController {
    @GetMapping
    public String healthCheck() {
        return "Verificação de integridade da API da Auto Escola 3ESPG!";
    }
}