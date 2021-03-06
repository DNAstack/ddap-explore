package com.dnastack.ddap.server;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import com.dnastack.ddap.common.AbstractBaseE2eTest;
import com.dnastack.ddap.common.setup.ConfigModel;
import com.dnastack.ddap.common.util.EnvUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Data;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigE2eTest extends AbstractBaseE2eTest {

    private static ConfigTestConfig testConfig;

    @BeforeClass
    public static void setup() {
        testConfig = EnvUtil
            .optionalEnvConfig("E2E_TEST_CONFIG_CONFIG", new ConfigTestConfig(), ConfigTestConfig.class);
        Assume.assumeTrue(testConfig.isEnabled());
    }

    @Test
    public void doNotAcceptDevCookieEncryptorCredentials() {

        Assume.assumeFalse("Dev cookie encryptor credentials are allowed on localhost", RestAssured.baseURI
            .startsWith("http://localhost:"));
        Assume.assumeFalse("Dev cookie encryptor credentials are allowed on localhost", RestAssured.baseURI
            .startsWith("http://host.docker.internal:"));
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
        Assume.assumeTrue(DDAP_PASSWORD != null || DDAP_USERNAME != null);
        Assume.assumeFalse("Dev credentials are allowed on localhost", RestAssured.baseURI
            .startsWith("http://localhost:"));
        Assume.assumeFalse("Dev credentials are allowed on localhost", RestAssured.baseURI
            .startsWith("http://host.docker.internal:"));
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
        // FIXME: DISCO-2698
        Assume.assumeTrue(ZonedDateTime.now().isAfter(ZonedDateTime.of(
            2020, 2, 29, 12, 0, 0, 0,
            ZoneId.of("America/Toronto"))
        ));
        Assume.assumeFalse("Dev keys are allowed on localhost", RestAssured.baseURI.startsWith("http://localhost:"));
        Assume.assumeFalse("Dev keys are allowed on localhost", RestAssured.baseURI
            .startsWith("http://host.docker.internal:"));
        final Response response = getRequestSpecWithBasicAuthIfNeeded()
            .redirects().follow(false)
            .when()
            .get(String
                .format("/api/v1alpha/realm/%s/resources/authorize?resource=1;thousand-genomes/views/discovery-access/roles/discovery", REALM));
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
        Assume.assumeTrue(DDAP_PASSWORD != null);
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

        getRequestSpecWithBasicAuthIfNeeded()
            .when()
            .get("/index.html")
            .then()
            .log().ifValidationFails()
            .statusCode(200);
    }

    @Test
    public void accessDamEndpoint() throws IOException {
        Assume.assumeTrue("This deployment does not use a DAM, and testing its features has been disabled", testConfig
            .isDamEnabled());
        getRequestSpecWithBasicAuthIfNeeded()
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

        getRequestSpecWithBasicAuthIfNeeded()
            .when()
            .get("/made-up-resource-name.js")
            .then()
            .log().ifValidationFails()
            .statusCode(404);
    }

    @Test
    public void noAngularRoutesForMapFiles() throws IOException {

        getRequestSpecWithBasicAuthIfNeeded()
            .when()
            .get("/made-up-resource-name.js.map")
            .then()
            .log().ifValidationFails()
            .statusCode(404);
    }

    @Test
    public void noAngularRoutesForHtmlFiles() throws IOException {

        getRequestSpecWithBasicAuthIfNeeded()
            .when()
            .get("/made-up-resource-name.html")
            .then()
            .log().ifValidationFails()
            .statusCode(404);
    }

    @Test
    public void noAngularRoutesForFileWithArbitraryExtension() throws IOException {

        getRequestSpecWithBasicAuthIfNeeded()
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

    @Data
    static class ConfigTestConfig implements ConfigModel {

        private boolean enabled = true;
        private boolean damEnabled = true;

        @Override
        public void validateConfig() {

        }
    }
}
