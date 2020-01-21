package com.dnastack.ddap.server;

import com.dnastack.ddap.common.AbstractBaseE2eTest;
import com.dnastack.ddap.common.TestingPersona;
import com.dnastack.ddap.common.util.DdapLoginUtil;
import dam.v1.DamService;
import io.restassured.response.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.cookie.Cookie;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dnastack.ddap.common.util.WebDriverCookieHelper.SESSION_COOKIE_NAME;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;


public class BeaconSearchApiTest extends AbstractBaseE2eTest {

    private static final String REALM = generateRealmName(BeaconSearchApiTest.class.getSimpleName());

    @Data
    public static class BeaconResponse {
        private BeaconInfo beaconInfo;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BeaconInfo {
        private String name;
        private String viewId;
        private String resourceId;
        private String resourceLabel;
        private String damId;
    }

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        final String damConfig = loadTemplate("/com/dnastack/ddap/adminConfig.json");
        validateProtoBuf(damConfig, DamService.DamConfig.newBuilder());
        setupRealmConfig(TestingPersona.ADMINISTRATOR, damConfig, "1", REALM);
    }

    @Test
    public void shouldGetTwoResultsForAggregateSearch() throws IOException {
        Cookie session = DdapLoginUtil.loginToDdap(DDAP_USERNAME, DDAP_PASSWORD);
        /* Run the aggregate search query on the realm */
        // @formatter:off
        final Response response = getRequestSpecification()
                .log().method()
                .log().uri()
            .cookie(SESSION_COOKIE_NAME, session.getValue())
                .when()
                .get("/api/v1alpha/realm/" + REALM + "/resources/search?type=beacon&assemblyId=GRCh37&referenceName=1&start=156105028&referenceBases=T&alternateBases=C");
        response
                .then()
                .log().everything()
                .contentType(JSON)
                .statusCode(200);
        // @formatter:on
        final BeaconResponse[] responses = response.as(BeaconResponse[].class);
        final Set<BeaconInfo> beaconInfoFromTestedDam = Arrays.stream(responses)
                                                              .map(BeaconResponse::getBeaconInfo)
                                                              .filter(info -> DAM_ID.equals(info.getDamId()))
                                                              .collect(Collectors.toSet());

        Assert.assertThat(beaconInfoFromTestedDam,
                          containsInAnyOrder(equalTo(new BeaconInfo("Beacon Discovery",
                                                                    "beacon",
                                                                    "ga4gh-apis",
                                                                    "GA4GH APIs",
                                                                    DAM_ID)),
                                            equalTo(new BeaconInfo("Beacon Discovery Access",
                                                                    "discovery-access",
                                                                    "thousand-genomes",
                                                                    "1000 Genomes (non-prod)",
                                                                    DAM_ID))));
    }

    @Test
    public void shouldGetOneResultForSingleResourceSearch() throws IOException {
        Cookie session = DdapLoginUtil.loginToDdap(DDAP_USERNAME, DDAP_PASSWORD);
        // @formatter:off
        getRequestSpecification()
            .log().method()
            .log().cookies()
            .log().uri()
            .cookie(SESSION_COOKIE_NAME, session.getValue())
        .when()
            .get(format(
                    // FIXME make DAM ID environment variable
                    "/api/v1alpha/realm/%s/resources/1/thousand-genomes/search" +
                            "?referenceName=13" +
                            "&start=32936732" +
                            "&referenceBases=G" +
                            "&alternateBases=C" +
                            "&type=beacon" +
                            "&assemblyId=GRCh37",
                    REALM))
        .then()
            .log().ifValidationFails()
            .contentType("application/json")
            .body("[0].beaconInfo.name", equalTo("Beacon Discovery Access"))
            .body("[0].beaconInfo.viewId", equalTo("discovery-access"))
            .body("[0].beaconInfo.resourceId", equalTo("thousand-genomes"))
            .body("[0].beaconInfo.resourceLabel", equalTo("1000 Genomes (non-prod)"))
            .body("[0].exists", anyOf(nullValue(), instanceOf(boolean.class)))
            .statusCode(200);
        // @formatter:on
    }

    @Test
    public void missingResourceUiLabel() throws IOException {
        Cookie session = DdapLoginUtil.loginToDdap(DDAP_USERNAME, DDAP_PASSWORD);
        String validPersonaToken = fetchRealPersonaDamToken(TestingPersona.USER_WITH_ACCESS, REALM);
        String refreshToken = fetchRealPersonaRefreshToken(TestingPersona.USER_WITH_ACCESS, REALM);

        // @formatter:off
        getRequestSpecification()
            .log().method()
            .log().cookies()
            .log().uri()
            .cookie(SESSION_COOKIE_NAME, session.getValue())
            .cookie("ic_identity", validPersonaToken)
            .cookie("ic_refresh", refreshToken)
        .when()
            .get(format(
                    // FIXME make DAM ID environment variable
                    "/api/v1alpha/realm/%s/resources/1/thousand-genomes/search" +
                            "?referenceName=13" +
                            "&start=32936732" +
                            "&referenceBases=G" +
                            "&alternateBases=C" +
                            "&type=beacon" +
                            "&assemblyId=GRCh37",
                    REALM))
        .then()
            .log().ifValidationFails()
            .contentType("application/json")
            .body("[0].beaconInfo.name", equalTo("Beacon Discovery Access"))
            .body("[0].beaconInfo.viewId", equalTo("discovery-access"))
            .body("[0].beaconInfo.resourceId", equalTo("thousand-genomes"))
            .body("[0].beaconInfo.resourceLabel", equalTo("1000 Genomes (non-prod)"))
            .body("[0].exists", anyOf(nullValue(), instanceOf(boolean.class)))
            .statusCode(200);
        // @formatter:on
    }

}
