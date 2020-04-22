package com.dnastack.ddap.explore.resource.exception;

public class ResourceIdEncodingException extends RuntimeException {

    public ResourceIdEncodingException() {
        super();
    }

    public ResourceIdEncodingException(String message) {
        super(message);
    }

    public ResourceIdEncodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
