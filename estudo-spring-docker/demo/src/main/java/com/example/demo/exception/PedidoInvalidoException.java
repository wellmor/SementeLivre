package com.example.demo.exception;

public class PedidoInvalidoException extends RuntimeException {
    public PedidoInvalidoException(String message) {
        super(message);
    }
}
