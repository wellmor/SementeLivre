package com.sementelivre.backend.exception;

public class PessoaNaoEncontradaException extends ResourceNotFoundException {
    public PessoaNaoEncontradaException(String message) {
        super(message);
    }
}
