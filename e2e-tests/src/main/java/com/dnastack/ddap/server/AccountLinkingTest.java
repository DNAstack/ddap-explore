package com.dnastack.ddap.server;

import com.dnastack.ddap.common.AbstractBaseE2eTest;
import com.dnastack.ddap.common.TestingPersona;
import com.dnastack.ddap.common.util.JwtTestUtil;
import dam.v1.DamService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("Duplicates")
@Slf4j
public class AccountLinkingTest extends AbstractBaseE2eTest {

    private static final String REALM = generateRealmName(AccountLinkingTest.class.getSimpleName());

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        final String damConfig = loadTemplate("/com/dnastack/ddap/accountLinkingTestRealmConfig.json");
        validateProtoBuf(damConfig, DamService.DamConfig.newBuilder());
        setupRealmConfig(TestingPersona.ADMINISTRATOR, damConfig, "1", REALM);
    }

    @Test
    public void personaLoginShouldHonourRequestedScopes() throws Exception {
        String requestedScope = "openid link:ic_" + System.currentTimeMillis();

        // @formatter:off
        String icTokenJwt = given()
            .log().method()
            .log().cookies()
            .log().uri()
            .auth().basic(DDAP_USERNAME, DDAP_PASSWORD)
            .redirects().follow(false)
        .when()
            .get(ddap("/identity/login?persona=nci_researcher&scope=" + requestedScope))
        .then()
            .log().body()
            .log().ifValidationFails()
            .statusCode(307)
            .cookie("ic_token")
        .extract()
            .cookie("ic_token");
        // @formatter:on

        Map<String, Object> icToken = JwtTestUtil.getBody(icTokenJwt);
        assertThat(icToken, hasEntry("scope", requestedScope));
    }

    @Test
    public void redirectToIcLoginPageShouldPassRequestedScopes() throws Exception {
        String requestedScope = "link:ic_" + System.currentTimeMillis();

        // @formatter:off
        given()
            .log().method()
            .log().cookies()
            .log().uri()
            .auth().basic(DDAP_USERNAME, DDAP_PASSWORD)
            .redirects().follow(false)
        .when()
            .get(ddap("/identity/login?scope=" + requestedScope))
        .then()
            .log().body()
            .log().ifValidationFails()
            .statusCode(307)
            .header("Location", containsString("&scope=" + requestedScope));
        // @formatter:on
    }

    @Test
    public void linkIcLoginExternalAccountShouldPassLinkScopeForAccountInIcToken() throws Exception {
        String icTokenJwt = fetchRealPersonaIcToken(TestingPersona.NCI_RESEARCHER, REALM);
        String refreshTokenJwt = fetchRealPersonaRefreshToken(TestingPersona.NCI_RESEARCHER, REALM);
        String baseAccountId = JwtTestUtil.getSubject(icTokenJwt);
        String requestedScope = "link:" + baseAccountId;

        // @formatter:off
        given()
            .log().method()
            .log().cookies()
            .log().uri()
            .auth().basic(DDAP_USERNAME, DDAP_PASSWORD)
            .cookie("ic_token", icTokenJwt)
            .cookie("refresh_token", refreshTokenJwt)
            .redirects().follow(false)
        .when()
            .get(ddap("/identity/link?provider=google"))
        .then()
            .log().body()
            .log().ifValidationFails()
            .statusCode(307)
            .header("Location", containsString(requestedScope));
        // @formatter:on
    }


    @Test
    public void linkAndUnlinkAccount() throws Exception {
        String icTokenJwtBeforeLinking = fetchRealPersonaIcToken("mr_hyde", REALM, "openid", "identities", "link");
        String refreshTokenJwt = fetchRealPersonaRefreshToken(TestingPersona.NCI_RESEARCHER, REALM);
        String baseAccountId = JwtTestUtil.getSubject(icTokenJwtBeforeLinking);

        // Link account
        // @formatter:off
        given()
            .log().method()
            .log().cookies()
            .log().uri()
            .auth().basic(DDAP_USERNAME, DDAP_PASSWORD)
            .cookie("ic_token", icTokenJwtBeforeLinking)
                .cookie("refresh_token", refreshTokenJwt)
            .redirects().follow(false)
        .when()
            .get(ddap("/identity/link?provider=dr_jekyll&type=persona"))
        .then()
            .log().body()
            .log().ifValidationFails()
            .statusCode(307)
                .header("Set-Cookie", startsWith("dam_token"))
            .header("Location", endsWith("/" + REALM + "/identity"));
        // @formatter:on

        // check that we can query my account, and that accounts were linked
        // @formatter:off
        List<IcConnectedAccount> connectedAccounts = given()
            .log().method()
            .log().cookies()
            .log().uri()
            .auth().basic(DDAP_USERNAME, DDAP_PASSWORD)
            .cookie("ic_token", icTokenJwtBeforeLinking)
        .when()
            .get(icViaDdap("/accounts/-"))
        .then()
            .log().body()
            .log().ifValidationFails()
            .statusCode(200)
        .extract()
            .jsonPath().getList("account.connectedAccounts", IcConnectedAccount.class);
        // @formatter:on

        assertAccountsContainSubject(connectedAccounts, "mr_hyde@era.nih.gov");
        assertAccountsContainSubject(connectedAccounts, "dr_jekyll@faculty.uni-heidelberg.de");

        // Unlink account
        String icTokenAfterLinking = fetchRealPersonaIcToken("mr_hyde", REALM);
        // @formatter:off
        given().log().method()
               .log().cookies()
               .log().uri()
               .auth().basic(DDAP_USERNAME, DDAP_PASSWORD)
               .cookie("ic_token", icTokenAfterLinking)
                .cookie("refresh_token", refreshTokenJwt)
               .redirects().follow(false)
               .when()
               .delete(ddap("/identity/link/mr_hyde@era.nih.gov"))
               .then()
               .log().body()
               .log().ifValidationFails()
               .statusCode(200)
                .header("Set-Cookie", startsWith("dam_token"));
        // @formatter:on

        // check that we can query my account, and that accounts are no-longer linked
        // @formatter:off
        connectedAccounts = given()
                .log().method()
                .log().cookies()
                .log().uri()
                .auth().basic(DDAP_USERNAME, DDAP_PASSWORD)
                .cookie("ic_token", icTokenAfterLinking)
                .when()
                .get(icViaDdap("/accounts/-"))
                .then()
                .log().body()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .jsonPath().getList("account.connectedAccounts", IcConnectedAccount.class);
        // @formatter:on

        assertAccountsContainSubject(connectedAccounts, "dr_jekyll@faculty.uni-heidelberg.de");
        assertAccountsDoesNotContainSubject(connectedAccounts, "mr_hyde@era.nih.gov");
    }

    private void assertAccountsDoesNotContainSubject(List<IcConnectedAccount> connectedAccounts, String subject) {
        assertThat("Connected accounts missing " + subject + ": " + connectedAccounts,
                   connectedAccounts.stream().anyMatch(account -> account.getProperties().getSubject().equals(subject)),
                   is(false));
    }

    private void assertAccountsContainSubject(List<IcConnectedAccount> connectedAccounts, String subject) {
        assertThat("Connected accounts missing " + subject + ": " + connectedAccounts,
                connectedAccounts.stream().anyMatch(account -> account.getProperties().getSubject().equals(subject)),
                is(true));
    }

    private String icViaDdap(String path) {
        return format("/identity/v1alpha/%s%s", REALM, path);
    }

    private String ddap(String path) {
        return format("/api/v1alpha/%s%s", REALM, path);
    }

    @Data
    static class IcConnectedAccount {
        IcConnectecAccountProperties properties;
    }

    @Data
    static class IcConnectecAccountProperties {
        String subject;
    }
}
