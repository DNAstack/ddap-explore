package com.dnastack.ddap.common.setup;

import com.dnastack.ddap.common.TestingPersona;
import com.dnastack.ddap.common.page.AnyDdapPage;
import org.apache.http.client.CookieStore;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

public interface LoginStrategy {

    CookieStore performPersonaLogin(String personaName, String realmName, String... scopes) throws IOException;
    <T extends AnyDdapPage> T performPersonaLogin(WebDriver driver, TestingPersona persona, String realmName, Function<WebDriver, T> pageFactory) throws IOException;
    <T extends AnyDdapPage> T authorizeForResources(WebDriver driver, TestingPersona persona, String realmName, URI authorizeUri, Function<WebDriver, T> pageFactory) throws IOException;

}
