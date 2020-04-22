package com.dnastack.ddap.explore.resource.spi;

public class SpiConfigurationException extends Exception {

    private static final long serialVersionUID = 957902579603950890L;

    public SpiConfigurationException() {
        super();
    }

    public SpiConfigurationException(String message) {
        super(message);
    }

    public SpiConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
