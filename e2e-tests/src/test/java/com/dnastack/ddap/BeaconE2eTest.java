package com.dnastack.ddap;

import org.junit.Test;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class BeaconE2eTest extends BaseE2eTest {

    @Test
    public void querySingleBeacon() throws IOException {
        // TODO [DISCO-2022] this test should create its own realm and populate it with the needed personas and beacons!
        String validPersonaToken = fetchRealPersonaDamToken("nci_researcher");

        // @formatter:off
        given()
            .log().method()
            .log().cookies()
            .log().uri()
            .auth().basic(DDAP_USERNAME, DDAP_PASSWORD)
            .cookie("dam_token", validPersonaToken)
        .when()
            .get("/api/resources/ga4gh-apis/search?referenceName=13&start=32936732&referenceBases=G&alternateBases=C&type=beacon&assemblyId=GRCh37")
        .then()
            .log().everything()
            .contentType("application/json")
            .body("[0].name", not(isEmptyOrNullString()))
            .body("[0].organization", not(isEmptyOrNullString()))
            .body("[0].exists", anyOf(nullValue(), instanceOf(boolean.class)))
            .statusCode(200);
        // @formatter:on
    }
}