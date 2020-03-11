package com.dnastack.ddap.common;

import com.dnastack.ddap.common.page.AnyDdapPage;
import com.dnastack.ddap.common.page.StandaloneLoginPage;
import org.apache.http.client.CookieStore;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

public class StandaloneModeLoginStrategy implements LoginStrategy {
    // FIXME Currently, the implementation is based on OAuth dance against Azure Active Directory directly. We would
    //       like to have it done against the wallet.
    private final URI baseUri;

    public StandaloneModeLoginStrategy(URI baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public CookieStore performPersonaLogin(String personaName, String realmName, String... scopes) throws IOException {
        return null;
    }

    @Override
    public <T extends AnyDdapPage> T performPersonaLogin(WebDriver driver, TestingPersona persona, String realmName, Function<WebDriver, T> pageFactory) throws IOException {
        // NOTE (1): In the standalone mode, the UI will enforce the authorization flow.
        // NOTE (2): In the standalone mode, persona and realmName are irreverent.
        driver.get(baseUri.resolve(String.format("/")).toString());
        StandaloneLoginPage page = new StandaloneLoginPage(driver);
        return page.logInAs("juti@dnastack.com", "Te9WAqGboV5bvYPpeQ2KOxJs", pageFactory);
    }

    @Override
    public <T extends AnyDdapPage> T authorizeForResources(WebDriver driver, TestingPersona persona, String realmName, URI authorizeUri, Function<WebDriver, T> pageFactory) throws IOException {
        return null;
    }

    @Override
    public boolean isDataAccessManagerRequired() {
        return false;
    }

    @Override
    public boolean isIdentityConcentratorRequired() {
        return false;
    }
}
