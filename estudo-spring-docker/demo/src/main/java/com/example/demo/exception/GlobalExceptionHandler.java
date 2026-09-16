package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<String, String> MENSAGENS_POR_CONSTRAINT = Map.of(
            "uk_pessoa_email", "E-mail já cadastrado no sistema.",
            "uk_pessoa_documento", "Documento já cadastrado no sistema.",
            "uk_proprietario_rg", "RG já cadastrado no sistema."
    );

    private static final String MENSAGEM_GENERICA_INTEGRIDADE =
            "Violação de integridade de dados (ex: registro já existe ou possui dependências).";

    @ExceptionHandler({EmailJaCadastradoException.class, DocumentoJaCadastradoException.class, RgJaCadastradoException.class})
    public ResponseEntity<ErrorResponse> handleConflictExceptions(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = mensagemParaViolacao(ex);
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", message, request.getRequestURI(), null);
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

    @ExceptionHandler(PessoaNaoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", "Erro de validação.", request.getRequestURI(), details);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Ocorreu um erro inesperado.", request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message, String path, List<String> details) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(Instant.now());
        errorResponse.setStatus(status.value());
        errorResponse.setError(error);
        errorResponse.setMessage(message);
        errorResponse.setPath(path);
        if (details != null && !details.isEmpty()) {
            errorResponse.setDetails(details);
        }
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(PedidoInvalidoException.class)
    public ResponseEntity<Object> handleBadRequest(RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
