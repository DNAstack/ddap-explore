package com.dnastack.ddap.explore.resource.exception;

public class IllegalIdentifierException extends RuntimeException {

    public IllegalIdentifierException() {
    }

    public IllegalIdentifierException(String message) {
        super(message);
    }

    public IllegalIdentifierException(String message, Throwable cause) {
        super(message, cause);
    }
}
