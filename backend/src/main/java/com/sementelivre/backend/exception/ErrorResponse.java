package com.sementelivre.backend.exception;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class ErrorResponse {
    private String error;
    private String message;
    private Instant timestamp;
    private Integer status;
    private String path;
    private List<String> fieldErrors;

    public ErrorResponse(){}

    public ErrorResponse(String error, String message, Instant timestamp, Integer status, String path, List<String> fieldErrors){
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }
    
}
