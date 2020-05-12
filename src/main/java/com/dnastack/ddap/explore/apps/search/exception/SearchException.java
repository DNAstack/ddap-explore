package com.dnastack.ddap.explore.apps.search.exception;

public class SearchException extends RuntimeException {

    public SearchException() {
        super();
    }

    public SearchException(String message) {
        super(message);
    }

    public SearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
