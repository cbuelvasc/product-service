package com.mercadolibre.domain.exception;

public class ProductDomainException extends RuntimeException {

    public ProductDomainException(String message) {
        super(message);
    }

    public ProductDomainException(String message, Throwable cause) {
        super(message, cause);
    }
} 