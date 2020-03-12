package com.dnastack.ddap.common.setup;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoopConfigStrategy implements ConfigStrategy {

    @Override
    public void doOnetimeSetup() {
      log.info("Skipping One time setup, nothing to do!");
    }
}
