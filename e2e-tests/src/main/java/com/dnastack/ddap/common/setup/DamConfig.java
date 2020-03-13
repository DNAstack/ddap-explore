package com.dnastack.ddap.common.setup;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hamcrest.Matchers;

@Getter
@Setter
public class DamConfig implements ConfigModel {


    /**
     * The Base URL of the DAM
     */
    private String damBaseUrl;

    /**
     * The Username for the dam if Basic Auth is enabled
     */
    private String damUsername;

    /**
     * The Password for the dam if basic Auth is enabled
     */
    private String damPassword;

    /**
     * The Realm JSON to send to the dam for the tests. Please note, this should not include Any templated values
     */
    private Map<String, Object> damRealmJson;

    /**
     * The Url of the Associated IC
     */
    private String icUrl;

    public void validateConfig() {
        assertThat(damBaseUrl, Matchers.notNullValue());
        assertThat(damRealmJson, Matchers.notNullValue());
    }
}
