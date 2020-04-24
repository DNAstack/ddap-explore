package com.dnastack.ddap.explore.resource.model;

import com.dnastack.ddap.explore.resource.exception.ResourceIdEncodingException;
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
        this.resourceId = id.resourceId;
        this.interfaceType = id.interfaceType;
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

    @JsonProperty("n")
    String resourceId;

    @JsonProperty("i")
    String interfaceType;

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


    public static Id decodeCollectionId(String idString) {
        Id collectionId = decodeId(idString);

        if (collectionId.getCollectionId() == null) {
            throw new ResourceIdEncodingException("Could not decode CollectionId, missing required encoded attribute for collection: c");
        }
        return collectionId;
    }

    public static Id decodeResourceId(String idString) {
        Id resourceId = decodeCollectionId(idString);
        if (resourceId.getResourceId() == null) {
            throw new ResourceIdEncodingException("Could not decode ResourceId, missing required encoded attribute for resource: n");
        }
        return resourceId;
    }

    public static Id decodeInterfaceId(String idString) {
        Id interfaceId = decodeResourceId(idString);
        if (interfaceId.getResourceId() == null) {
            throw new ResourceIdEncodingException("Could not decode InterfaceId, missing required encoded attribute for interface: i");
        }
        return interfaceId;
    }

    private static Id decodeId(String idString) {
        String decodedIdString = new String(Base64.getUrlDecoder().decode(idString), StandardCharsets.UTF_8);
        try {
            Id id = mapper.readValue(decodedIdString, Id.class);
            if (id.getSpiKey() == null) {
                throw new ResourceIdEncodingException("Could not decode CollectionId, missing required encoded attribute for spiKey: k");
            }
            if (id.getRealm() == null) {
                throw new ResourceIdEncodingException("Could not decode CollectionId, missing required encoded attribute for realm: r");
            }
            return id;
        } catch (IOException e) {
            throw new ResourceIdEncodingException(
                "Could not decode resource id: " + idString + " - " + e.getMessage(), e);
        }
    }

    public String encodeId() {
        try {
            return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mapper.writeValueAsString(this).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ResourceIdEncodingException("Could not encode resource Id", e);
        }
    }

}
