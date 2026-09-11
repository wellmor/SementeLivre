package com.sementelivre.backend.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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

    //trata erro 404 (recurso não encontrado) 
    @ExceptionHandler({
        ResourceNotFoundException.class,
        RecursoNaoEncontradoException.class,
        NoSuchElementException.class
})
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

    //trata erro 409 - conflito no bd (ex: email duplicado)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseExceptions(DataIntegrityViolationException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT; 
        
        ErrorResponse err = new ErrorResponse(
            "Database Conflict",
            "Violação de integridade nos dados.",
            Instant.now(),
            status.value(),
            request.getRequestURI(),
            null
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(UploadFotoException.class)
    public ResponseEntity<ErrorResponse> handleUploadFotoException(
        UploadFotoException ex,
        HttpServletRequest request) {

        ErrorResponse erro = new ErrorResponse(
            "Erro no upload da foto",
            ex.getMessage(),
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            null
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(erro);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException ex,
        HttpServletRequest request) {

    ErrorResponse erro = new ErrorResponse(
            "Requisição inválida",
            ex.getMessage(),
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI(),
            null
    );

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(erro);
    }

}
