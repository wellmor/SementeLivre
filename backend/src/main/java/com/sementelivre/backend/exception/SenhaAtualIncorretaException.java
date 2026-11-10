package com.sementelivre.backend.exception;

public class SenhaAtualIncorretaException extends RuntimeException {
    public SenhaAtualIncorretaException(String message) {
        super(message);
    }
}
