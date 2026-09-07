package com.zahid.inventoryapi.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        Map<String, List<String>> validationErrors) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, path, LocalDateTime.now(), null);
    }

    public static ErrorResponse ofValidation(int status, String error, String message, String path,
            Map<String, List<String>> validationErrors) {
        return new ErrorResponse(status, error, message, path, LocalDateTime.now(), validationErrors);
    }
}