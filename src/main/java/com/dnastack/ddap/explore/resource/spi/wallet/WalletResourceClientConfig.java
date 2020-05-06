package com.dnastack.ddap.explore.resource.spi.wallet;

import com.dnastack.ddap.explore.resource.model.Collection;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class WalletResourceClientConfig {

    private URI tokenUrl;
    private URI authorizationUrl;
    private String clientId;
    private String clientSecret;

    /**
     * Resource Configuration should be provided as a Map, where the key is the resource ID, and the value is the wallet
     * resource representation.
     */
    private Map<String, WalletResource> resources = new HashMap<>();

    /**
     * Collection Configuration should be provided as a Map, where the key is the collection ID, and the value is the
     * wallet resource representation.
     */
    private Map<String, Collection> collections = new HashMap<>();

}
