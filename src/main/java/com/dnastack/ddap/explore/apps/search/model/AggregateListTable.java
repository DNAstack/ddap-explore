package com.dnastack.ddap.explore.apps.search.model;

import java.net.URI;
import java.util.List;
import lombok.Data;

@Data
public class AggregateListTable {

    private URI authorizationUrlBase;
    private boolean requiresAdditionalAuth = false;
    private List<ListTables> tableResources;

}
