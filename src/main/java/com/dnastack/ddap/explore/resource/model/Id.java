package com.dnastack.ddap.explore.resource.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;

@Data
public class Id implements Serializable {


    private static final ObjectMapper mapper = new ObjectMapper();
    private static final long serialVersionUID = -1479218240983143860L;

    public Id() {
    }

    public Id(Id id) {
        this.spiKey = id.spiKey;
        this.collectionId = id.collectionId;
        this.realm = id.realm;
        this.additionalProperties.putAll(id.additionalProperties);
    }

    /**
     * SPI Skey
     */
    @JsonProperty("k")
    String spiKey;

    @JsonProperty("r")
    String realm;

    /**
     * Collection Id
     */
    @JsonProperty("c")
    String collectionId;

    @JsonIgnore
    Map<String, String> additionalProperties = new TreeMap<>();

    @JsonAnyGetter
    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(String key, String value) {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<>();
        }
        additionalProperties.put(key, value);
    }

    @JsonAnySetter
    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        if (this.additionalProperties == null) {
            this.additionalProperties = additionalProperties;
        } else {
            this.additionalProperties.putAll(additionalProperties);
        }
    }

    public static Id decodeId(String idString) {
        String decodedIdString = new String(Base64.getUrlDecoder().decode(idString), StandardCharsets.UTF_8);
        try {
            return mapper.readValue(decodedIdString, Id.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String encodeId() {
        try {
            return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mapper.writeValueAsString(this).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
