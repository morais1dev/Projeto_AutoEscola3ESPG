package br.com.fiap3espg.autoescola3espg.domain.endereco;

public class CepNaoEncontradoException extends RuntimeException {
    public CepNaoEncontradoException(String message) {
        super(message);
    }
}
