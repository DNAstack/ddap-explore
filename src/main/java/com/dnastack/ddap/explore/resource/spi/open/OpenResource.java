package com.dnastack.ddap.explore.resource.spi.open;

import com.dnastack.ddap.explore.resource.model.AccessInterface;
import com.dnastack.ddap.explore.resource.model.Id.InterfaceId;
import com.dnastack.ddap.explore.resource.model.Resource;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class OpenResource {


    private String collectionId;
    private String name;
    private String description;
    private URI imageUrl;
    private String interfaceType;
    private URI interfaceUri;
    private Map<String, String> metadata;

}
