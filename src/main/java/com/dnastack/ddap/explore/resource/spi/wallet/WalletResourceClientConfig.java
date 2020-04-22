package com.dnastack.ddap.explore.resource.spi.wallet;

import com.dnastack.ddap.explore.resource.model.Collection;
import java.net.URI;
import java.util.Map;
import lombok.Data;

@Data
public class WalletResourceClientConfig {

    private URI tokenUrl;
    private URI authorizationUrl;
    private String clientId;
    private String clientSecret;

    /**
     * Resource Configuration should be provided as a Map, where the key is any arbitrary value, and the value is the
     * wallet resource representation. This approach is strictly to overcome springs inability to rebind configuration
     * parameters once the application has started
     */
    private Map<String, WalletResource> resources;

    /**
     * Collection Configuration should be provided as a Map, where the key is any arbitrary value, and the value is the
     * wallet resource representation. This approach is strictly to overcome springs inability to rebind configuration
     * parameters once the application has started
     */
    private Map<String, Collection> collections;

    public java.util.Collection<WalletResource> getResources() {
        return resources.values();
    }

    public java.util.Collection<Collection> getCollections() {
        return collections.values();
    }

}
