package com.dnastack.ddap.explore.cli;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class CommandLineResourceController {

    private final Resource cliZip;

    public CommandLineResourceController(@Value("classpath:/static/ddap-cli.zip") Resource cliZip) {
        this.cliZip = cliZip;
    }

    @GetMapping(path = "/api/v1alpha/cli/download", produces = "application/zip")
    public Resource downloadCliZip() {
        return cliZip;
    }

}
