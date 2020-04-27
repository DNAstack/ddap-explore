package com.dnastack.ddap.explore.apps.dataset.controller;

import com.dnastack.ddap.common.controller.DdapErrorResponse;
import com.dnastack.ddap.explore.apps.dataset.client.TableErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class TableExceptionHandler {

    @ExceptionHandler(TableErrorException.class)
    public ResponseEntity<DdapErrorResponse> handle(TableErrorException ex) {
        int status = ex.getStatus() == null ? 500 : ex.getStatus();
        return ResponseEntity.status(status).body(new DdapErrorResponse(ex.getMessage(), status));
    }

}
