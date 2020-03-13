package com.dnastack.ddap.common;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

import com.dnastack.ddap.common.setup.ConfigStrategy;
import com.dnastack.ddap.common.setup.LoginStrategy;
import com.dnastack.ddap.common.setup.StrategyFactory;
import com.dnastack.ddap.common.util.EnvUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;

@SuppressWarnings("Duplicates")
@Slf4j
public abstract class AbstractBaseE2eTest {

    public static final String DDAP_BASE_URL = EnvUtil.requiredEnv("E2E_BASE_URI");
    public static final String REALM = EnvUtil.requiredEnv("E2E_TEST_REALM");
    public static final String DDAP_USERNAME = EnvUtil.optionalEnv("E2E_BASIC_USERNAME", null);
    public static final String DDAP_PASSWORD = EnvUtil.optionalEnv("E2E_BASIC_PASSWORD", null);
    public static final String DDAP_COOKIES_ENCRYPTOR_PASSWORD = EnvUtil
        .optionalEnv("E2E_COOKIES_ENCRYPTOR_PASSWORD", "abcdefghijk");
    public static final String DDAP_COOKIES_ENCRYPTOR_SALT = EnvUtil
        .optionalEnv("E2E_COOKIES_ENCRYPTOR_SALT", "598953e322");
    public static final String DDAP_E2E_TEST_MODE = EnvUtil.optionalEnv("E2E_TEST_MODE", "normal");

    protected static LoginStrategy loginStrategy;
    protected static ConfigStrategy configStrategy;


    @BeforeClass
    public static void staticSetup() {
        log.debug("Test Mode: {}", DDAP_E2E_TEST_MODE);
        Assume.assumeThat(DDAP_E2E_TEST_MODE, equalTo("normal"));

        try {
            loginStrategy = StrategyFactory.getLoginStrategy();
            configStrategy = StrategyFactory.getConfigStrategy();
        } catch (Exception e){
            log.error(e.getMessage(),e);
            fail(e.getMessage());
        }
        setupRestassured();
    }

    private static void setupRestassured() {
        RestAssured.config = RestAssuredConfig.config()
            .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (cls, charset) -> {
                    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    return mapper;
                }
            ));
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = DDAP_BASE_URL;
    }

    protected static RequestSpecification getRequestSpecification() {
        return given();
    }

    protected static String loadTemplate(String resourcePath) {
        assertThat("Given config was null", resourcePath, notNullValue());
        final String resourceTemplate;
        try (InputStream is = AbstractBaseE2eTest.class.getResourceAsStream(resourcePath)) {
            final StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer, StandardCharsets.UTF_8);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load test resource template.", e);
        }
    }

    private static String stripTrailingSlash(String url) {
        return (url.endsWith("/"))
            ? url.substring(0, url.length() - 1)
            : url;
    }

}
