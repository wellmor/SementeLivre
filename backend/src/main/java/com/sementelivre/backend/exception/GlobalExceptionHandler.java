package com.sementelivre.backend.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<String, String> MENSAGENS_POR_CONSTRAINT = Map.of(
            "uk_pessoa_email", "E-mail já cadastrado no sistema.",
            "uk_pessoa_documento", "Documento já cadastrado no sistema.",
            "uk_proprietario_rg", "RG já cadastrado no sistema."
    );

    private static final String MENSAGEM_GENERICA_INTEGRIDADE = "Violação de integridade nos dados.";

    //trata erro 404 (recurso não encontrado)
    @ExceptionHandler({ResourceNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFound(RuntimeException e, HttpServletRequest request){
        HttpStatus status = HttpStatus.NOT_FOUND;

        ErrorResponse err = new ErrorResponse(
            "Resource Not Found",
            e.getMessage(),
            Instant.now(),
            status.value(),
            request.getRequestURI(),
            null
        );
        return ResponseEntity.status(status).body(err);
    }

    //trata os conflitos de negócio de pessoa (email/documento/RG duplicado), verificados no service antes de persistir
    @ExceptionHandler({EmailJaCadastradoException.class, DocumentoJaCadastradoException.class, RgJaCadastradoException.class})
    public ResponseEntity<ErrorResponse> handleConflitosDePessoa(RuntimeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;

        ErrorResponse err = new ErrorResponse(
            "Conflict",
            e.getMessage(),
            Instant.now(),
            status.value(),
            request.getRequestURI(),
            null
        );
        return ResponseEntity.status(status).body(err);
    }

    //trata erro 400 (Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        //extrai os erros especificos de cada campo do DTO
        List<String> validationErrors = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            validationErrors.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }

        ErrorResponse err = new ErrorResponse(
            "Validation Error",
            "Um ou mais campos estão inválidos",
            Instant.now(),
            status.value(),
            request.getRequestURI(),
            validationErrors
        );
        return ResponseEntity.status(status).body(err);
    }

    //trata erro 400 (documento com dígito verificador/tipo/tamanho inválido, validado no service)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse err = new ErrorResponse(
            "Bad Request",
            e.getMessage(),
            Instant.now(),
            status.value(),
            request.getRequestURI(),
            null
        );
        return ResponseEntity.status(status).body(err);
    }

    //trata erro 409 - conflito no bd (ex: corrida entre o check de unicidade do service e o insert;
    //a constraint UNIQUE do banco barra a segunda). Tenta identificar qual campo colidiu pelo nome da
    //constraint; se não reconhecer (FK, NOT NULL etc.), mantém a mensagem genérica.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseExceptions(DataIntegrityViolationException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;

        ErrorResponse err = new ErrorResponse(
            "Database Conflict",
            mensagemParaViolacao(e),
            Instant.now(),
            status.value(),
            request.getRequestURI(),
            null
        );
        return ResponseEntity.status(status).body(err);
    }

    private String mensagemParaViolacao(DataIntegrityViolationException ex) {
        String constraintName = extrairNomeConstraint(ex);
        if (constraintName != null && MENSAGENS_POR_CONSTRAINT.containsKey(constraintName)) {
            return MENSAGENS_POR_CONSTRAINT.get(constraintName);
        }

        // Fallback defensivo: getConstraintName() pode vir nulo dependendo do driver/dialect;
        // nesse caso tenta reconhecer a constraint pelo texto da mensagem da causa raiz.
        Throwable causaMaisEspecifica = ex.getMostSpecificCause();
        String mensagemCausa = causaMaisEspecifica != null ? causaMaisEspecifica.getMessage() : null;
        if (mensagemCausa != null) {
            for (Map.Entry<String, String> entrada : MENSAGENS_POR_CONSTRAINT.entrySet()) {
                if (mensagemCausa.contains(entrada.getKey())) {
                    return entrada.getValue();
                }
            }
        }

        return MENSAGEM_GENERICA_INTEGRIDADE;
    }

    private String extrairNomeConstraint(Throwable ex) {
        Throwable atual = ex;
        while (atual != null) {
            if (atual instanceof ConstraintViolationException cve) {
                return cve.getConstraintName();
            }
            atual = atual.getCause();
        }
        return null;
    }
}
