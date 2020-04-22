package com.dnastack.ddap.explore.resource.spi.wallet;

import com.dnastack.ddap.explore.resource.model.AccessInterface;
import com.dnastack.ddap.explore.resource.model.Id;
import com.dnastack.ddap.explore.resource.model.Resource;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Data;

/**
 * An internal representation providing additional configuration of a resource to be served by the {@link
 * ReactiveWalletResourceClient}
 */
@Data
public class WalletResource {

    private String collectionName;
    private String name;
    private String description;
    private URI imageUrl;
    private String interfaceType;
    private URI interfaceUri;
    private String audience;
    private String scope;
    private Map<String, String> metadata;


    public boolean idEquals(Id id) {
        return idEquals(new WalletId(id));
    }

    public boolean idEquals(WalletId id) {
        return Objects.equals(id.getName(), name) && Objects.equals(id.getCollectionId(), collectionName) && Objects
            .equals(id.getInterfaceType(), interfaceType);
    }

    public Resource toResource(String realm, String spiKey) {
        WalletId collectionId = new WalletId();
        collectionId.setRealm(realm);
        collectionId.setSpiKey(spiKey);
        collectionId.setCollectionId(collectionName);

        WalletId id = new WalletId(collectionId);
        id.setName(name);

        WalletId authorizationId = new WalletId(id);
        authorizationId.setInterfaceType(interfaceType);

        return Resource.newBuilder()
            .id(id.encodeId())
            .collectionId(collectionId.encodeId())
            .name(name)
            .imageUrl(imageUrl)
            .description(description)
            .interfaces(List.of(new AccessInterface(interfaceType, interfaceUri, authorizationId.encodeId())))
            .metadata(metadata != null ? new HashMap<>(metadata) : null)
            .build();
    }

}
