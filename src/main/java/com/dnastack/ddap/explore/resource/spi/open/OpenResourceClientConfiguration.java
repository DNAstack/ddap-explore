package com.dnastack.ddap.explore.resource.spi.open;

import com.dnastack.ddap.explore.resource.model.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class OpenResourceClientConfiguration {


    /**
     * Resource Configuration should be provided as a Map, where the key is any arbitrary value, and the value is the
     * wallet resource representation. This approach is strictly to overcome springs inability to rebind configuration
     * parameters once the application has started
     */
    private Map<String, OpenResource> resources = new HashMap<>();

    /**
     * Collection Configuration should be provided as a Map, where the key is any arbitrary value, and the value is the
     * wallet resource representation. This approach is strictly to overcome springs inability to rebind configuration
     * parameters once the application has started
     */
    private Map<String, Collection> collections = new HashMap<>();

}
