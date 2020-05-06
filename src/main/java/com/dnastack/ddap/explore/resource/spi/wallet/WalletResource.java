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

    private String collectionId;
    private String name;
    private String description;
    private URI imageUrl;
    private String interfaceType;
    private URI interfaceUri;
    private String audience;
    private String scope;
    private Map<String, String> metadata;


}
