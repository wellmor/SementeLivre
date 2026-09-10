package com.example.demo.exception;

public class PessoaNaoEncontradaException extends RuntimeException {
    public PessoaNaoEncontradaException(String message) {
        super(message);
    }
}
