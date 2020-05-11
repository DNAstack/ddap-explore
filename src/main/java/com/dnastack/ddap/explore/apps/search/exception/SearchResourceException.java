package com.dnastack.ddap.explore.apps.search.exception;

import lombok.Getter;


public class SearchResourceException extends RuntimeException {
    @Getter
    private final int status;

    public SearchResourceException(String message, int status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
