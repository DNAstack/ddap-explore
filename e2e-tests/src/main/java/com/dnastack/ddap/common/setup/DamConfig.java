package com.dnastack.ddap.common.setup;

import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hamcrest.Matchers;

@Getter
@Setter
public class DamConfig implements ConfigModel {


    private String damBaseUrl;
    private String damUsername;
    private String damPassword;

    //Process this as nested JSON, so the string does not need to be escaped
    private Map<String,Object> damRealmJson;
    private String icUrl;

    public void validateConfig() {
        assertThat(damBaseUrl, Matchers.notNullValue());
        assertThat(damRealmJson, Matchers.notNullValue());
    }

    public String getDamRealmJsonAsString(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(damRealmJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
