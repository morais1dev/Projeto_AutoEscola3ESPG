package br.com.fiap3espg.autoescola3espg.domain.instrutor;

public class InstrutorNotFoundException extends RuntimeException {
    public InstrutorNotFoundException(String message) {
        super(message);
    }
}