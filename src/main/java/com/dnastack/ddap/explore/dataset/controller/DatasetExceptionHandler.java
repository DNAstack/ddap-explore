package com.dnastack.ddap.explore.dataset.controller;

import com.dnastack.ddap.common.controller.DdapErrorResponse;
import com.dnastack.ddap.explore.dataset.client.DatasetErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class DatasetExceptionHandler {

    @ExceptionHandler(DatasetErrorException.class)
    public ResponseEntity<DdapErrorResponse> handle(DatasetErrorException ex) {
        int status = ex.getStatus() == null ? 500 : ex.getStatus();
        return ResponseEntity.status(status).body(new DdapErrorResponse(ex.getMessage(), status));
    }

}
