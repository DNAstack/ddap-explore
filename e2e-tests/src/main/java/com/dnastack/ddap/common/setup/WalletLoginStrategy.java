package com.dnastack.ddap.common.setup;

import static com.dnastack.ddap.common.AbstractBaseE2eTest.DDAP_BASE_URL;
import static com.dnastack.ddap.common.AbstractBaseE2eTest.DDAP_PASSWORD;
import static com.dnastack.ddap.common.AbstractBaseE2eTest.DDAP_USERNAME;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.dnastack.ddap.common.TestingPersona;
import com.dnastack.ddap.common.page.AnyDdapPage;
import com.dnastack.ddap.common.util.DdapLoginUtil;
import com.dnastack.ddap.common.util.EnvUtil;
import com.dnastack.ddap.common.util.WebDriverCookieHelper;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.WebDriver;

@Slf4j
public class WalletLoginStrategy implements LoginStrategy {

    private Map<String, LoginInfo> personalAccessTokens;
    private WalletConfig walletConfig;

    public WalletLoginStrategy() {

        walletConfig = EnvUtil.requiredEnvConfig("E2E_WALLET_CONFIG", WalletConfig.class);

        personalAccessTokens = new HashMap<>();
        personalAccessTokens
            .put(TestingPersona.ADMINISTRATOR.getId(), new LoginInfo(walletConfig.getAdminUserEmail(), walletConfig
                .getAdminUserToken()));
        personalAccessTokens.put(TestingPersona.USER_WITH_ACCESS.getId(), new LoginInfo(walletConfig
            .getWhitelistUserEmail(), walletConfig.getWhitelistUserToken()));
        personalAccessTokens.put(TestingPersona.USER_WITHOUT_ACCESS.getId(), new LoginInfo(walletConfig
            .getPlainUserEmail(), walletConfig.getPlainUserToken()));
    }

    @Override
    public CookieStore performPersonaLogin(String personaName, String realmName, String... scopes) throws IOException {
        throw new UnsupportedOperationException("Persona login is not supported using this login strategy");
    }

    @Override
    public <T extends AnyDdapPage> T performPersonaLogin(WebDriver driver, TestingPersona persona, String realmName, Function<WebDriver, T> pageFactory) throws IOException {

        driver.get(DDAP_BASE_URL);
        {

            doWait(2_000L);
            String currentPage = driver.getCurrentUrl();
            if (currentPage.startsWith(DDAP_BASE_URL) && currentPage.endsWith("/login")) {
                WebDriverCookieHelper.addCookiesFromStoreToSelenium(DdapLoginUtil.loginToDdap(DDAP_BASE_URL, DDAP_USERNAME, DDAP_PASSWORD),driver);
                driver.get(DDAP_BASE_URL);
                doWait(2_000L);
            }

            if (driver.getCurrentUrl().startsWith(walletConfig.getWalletUrl())) {
                final CookieStore cookieStore = DdapLoginUtil.loginToDdap(DDAP_BASE_URL, DDAP_USERNAME, DDAP_PASSWORD);
                final HttpClient httpclient = setupHttpClient(cookieStore);
                walletLogin(httpclient, personalAccessTokens.get(persona.getId()));
                WebDriverCookieHelper.addCookiesFromStoreToSelenium(cookieStore, driver);
                doWait(2_000L);
            }

        }
        driver.get(DDAP_BASE_URL);
        return pageFactory.apply(driver);
    }


    @Override
    public <T extends AnyDdapPage> T authorizeForResources(WebDriver driver, TestingPersona persona, String realmName, URI authorizeUri, Function<WebDriver, T> pageFactory) throws IOException {
        final CookieStore cookieStore =DdapLoginUtil.loginToDdap(DDAP_BASE_URL, DDAP_USERNAME, DDAP_PASSWORD);
        final HttpClient httpclient = setupHttpClient(cookieStore);

        {
            driver.get(authorizeUri.toString());
            doWait(5_000L);
            if (driver.getCurrentUrl().startsWith(walletConfig.getWalletUrl())) {
                walletLogin(httpclient,personalAccessTokens.get(persona));
                WebDriverCookieHelper.addCookiesFromStoreToSelenium(cookieStore, driver);
            }
        }

        return pageFactory.apply(driver);
    }


    private HttpClient setupHttpClient(CookieStore cookieStore) {
        return HttpClientBuilder.create()
            .setDefaultCookieStore(cookieStore)
            .setDefaultRequestConfig(RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .build())
            .build();
    }

    private void walletLogin(HttpClient httpClient, LoginInfo loginInfo) throws IOException {
        final HttpGet request = new HttpGet(format("%s/login/token?token=%s&email=%s", walletConfig.getWalletUrl(), loginInfo
            .getPersonalAccessToken(), loginInfo.getEmail()));

        final HttpClientContext context = new HttpClientContext();
        final HttpResponse response = httpClient.execute(request, context);
        String responseBody = EntityUtils.toString(response.getEntity());
        final String responseMessage = format("Redirects:\n%s\n\nHeaders: %s\nResponse body: %s",
            context.getRedirectLocations()
                .stream()
                .map(uri -> "\t" + uri)
                .collect(Collectors.joining("\n")),
            Arrays.toString(response.getAllHeaders()),
            responseBody);
        assertThat(responseMessage, response.getStatusLine().getStatusCode(), is(200));
    }



    private void doWait(Long timeoutms) {
        try {
            Thread.sleep(timeoutms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class CsrfToken {

        private String path;
        private String state;
    }

    @Value
    static class LoginInfo {

        String email;
        String personalAccessToken;
    }

}
