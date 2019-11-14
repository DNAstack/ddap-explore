package com.dnastack.ddap.server;

import com.dnastack.ddap.common.AbstractBaseE2eTest;
import com.dnastack.ddap.common.TestingPersona;
import org.junit.BeforeClass;
import org.junit.Test;
import dam.v1.DamService;

import java.io.IOException;

import static java.lang.String.format;

public class WorkflowAccessApiTest extends AbstractBaseE2eTest {
    private static final String REALM = generateRealmName(WorkflowAccessApiTest.class.getSimpleName());

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        final String damConfig = loadTemplate("/com/dnastack/ddap/adminConfig.json");
        validateProtoBuf(damConfig, DamService.DamConfig.newBuilder());
        setupRealmConfig(TestingPersona.ADMINISTRATOR, damConfig, "1", REALM);
    }

    @Test
    public void getAccessTokenOfBucketAndRedirect() throws IOException {
        String damToken = fetchRealPersonaDamToken(TestingPersona.USER_WITH_ACCESS, REALM);
        String refreshToken = fetchRealPersonaDamToken(TestingPersona.USER_WITH_ACCESS, REALM);

        getRequestSpecification()
                .log().uri()
                .cookie("dam_token", damToken)
                .cookie("refresh_token", refreshToken)
                .when()
                .get(format("/api/v1alpha/%s/access/gcs/%s", REALM, TEST_BUCKET))
                .then()
                .log().ifValidationFails()
        .statusCode(308);
    }
}
