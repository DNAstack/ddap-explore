package com.dnastack.ddap.explore.apps.search.controller;

import com.dnastack.ddap.explore.apps.search.client.SearchClient;
import com.dnastack.ddap.explore.apps.search.model.SearchResourceResponseModel;
import com.dnastack.ddap.explore.apps.search.model.SearchTablesResponseModel;
import com.dnastack.ddap.explore.apps.search.service.SearchResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/realm/{realm}/search")
public class SearchV1AlphaController {
    private SearchClient searchClient;
    private SearchResourceService searchResourceService;

    @Autowired
    public SearchV1AlphaController(SearchClient searchClient,
                            SearchResourceService searchResourceService) {
        this.searchClient = searchClient;
        this.searchResourceService = searchResourceService;
    }

    @GetMapping("/resources")
    public Mono<List<SearchResourceResponseModel>> getSearchResources(@PathVariable String realm) {
        return searchResourceService.getSearchResources(realm);
    }

    @GetMapping("/resource/{resourceName}")
    public Mono<List<SearchResourceResponseModel>> getResourceDetails(@PathVariable String realm,
                                                                @PathVariable String resourceName) {
        return searchResourceService.getSearchResourceViews(resourceName, realm);
    }

    @GetMapping("/tables")
    public Mono<SearchTablesResponseModel> getTables(@PathVariable String realm,
                                                     @RequestParam("resource") String resourcePath,
                                                     @RequestParam(value = "connectorKey", required = false) String connectorKey,
                                                     @RequestParam(value = "connectorToken", required = false) String connectorToken,
                                                     @RequestParam(value = "accessToken", required = false) String accessToken) {
        String urlDecodedResourcePath = URLDecoder.decode(resourcePath, Charset.defaultCharset());
        Mono<URI> interfaceUriMono = searchResourceService.lookupFirstInterfaceUrlByResourcePath(realm, urlDecodedResourcePath);

        return interfaceUriMono
            .flatMap((rootUri) -> {
                URI destinationUri = URI.create(rootUri + "/tables");
                if (accessToken == null || connectorKey == null || connectorToken == null) {
                    return searchClient.getTables(destinationUri);
                }
                return searchClient.getTables(destinationUri, accessToken, connectorKey, connectorToken);
            });
    }

    @PostMapping("/query")
    public Mono<Object> query(@PathVariable String realm,
                              @RequestParam("resource") String resourcePath,
                              @RequestParam(value = "connectorKey", required = false) String connectorKey,
                              @RequestParam(value = "connectorToken", required = false) String connectorToken,
                              @RequestParam(value = "accessToken", required = false) String accessToken,
                              @RequestBody Map<String, String> queryData) {
        String urlDecodedResourcePath = URLDecoder.decode(resourcePath, Charset.defaultCharset());
        Mono<URI> interfaceUriMono = searchResourceService.lookupFirstInterfaceUrlByResourcePath(realm, urlDecodedResourcePath);

        return interfaceUriMono
            .flatMap((rootUri) -> searchClient.query(URI.create(rootUri + "/search"), queryData, accessToken, connectorKey, connectorToken));
    }
}
