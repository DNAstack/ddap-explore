package com.dnastack.ddap.server;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import com.dnastack.ddap.common.AbstractBaseE2eTest;
import java.io.IOException;
import org.junit.Test;

public class DatasetApiTest extends AbstractBaseE2eTest {

    private static final String DATASET_URL_WITH_INLINE_SCHEMA = "https://storage.googleapis"
        + ".com/ddap-e2etest-public-objects/table/subjects/data";
    private static final String DATASET_URL_WITH_RESOLVED_SCHEMA = "https://storage.googleapis"
        + ".com/ddap-e2etest-public-objects/table/subjects-referenced-schema/data";


    @Test
    public void shouldGetSingleDatasetFromFetch() throws IOException {

        getRequestSpecWithBasicAuthIfNeeded()
            .log().cookies()
            .queryParam("dataset_url",DATASET_URL_WITH_INLINE_SCHEMA)
        .when()
            .get(format("/api/v1alpha/realm/%s/table",REALM))
            .then()
            .log().ifValidationFails()
            .contentType("application/json")
            .body("data.size()",greaterThanOrEqualTo(1))
            .body("data_model.$id",equalTo("ca.personalgenomes.schema.Subject"))
            .statusCode(200);
    }

    @Test
    public void shouldGetSingleDatasetFromFetchAndResolveRemoteSchema() throws IOException {

        getRequestSpecWithBasicAuthIfNeeded()
            .log().cookies()
            .queryParam("dataset_url",DATASET_URL_WITH_RESOLVED_SCHEMA)
        .when()
            .get(format("/api/v1alpha/realm/%s/table",REALM))
            .then()
            .log().ifValidationFails()
            .contentType("application/json")
            .body("data.size()",greaterThanOrEqualTo(1))
            .body("data_model.$id",equalTo("ca.personalgenomes.schema.Subject"))
            .body("data_model.properties.blood_type.$id",equalTo("ca.personalgenomes.schemas.BloodType"))
            .body("data_model.properties.sex.$id",equalTo("ca.personalgenomes.schemas.Sex"))
            .statusCode(200);
    }

    @Test
    public void shouldGetErrorMessageFromNonexistantDataset() throws IOException {

        getRequestSpecWithBasicAuthIfNeeded()
            .log().cookies()
            .queryParam("dataset_url","https://storage.googleapis.com/ga4gh-dataset-sample/table/non-existant/data")
        .when()
            .get(format("/api/v1alpha/realm/%s/table",REALM))
            .then()
            .log().ifValidationFails()
            .contentType("application/json")
            .body("message",notNullValue())
            .body("statusCode",equalTo(404))
            .statusCode(404);
    }

}
