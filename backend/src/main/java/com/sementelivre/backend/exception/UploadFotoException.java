package com.sementelivre.backend.exception;

public class UploadFotoException extends RuntimeException {

    public UploadFotoException(String message) {
        super(message);
    }

    public UploadFotoException(String message, Throwable cause) {
        super(message, cause);
    }
}