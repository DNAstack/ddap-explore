package com.dnastack.ddap.common.setup;

import com.dnastack.ddap.common.TestingPersona;
import com.dnastack.ddap.common.page.AnyDdapPage;
import java.io.IOException;
import java.net.URI;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.CookieStore;
import org.openqa.selenium.WebDriver;

@Slf4j
public class NoopConfigStrategy implements ConfigStrategy, LoginStrategy {

    @Override
    public void doOnetimeSetup() {
        log.info("Skipping One time setup, nothing to do!");
    }

    @Override
    public CookieStore performPersonaLogin(String personaName, String realmName, String... scopes) throws IOException {
        throw new UnsupportedOperationException("Perfoming Persona login with This Strategy is not supported");
    }

    @Override
    public <T extends AnyDdapPage> T performPersonaLogin(WebDriver driver, TestingPersona persona, String realmName, Function<WebDriver, T> pageFactory) throws IOException {
        return pageFactory.apply(driver);
    }

    @Override
    public <T extends AnyDdapPage> T authorizeForResources(WebDriver driver, TestingPersona persona, String realmName, URI authorizeUri, Function<WebDriver, T> pageFactory) throws IOException {
        return pageFactory.apply(driver);
    }
}
