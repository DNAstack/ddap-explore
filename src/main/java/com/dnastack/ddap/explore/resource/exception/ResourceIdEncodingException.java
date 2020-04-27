package com.dnastack.ddap.explore.resource.exception;

public class ResourceIdEncodingException extends IllegalArgumentException {

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
