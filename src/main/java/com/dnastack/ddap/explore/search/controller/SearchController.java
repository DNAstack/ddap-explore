package com.dnastack.ddap.explore.search.controller;

import com.dnastack.ddap.explore.search.client.SearchClient;
import com.dnastack.ddap.explore.search.model.SearchResourceResponseModel;
import com.dnastack.ddap.explore.search.model.SearchTablesResponseModel;
import com.dnastack.ddap.explore.search.service.SearchResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import dam.v1.DamService.GetFlatViewsResponse.FlatView;


import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/realm/{realm}/search")
public class SearchController {

    private SearchClient searchClient;
    private SearchResourceService searchResourceService;

    @Autowired
    public SearchController(SearchClient searchClient,
                            SearchResourceService searchResourceService) {
        this.searchClient = searchClient;
        this.searchResourceService = searchResourceService;
    }

    @GetMapping("/resources")
    public Mono<List<SearchResourceResponseModel>> getSearchResources(@PathVariable String realm) {
        return searchResourceService.getSearchResources(realm);
    }

    @GetMapping("/resource/{resourceName}")
    public Mono<SearchResourceResponseModel> getResourceDetails(@PathVariable String realm,
                                                                @PathVariable String resourceName) {
        return searchResourceService.getSearchResourceViews(resourceName, realm);
//        return null;
    }

    @GetMapping("/tables")
    public Mono<SearchTablesResponseModel> getTables(@PathVariable String realm,
                                                     @RequestParam("resource") String resourcePath,
                                                     @RequestParam("accessToken") String accessToken) {
        String urlDecodedResourcePath = URLDecoder.decode(resourcePath, Charset.defaultCharset());
        Mono<URI> interfaceUriMono = searchResourceService.lookupFirstInterfaceUrlByResourcePath(realm, urlDecodedResourcePath);

        return interfaceUriMono
            .flatMap((rootUri) -> searchClient.getTables(URI.create(rootUri + "/tables"), accessToken));
    }

    @PostMapping("/query")
    public Mono<Object> query(@PathVariable String realm,
                              @RequestParam("resource") String resourcePath,
                              @RequestBody Map<String, String> queryData) {
        String urlDecodedResourcePath = URLDecoder.decode(resourcePath, Charset.defaultCharset());
        Mono<URI> interfaceUriMono = searchResourceService.lookupFirstInterfaceUrlByResourcePath(realm, urlDecodedResourcePath);

        return interfaceUriMono
            .flatMap((rootUri) -> searchClient.query(URI.create(rootUri + "/search"), queryData));
    }
}
