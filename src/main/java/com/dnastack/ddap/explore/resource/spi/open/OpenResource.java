package com.dnastack.ddap.explore.resource.spi.open;

import com.dnastack.ddap.explore.resource.model.AccessInterface;
import com.dnastack.ddap.explore.resource.model.Id;
import com.dnastack.ddap.explore.resource.model.Resource;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Data;

@Data
public class OpenResource {

    private String collectionName;
    private String name;
    private String description;
    private URI imageUrl;
    private String interfaceType;
    private URI interfaceUri;
    private Map<String, String> metadata;

    public boolean idEquals(Id id) {
        return Objects.equals(id.getResourceId(), name) && Objects.equals(id.getCollectionId(), collectionName)
            && Objects
            .equals(id.getInterfaceType(), interfaceType);
    }

    public Resource toResource(String realm, String spiKey) {
        Id collectionId = new Id();
        collectionId.setRealm(realm);
        collectionId.setSpiKey(spiKey);
        collectionId.setCollectionId(collectionName);
        Id resourceId = new Id(collectionId);
        resourceId.setResourceId(name);
        Id interfaceId = new Id(resourceId);
        interfaceId.setInterfaceType(interfaceType);
        return Resource.newBuilder()
            .id(resourceId.encodeId())
            .collectionId(collectionId.encodeId())
            .name(name)
            .imageUrl(imageUrl)
            .description(description)
            .interfaces(List.of(new AccessInterface(interfaceType, interfaceUri, interfaceId.encodeId(),false)))
            .metadata(metadata != null ? new HashMap<>(metadata) : null)
            .build();
    }

}
