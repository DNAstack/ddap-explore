package com.dnastack.ddap.explore.dataset.client;

import lombok.Getter;

public class TableErrorException extends RuntimeException {

    @Getter
    private Integer status;

    public TableErrorException(Integer status, String message) {
        super(message);
        this.status = status;
    }

    public TableErrorException(Integer status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
