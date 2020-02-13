package com.dnastack.ddap.server;

import com.dnastack.ddap.common.AbstractBaseE2eTest;
import com.dnastack.ddap.common.TestingPersona;
import com.dnastack.ddap.common.util.DdapLoginUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.Data;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static com.dnastack.ddap.common.util.WebDriverCookieHelper.SESSION_COOKIE_NAME;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ConfigE2eTest extends AbstractBaseE2eTest {

    private static final String REALM = generateRealmName(ConfigE2eTest.class.getSimpleName());

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        final String damConfig = loadTemplate("/com/dnastack/ddap/adminConfig.json");
        setupRealmConfig(TestingPersona.ADMINISTRATOR, damConfig, "1", REALM);
    }

    @Test
    public void doNotAcceptDevCookieEncryptorCredentials() {
        Assume.assumeFalse("Dev cookie encryptor credentials are allowed on localhost", RestAssured.baseURI.startsWith("http://localhost:"));
        Assume.assumeFalse("Dev cookie encryptor credentials are allowed on localhost", RestAssured.baseURI.startsWith("http://host.docker.internal:"));
        assertThat("Default dev credentials for cookie encryptor are allowed only on localhost",
            DDAP_COOKIES_ENCRYPTOR_PASSWORD, not(equalTo("abcdefghijk"))
            // It is enough to test value of 'E2E_COOKIES_ENCRYPTOR_PASSWORD' env,
            // since the password must be identical to 'DDAP_COOKIES_ENCRYPTOR_PASSWORD' of deployed app.
            // In case of mismatch there will be failures all over the e2e suite.
        );
        assertThat("Default dev credentials for cookie encryptor are allowed only on localhost",
            DDAP_COOKIES_ENCRYPTOR_SALT, not(equalTo("598953e322"))
        );
    }

    @Test
    public void doNotAcceptDevCredentials() {
        Assume.assumeTrue(DDAP_PASSWORD != null);
        Assume.assumeFalse("Dev credentials are allowed on localhost", RestAssured.baseURI.startsWith("http://localhost:"));
        Assume.assumeFalse("Dev credentials are allowed on localhost", RestAssured.baseURI.startsWith("http://host.docker.internal:"));
        given()
            .log().method()
            .log().uri()
        .when()
            .get("/index.html")
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("html.head.title", containsString("Please sign in"));
    }

    @Test(expected = SignatureException.class)
    public void doNotUseDevSigningKeyForOAuthState() throws IOException {
        Assume.assumeTrue(Instant.now().isAfter(Instant.ofEpochSecond(1581125077))); // Feb 7, 2020
        Assume.assumeFalse("Dev keys are allowed on localhost", RestAssured.baseURI.startsWith("http://localhost:"));
        Assume.assumeFalse("Dev keys are allowed on localhost", RestAssured.baseURI.startsWith("http://host.docker.internal:"));
        Cookie session = DdapLoginUtil.loginToDdap(DDAP_BASE_URL, DDAP_USERNAME, DDAP_PASSWORD);

        final Response response = given()
                .log().method()
                .log().uri()
                .redirects().follow(false)
            .cookie(SESSION_COOKIE_NAME, session.getValue())
                .when()
                .get(String.format("/api/v1alpha/realm/%s/resources/authorize?resource=1;thousand-genomes/views/discovery-access/roles/discovery", REALM));
        response
                .then()
                .log().ifValidationFails()
                .statusCode(allOf(greaterThanOrEqualTo(300), lessThan(400)))
                .header("Location", startsWith("https://"));

        final URI location = URI.create(response.getHeader("Location"));
        final List<NameValuePair> queryPairs = URLEncodedUtils.parse(location, StandardCharsets.UTF_8);
        final String stateJwt = queryPairs.stream()
                                          .filter(pair -> pair.getName().equals("state"))
                                          .map(NameValuePair::getValue)
                                          .findFirst()
                                          .orElseThrow(() -> new AssertionError(
                                                  "No state parameter in login redirect URL."));

        final String base64EncodedDevSigningKey = "VGhlcmUgb25jZSB3YXMgYSBsYW5ndWFnZSBjYWxsZWQgYmFzaApJdCdzIHNlbWFudGljcyB3ZXJlIG9mdGVuIHF1aXRlIHJhc2gKQnV0IGl0IHdvcmtlZCwgbW9yZSBvciBsZXNzCkV2ZW4gdGhvdWdoIGl0J3MgYSBtZXNzClNvIEkgZ3Vlc3MgaXQgc3RheXMgb3V0IG9mIHRoZSB0cmFzaAo=";
        Jwts.parser()
            .setSigningKey(base64EncodedDevSigningKey)
            .parseClaimsJws(stateJwt);
    }

    @Test
    public void requireSomeCredentials() {
        given()
                .log().method()
                .log().uri()
        .when()
                .get("/index.html")
        .then()
                .log().ifValidationFails()
                .statusCode(200)
        .body("html.head.title", containsString("Please sign in"));
    }

    @Test
    public void accessRootWithoutCredentials() {
        given()
                .log().method()
                .log().uri()
        .when()
                .get("/")
        .then()
                .log().ifValidationFails()
                .statusCode(200);
    }

    @Test
    public void accessAngularIndexPage() throws IOException {
        Cookie session = DdapLoginUtil.loginToDdap(DDAP_BASE_URL, DDAP_USERNAME, DDAP_PASSWORD);

        given()
                .log().method()
                .log().uri()
            .cookie(SESSION_COOKIE_NAME, session.getValue())
        .when()
                .get("/index.html")
        .then()
                .log().ifValidationFails()
                .statusCode(200);
    }

    @Test
    public void accessDamEndpoint() throws IOException {
        Cookie session = DdapLoginUtil.loginToDdap(DDAP_BASE_URL, DDAP_USERNAME, DDAP_PASSWORD);

        given()
                .log().method()
                .log().uri()
            .cookie(SESSION_COOKIE_NAME, session.getValue())
        .when()
                .get("/dam/1/v1alpha/dnastack/resources")
        .then()
                .log().ifValidationFails()
                .contentType("application/json")
                .statusCode(200);
    }

    @Test
    public void serveAngularRoutes() {
        given()
                .log().method()
                .log().uri()
        .when()
                .get("/resources")
        .then()
                .log().ifValidationFails()
                .statusCode(200);
    }

    @Test
    public void angularRoutesDoNotWorkForJavaScriptFiles() throws IOException {
        Cookie session = DdapLoginUtil.loginToDdap(DDAP_BASE_URL, DDAP_USERNAME, DDAP_PASSWORD);

        given()
                .log().method()
                .log().uri()
            .cookie(SESSION_COOKIE_NAME, session.getValue())
        .when()
                .get("/made-up-resource-name.js")
        .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    @Test
    public void noAngularRoutesForMapFiles() throws IOException {
        Cookie session = DdapLoginUtil.loginToDdap(DDAP_BASE_URL, DDAP_USERNAME, DDAP_PASSWORD);

        given()
                .log().method()
                .log().uri()
            .cookie(SESSION_COOKIE_NAME, session.getValue())
        .when()
                .get("/made-up-resource-name.js.map")
        .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    @Test
    public void noAngularRoutesForHtmlFiles() throws IOException {
        Cookie session = DdapLoginUtil.loginToDdap(DDAP_BASE_URL, DDAP_USERNAME, DDAP_PASSWORD);

        given()
                .log().method()
                .log().uri()
            .cookie(SESSION_COOKIE_NAME, session.getValue())
        .when()
                .get("/made-up-resource-name.html")
        .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    @Test
    public void noAngularRoutesForFileWithArbitraryExtension() throws IOException {
        Cookie session = DdapLoginUtil.loginToDdap(DDAP_BASE_URL, DDAP_USERNAME, DDAP_PASSWORD);

        given()
                .log().method()
                .log().uri()
            .cookie(SESSION_COOKIE_NAME, session.getValue())
        .when()
                .get("/made-up-resource-name.foobar")
        .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    @Data
    static class CliLoginResponse {
        private String token;
    }
}
