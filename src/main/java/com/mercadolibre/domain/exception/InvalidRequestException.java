package com.mercadolibre.domain.exception;

/**
 * Thrown when the request is invalid (e.g. missing or empty required data such as product IDs).
 * Should be mapped to HTTP 422 Unprocessable Entity.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
