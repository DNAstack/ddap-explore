package com.dnastack.ddap.common;

public class PolicyRequirementFailedException extends RuntimeException {

    public PolicyRequirementFailedException() {
    }

    public PolicyRequirementFailedException(String message) {
        super(message);
    }
}
