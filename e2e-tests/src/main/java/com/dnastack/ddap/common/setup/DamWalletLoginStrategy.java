package com.dnastack.ddap.common.setup;

import static com.dnastack.ddap.common.AbstractBaseE2eTest.DDAP_BASE_URL;
import static com.dnastack.ddap.common.AbstractBaseE2eTest.DDAP_PASSWORD;
import static com.dnastack.ddap.common.AbstractBaseE2eTest.DDAP_USERNAME;
import static java.lang.String.format;
import static java.lang.String.join;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

import com.dnastack.ddap.common.PolicyRequirementFailedException;
import com.dnastack.ddap.common.TestingPersona;
import com.dnastack.ddap.common.page.AnyDdapPage;
import com.dnastack.ddap.common.util.DdapLoginUtil;
import com.dnastack.ddap.common.util.EnvUtil;
import com.dnastack.ddap.common.util.WebDriverCookieHelper;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.WebDriver;

@Slf4j
public class DamWalletLoginStrategy implements LoginStrategy {

    private static final Pattern STATE_PATTERN = Pattern.compile("\\s*var\\s+state\\s*=\\s*\'([^\']+)\'");
    private static final Pattern PATH_PATTERN = Pattern.compile("\\s*var\\s+path\\s*=\\s*\'([^\']+)\'");
    private static final String[] DEFAULT_SCOPES = new String[]{"openid", "ga4gh_passport_v1", "account_admin",
        "identities", "offline_access"};

    private Map<String, LoginInfo> personalAccessTokens;
    private WalletConfig walletConfig;
    private DamConfig damConfig;


    public DamWalletLoginStrategy() {

        walletConfig = EnvUtil.requiredEnvConfig("E2E_WALLET_CONFIG", WalletConfig.class);
        damConfig = EnvUtil.requiredEnvConfig("E2E_DAM_CONFIG", DamConfig.class);

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
        final LoginInfo loginInfo = personalAccessTokens.get(personaName);
        Cookie session = DdapLoginUtil
            .loginToDdap(damConfig.getDamBaseUrl(), damConfig.getDamUsername(), damConfig.getDamPassword())
            .orElse(null);
        final CookieStore cookieStore = WebDriverCookieHelper.setupCookieStore(session);
        final HttpClient httpclient = setupHttpClient(cookieStore);

        {
            final String scopeString = (scopes.length == 0) ? "" : "&scope=" + join("+", scopes);
            HttpGet request = new HttpGet(format("%s/api/v1alpha/realm/%s/identity/login?loginHint=wallet:%s%s", damConfig
                .getDamBaseUrl(), realmName, loginInfo
                .getEmail(), scopeString));
            log.info("Sending login request: {}", request);

            final HttpClientContext context = new HttpClientContext();
            final HttpResponse response = httpclient.execute(request, context);
            String responseBody = EntityUtils.toString(response.getEntity());
            context.getRedirectLocations()
                .forEach(uri -> log.info("Redirect uri {}", uri.toString()));
            assertThat("Response body: " + responseBody, response.getStatusLine().getStatusCode(), is(200));
        }

        final CsrfToken csrfToken = walletLogin(httpclient, loginInfo);

        acceptPermissions(httpclient, csrfToken);

        return cookieStore;
    }

    @Override
    public <T extends AnyDdapPage> T performPersonaLogin(WebDriver driver, TestingPersona persona, String realmName, Function<WebDriver, T> pageFactory) throws IOException {
        /*
         * Can't login to ddap-explore anymore (aside from sandbox credentials)
         * TODO get rid of this method -- move to somewhere with better name
         */
        // Need to navigate to site before setting cookie
        driver.get(URI.create(DDAP_BASE_URL).resolve(format("/%s", realmName)).toString());
        {
            // Need to add session cookie separately
            DdapLoginUtil.loginToDdap(DDAP_BASE_URL, DDAP_USERNAME, DDAP_PASSWORD)
                .ifPresent(cookie -> WebDriverCookieHelper.addBrowserCookie(driver, cookie));
        }
        // Visit again with session cookie
        driver.get(URI.create(DDAP_BASE_URL).resolve(format("/%s", realmName)).toString());

        return pageFactory.apply(driver);
    }

