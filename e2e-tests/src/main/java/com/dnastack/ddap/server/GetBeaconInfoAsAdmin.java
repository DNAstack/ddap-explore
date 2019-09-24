package com.dnastack.ddap.server;

import com.dnastack.ddap.common.AbstractBaseE2eTest;
import com.dnastack.ddap.common.TestingPersona;
import com.fasterxml.jackson.databind.DeserializationFeature;
import dam.v1.DamService;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;


public class GetBeaconInfoAsAdmin extends AbstractBaseE2eTest {

    private static final String REALM = generateRealmName(GetBeaconInfoAsAdmin.class.getSimpleName());

    @Before
    public void setupRealm() throws IOException {
        String realmConfigString = loadTemplate("/com/dnastack/ddap/adminConfig.json");

        DamService.DamConfig.Builder damConfigBuilder = DamService.DamConfig.newBuilder();
        validateProtoBuf(realmConfigString, damConfigBuilder);

        setupRealmConfig(TestingPersona.ADMINISTRATOR, realmConfigString, "1", REALM);
        RestAssured.config = RestAssuredConfig.config()
                                              .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                                                      (cls, charset) -> new com.fasterxml.jackson.databind.ObjectMapper()
                                                              .findAndRegisterModules()
                                                              .configure(
                                                                      DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                                                      false)));
    }

    /**
     * Test to exercise what happens to Beacon Org and name we get when we make request as a user that doesn't have
     * resource access rights. In this case, we're making request as Administrator and ensure that we still get
     * beaconInfo name and resourceId in the response.
     * @throws IOException
     */
    @Test
    public void exerciseMissingBeaconInfo() throws IOException {
        String validPersonaToken = fetchRealPersonaDamToken(TestingPersona.ADMINISTRATOR, REALM);
        String refreshToken = fetchRealPersonaRefreshToken(TestingPersona.ADMINISTRATOR, REALM);

        // @formatter:off
        final Response response = given()
                .log().method()
                .log().uri()
                .when()
                .auth().basic(DDAP_USERNAME, DDAP_PASSWORD)
                .cookie("dam_token", validPersonaToken)
                .cookie("refresh_token", refreshToken)
                .get("/api/v1alpha/" + REALM + "/resources/search?type=beacon&assemblyId=GRCh37&referenceName=1&start=156105028&referenceBases=T&alternateBases=C");
        response
                    .then()
                    .log().everything()
                    .contentType(JSON)
                    .statusCode(200);
        // @formatter:on
        final BeaconQueryResult[] allResults = response.as(BeaconQueryResult[].class);
        final List<BeaconInfo> results = Arrays.stream(allResults)
                                               .map(BeaconQueryResult::getBeaconInfo)
                                               .filter(info -> DAM_ID.equals(info.getDamId()))
                                               .collect(Collectors.toList());

        Assert.assertThat(results,
                          containsInAnyOrder(equalTo(new BeaconInfo("Beacon Discovery", "GA4GH APIs", DAM_ID)),
                                             equalTo(new BeaconInfo("Beacon Discovery Access", "1000 Genomes (non-prod)", DAM_ID))));
    }

    @Data
    static class BeaconQueryResult {
        private BeaconInfo beaconInfo;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class BeaconInfo {
        private String name;
        private String resourceLabel;
        private String damId;
    }

}
