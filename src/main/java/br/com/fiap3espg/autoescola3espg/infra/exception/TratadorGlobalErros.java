package br.com.fiap3espg.autoescola3espg.infra.exception;

import br.com.fiap3espg.autoescola3espg.domain.aluno.AlunoNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.endereco.CepNaoEncontradoException;
import br.com.fiap3espg.autoescola3espg.domain.endereco.ServicoExternoIndisponivelException;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.InstrucaoNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.InstrutorNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.usuario.UsuarioNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class TratadorGlobalErros {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> tratarNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DadosBadRequest>> tratarBadRequest(
            MethodArgumentNotValidException ex) {
        List<FieldError> erros = ex.getFieldErrors();
        return ResponseEntity
                .badRequest()
                .body(erros
                        .stream()
                        .map(DadosBadRequest::new)
                        .toList()
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<DadosMessage> tratarCorpoInvalido() {
        return ResponseEntity
                .badRequest()
                .body(new DadosMessage("Corpo da requisição ausente ou inválido (verifique o JSON, os formatos de data e os valores permitidos)!"));
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<DadosMessage> tratarRegraDeNegocio(ValidacaoException ex) {
        return ResponseEntity
                .badRequest()
                .body(new DadosMessage(ex.getMessage()));
    }

    @ExceptionHandler({
            InstrutorNotFoundException.class,
            AlunoNotFoundException.class,
            UsuarioNotFoundException.class,
            InstrucaoNotFoundException.class,
            CepNaoEncontradoException.class
    })
    public ResponseEntity<DadosMessage> tratarRecursoNaoEncontrado(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new DadosMessage(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<DadosMessage> tratarViolacaoIntegridade() {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new DadosMessage("Operação viola a integridade dos dados (ex: registro duplicado)!"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<DadosMessage> tratarCredenciaisInvalidas() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new DadosMessage("Login ou senha inválidos!"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<DadosMessage> tratarFalhaAutenticacao() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new DadosMessage("Falha na autenticação!"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<DadosMessage> tratarAcessoNegado() {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new DadosMessage("Acesso negado: seu perfil não tem permissão para esta operação!"));
    }

    @ExceptionHandler(ServicoExternoIndisponivelException.class)
    public ResponseEntity<DadosMessage> tratarServicoExterno(ServicoExternoIndisponivelException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new DadosMessage(ex.getMessage()));
    }

    private record DadosBadRequest(
            String field,
            String message) {
        public DadosBadRequest(FieldError erro) {
            this(erro.getField(), erro.getDefaultMessage());
        }
    }

    private record DadosMessage(String message) {
    }
}
