package br.com.fiap3espg.autoescola3espg.infra.security;

public class TokenInvalidoException extends RuntimeException {
    public TokenInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
