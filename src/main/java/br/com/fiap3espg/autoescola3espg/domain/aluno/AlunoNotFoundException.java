package br.com.fiap3espg.autoescola3espg.domain.aluno;

public class AlunoNotFoundException extends RuntimeException {
    public AlunoNotFoundException(String message) {
        super(message);
    }
}