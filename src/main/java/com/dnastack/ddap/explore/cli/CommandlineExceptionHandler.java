package com.dnastack.ddap.explore.cli;

import com.dnastack.ddap.common.controller.DdapErrorResponse;
import com.dnastack.ddap.common.security.UserTokenCookiePackager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CommandlineExceptionHandler {

    @Autowired
    private UserTokenCookiePackager cookiePackager;

    @ExceptionHandler(CliSessionNotFoundException.class)
    public ResponseEntity<DdapErrorResponse> handle(RuntimeException ex) {
        return ResponseEntity.status(400).body(new DdapErrorResponse(ex.getMessage(), 400));
    }

}
