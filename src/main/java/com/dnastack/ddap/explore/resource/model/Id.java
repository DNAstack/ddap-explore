package com.dnastack.ddap.explore.resource.model;

import com.dnastack.ddap.explore.resource.exception.IllegalIdentifierException;
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
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;
import lombok.NonNull;

@Data
public abstract class Id implements Serializable {


    private static final ObjectMapper mapper = new ObjectMapper();
    private static final long serialVersionUID = -1479218240983143860L;
    public static final String COLLECTION_PROPERTY_KEY = "c";
    public static final String RESOURCE_PROPERTY_KEY = "n";
    public static final String INTERFACE_PROPERTY_KEY = "i";

    public Id() {
    }

    public Id(Id id) {
        this.spiKey = id.spiKey;
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

    @JsonIgnore
    Map<String, String> additionalProperties = new TreeMap<>();

    @JsonAnyGetter
    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }


    @JsonIgnore
    public String getAdditionalProperty(String key) {
        return additionalProperties.get(key);
    }

    @JsonAnySetter
    public void setAdditionalProperty(String key, String value) {
        if (additionalProperties == null) {
            additionalProperties = new TreeMap<>();
        }
        additionalProperties.put(key, value);
    }

    @JsonAnySetter
    public void setAdditionalProperties(@NonNull Map<String, String> additionalProperties) {
        if (this.additionalProperties == null) {
            this.additionalProperties = additionalProperties;
        } else {
            this.additionalProperties.putAll(additionalProperties);
        }
    }


    public void validate() {
        if (spiKey == null) {
            throw new IllegalIdentifierException("Missing Required attribute in identifier: spiKey");
        }

        if (realm == null) {
            throw new IllegalIdentifierException("Missing Required attribute in identifier: realm");
        }
    }

    public CollectionId toCollectionId() {
        CollectionId collectionId = new CollectionId(this);
        return collectionId;
    }

    public ResourceId toResourceId() {
        ResourceId resourceId = new ResourceId(this);
        return resourceId;
    }

    public InterfaceId toInterfaceId() {
        InterfaceId interfaceId = new InterfaceId(this);
        return interfaceId;
    }

    @Data
    public static class CollectionId extends Id {

        private static final long serialVersionUID = -8122211961586936183L;
        private static final String COLLECTION_ID = "c";

        @JsonProperty(COLLECTION_PROPERTY_KEY)
        String name;

        public CollectionId() {
        }

        public CollectionId(Id id) {
            super(id);

            if (id instanceof CollectionId) {
                this.name = ((CollectionId) id).getName();
            }

            if (id instanceof ResourceId) {
                this.name = ((ResourceId) id).getCollectionName();
            }

            if (id instanceof InterfaceId) {
                this.name = ((InterfaceId) id).getCollectionName();
            }

        }

        @Override
        public void validate() {
            super.validate();
            if (getName() == null) {
                throw new IllegalIdentifierException("Missing Required attribute in identifier: collectionId");
            }
        }
    }

    @Data
    public static class ResourceId extends Id {

        private static final long serialVersionUID = -8122211961586936183L;
        private static final String RESOURCE_ID_KEY = "n";
        private static final String COLLECTION_ID = "c";

        @JsonProperty(COLLECTION_PROPERTY_KEY)
        String collectionName;

        @JsonProperty(RESOURCE_PROPERTY_KEY)
        String name;

        public ResourceId() {
        }

        public ResourceId(Id id) {
            super(id);
            if (id instanceof CollectionId) {
                this.name = ((CollectionId) id).getName();
            }

            if (id instanceof ResourceId) {
                collectionName = ((ResourceId) id).getCollectionName();
                this.name = ((ResourceId) id).getName();
            }

            if (id instanceof InterfaceId) {
                collectionName = ((InterfaceId) id).getCollectionName();
                this.name = ((InterfaceId) id).getResourceName();
            }
        }

        @Override
        public void validate() {
            super.validate();
            if (getName() == null) {
                throw new IllegalIdentifierException("Missing Required attribute in identifier: resourceId");
            }

            if (getCollectionName() == null) {
                throw new IllegalIdentifierException("Missing Required attribute in identifier: collectionId");
            }
        }
    }

    @Data
    public static class InterfaceId extends Id {

        private static final long serialVersionUID = -8122211961586936183L;

        @JsonProperty(COLLECTION_PROPERTY_KEY)
        String collectionName;

        @JsonProperty(RESOURCE_PROPERTY_KEY)
        String resourceName;

        @JsonProperty(INTERFACE_PROPERTY_KEY)
        String type;

        public InterfaceId() {
        }

        public InterfaceId(Id id) {
            super(id);
            if (id instanceof CollectionId) {
                resourceName = ((CollectionId) id).getName();
            }

            if (id instanceof ResourceId) {
                collectionName = ((ResourceId) id).getCollectionName();
                resourceName = ((ResourceId) id).getName();
            }

            if (id instanceof InterfaceId) {
                collectionName = ((InterfaceId) id).getCollectionName();
                resourceName = ((InterfaceId) id).getResourceName();
                this.type = ((InterfaceId) id).getType();
            }
        }


        @Override
        public void validate() {
            super.validate();
            if (getResourceName() == null) {
                throw new IllegalIdentifierException("Missing Required attribute in identifier: resourceId");
            }

            if (getCollectionName() == null) {
                throw new IllegalIdentifierException("Missing Required attribute in identifier: collectionId");
            }
            if (getType() == null) {
                throw new IllegalIdentifierException("Missing Required attribute in identifier: interfaceType");
            }
        }
    }


    public static CollectionId decodeCollectionId(String idString) {
        CollectionId collectionId = decodeId(idString, CollectionId.class);
        collectionId.validate();
        return collectionId;
    }

    public static ResourceId decodeResourceId(String idString) {
        ResourceId resourceId = decodeId(idString, ResourceId.class);
        resourceId.validate();
        return resourceId;
    }

    public static InterfaceId decodeInterfaceId(String idString) {
        InterfaceId interfaceId = decodeId(idString, InterfaceId.class);
        interfaceId.validate();
        return interfaceId;
    }

    private static <T extends Id> T decodeId(String idString, Class<T> clazz) {
        String decodedIdString = new String(Base64.getUrlDecoder().decode(idString), StandardCharsets.UTF_8);
        try {
            return mapper.readValue(decodedIdString, clazz);
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
