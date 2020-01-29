package com.dnastack.ddap.explore.cli;

public class CartTokenNotFoundInStorageException extends RuntimeException {

    public CartTokenNotFoundInStorageException() {
    }

    public CartTokenNotFoundInStorageException(String message) {
        super(message);
    }
}
