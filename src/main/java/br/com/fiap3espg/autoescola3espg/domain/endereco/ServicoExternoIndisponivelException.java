package br.com.fiap3espg.autoescola3espg.domain.endereco;

public class ServicoExternoIndisponivelException extends RuntimeException {
    public ServicoExternoIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}
