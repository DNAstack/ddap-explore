package com.dnastack.ddap.server;

import com.dnastack.ddap.common.AbstractBaseE2eTest;
import com.dnastack.ddap.common.TestingPersona;
import dam.v1.DamService;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static java.lang.String.format;

public class ViewsApiTest extends AbstractBaseE2eTest {


    private static final String REALM = generateRealmName(ViewsApiTest.class.getSimpleName());

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        final String damConfig = loadTemplate("/com/dnastack/ddap/adminConfig.json");
        validateProtoBuf(damConfig, DamService.DamConfig.newBuilder());
        setupRealmConfig(TestingPersona.ADMINISTRATOR, damConfig, "1", REALM);
    }

    @Test
    public void shouldReturnViewForBucket() throws IOException {
        // @formatter:off
        getRequestSpecification()
            .log().method()
            .log().cookies()
            .log().uri()
            .contentType("application/json")
            .body(Arrays.asList("gs://ga4gh-apis-controlled-access","https://www.googleapis.com/storage/v1/b/ga4gh-apis-controlled-access"))
        .when()
            .post(format("/api/v1alpha/realm/%s/views/lookup",REALM))
            .then()
            .log().ifValidationFails()
            .contentType("application/json")
            .body("gs://ga4gh-apis-controlled-access", Matchers.hasItem("1;thousand-genomes/views/gcs-file-access/roles/viewer"))
            .statusCode(200);
        // @formatter:on

    }

    @Test
    public void shouldNotReturnViewForPartialSubset() throws IOException {
        // @formatter:off
        getRequestSpecification()
            .log().method()
            .log().cookies()
            .log().uri()
            .contentType("application/json")
            .body(Arrays.asList("gs://ga4gh-apis-controlled-access-with-more-stuff"))
        .when()
            .post(format("/api/v1alpha/realm/%s/views/lookup",REALM))
            .then()
            .log().ifValidationFails()
            .contentType("application/json")
            .body("isEmpty()",Matchers.is(true))
            .statusCode(200);
        // @formatter:on

    }

    @Test
    public void shouldReturnEmptyViewsForNonExistantResource() throws IOException {
        // @formatter:off
        getRequestSpecification()
            .log().method()
            .log().cookies()
            .log().uri()
            .contentType("application/json")
            .body(Arrays.asList("gs://empty-view"))
        .when()
            .post(format("/api/v1alpha/realm/%s/views/lookup",REALM))
            .then()
            .log().ifValidationFails()
            .body("isEmpty()",Matchers.is(true))
            .contentType("application/json")
            .statusCode(200);
        // @formatter:on

    }

}
