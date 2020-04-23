package com.dnastack.ddap.common.util;

public class SessionCookieNotPresentException extends RuntimeException {

    public SessionCookieNotPresentException() {
        super();
    }

    public SessionCookieNotPresentException(String message) {
        super(message);
    }
}