    @Override
    public <T extends AnyDdapPage> T authorizeForResources(WebDriver driver, TestingPersona persona, String realmName, URI authorizeUri, Function<WebDriver, T> pageFactory) throws IOException {
        final LoginInfo loginInfo = personalAccessTokens.get(persona.getId());
        Cookie session = DdapLoginUtil.loginToDdap(DDAP_BASE_URL, DDAP_USERNAME, DDAP_PASSWORD).orElse(null);
        final CookieStore cookieStore = WebDriverCookieHelper.setupCookieStore(session);
        final HttpClient httpclient = setupHttpClient(cookieStore);

        {
            String authorizeUriWithLoginHint = authorizeUri.toASCIIString();
            authorizeUriWithLoginHint = new StringBuilder(authorizeUriWithLoginHint)
                .insert(
                    authorizeUriWithLoginHint.indexOf("?") + 1, format("loginHint=wallet:%s&", loginInfo.getEmail()))
                .toString();

            HttpGet request = new HttpGet(URI.create(authorizeUriWithLoginHint));
            log.info("Sending login request for resource authorization: {}", request);

            HttpClientContext context = HttpClientContext.create();
            final HttpResponse response = httpclient.execute(request, context);
            context.getRedirectLocations()
                .forEach(uri -> log.info("Redirect uri {}", uri.toString()));

            String responseBody = EntityUtils.toString(response.getEntity());
            assertThat("Response body: " + responseBody, response.getStatusLine().getStatusCode(), is(200));
        }

        final CsrfToken csrfToken = walletLogin(httpclient, loginInfo);

        final URI finalLocation = acceptPermissions(httpclient, csrfToken);

        WebDriverCookieHelper.addCookiesFromStoreToSelenium(cookieStore, driver);
        driver.get(finalLocation.toString());

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

    private CsrfToken walletLogin(HttpClient httpClient, LoginInfo loginInfo) throws IOException {
        final HttpGet request = new HttpGet(format("%s/login/token?token=%s", walletConfig.getWalletUrl(), loginInfo
            .getPersonalAccessToken()));

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

            /*
             There is a form for consenting to sharing claims with DDAP that must be clicked. It contains a CSRF
             token in the page within a JavaScript function. This was a quick way to workaround the issue but it is brittle.
             We need to figure out a proper way for test users to log in non-interactively in the DAM.
             */
        final Matcher pathMatcher = PATH_PATTERN.matcher(responseBody);
        final Matcher stateMatcher = STATE_PATTERN.matcher(responseBody);

        assertTrue(responseBody, pathMatcher.find());
        assertTrue(responseBody, stateMatcher.find());

        return new CsrfToken(pathMatcher.group(1), stateMatcher.group(1));
    }

    private URI acceptPermissions(HttpClient httpClient, CsrfToken csrfToken) throws IOException {
        final HttpGet request = new HttpGet(format("%s%s?state=%s&agree=y", walletConfig.getPassportIssuer(), csrfToken
            .getPath(), csrfToken
            .getState()));

        HttpClientContext context = HttpClientContext.create();
        final HttpResponse response = httpClient.execute(request, context);

        URI finalLocation = null;
        List<URI> locations = context.getRedirectLocations();
        if (locations != null) {
            finalLocation = locations.get(locations.size() - 1);
            log.info("Final location after permission acknowledgment: {}", finalLocation);
        }

        String responseBody = EntityUtils.toString(response.getEntity());
        final String responseMessage =
            "Headers: " + Arrays.toString(response.getAllHeaders()) + "\nResponse body: " + responseBody;

        if (response.getStatusLine().getStatusCode() != 200) {
            if (responseMessage.contains("policy requirements failed")) {
                throw new PolicyRequirementFailedException(responseMessage);
            }
            log.info("Response: {}", responseBody);
            throw new IllegalStateException(responseMessage);
        }

        return finalLocation;
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
