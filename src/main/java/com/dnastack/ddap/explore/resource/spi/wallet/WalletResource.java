package com.dnastack.ddap.explore.resource.spi.wallet;

import com.dnastack.ddap.explore.resource.model.AccessInterface;
import com.dnastack.ddap.explore.resource.model.Id.InterfaceId;
import com.dnastack.ddap.explore.resource.model.Resource;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    public InterfaceId getInterfaceId(String realm,String spiKey){
        InterfaceId id = new InterfaceId();
        id.setRealm(realm);
        id.setSpiKey(spiKey);
        id.setResourceName(name);
        id.setCollectionName(collectionName);
        id.setType(interfaceType);
        return id;
    }

    public Resource toResource(String realm, String spiKey) {
        InterfaceId interfaceId = getInterfaceId(realm,spiKey);
        return Resource.newBuilder()
            .id(interfaceId.toResourceId().encodeId())
            .collectionId(interfaceId.toCollectionId().encodeId())
            .name(name)
            .imageUrl(imageUrl)
            .description(description)
            .interfaces(List.of(new AccessInterface(interfaceType, interfaceUri, interfaceId.encodeId(), true)))
            .metadata(metadata != null ? new HashMap<>(metadata) : null)
            .build();
    }

}
