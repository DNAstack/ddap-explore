package com.dnastack.ddap.explore.search.controller;

import com.dnastack.ddap.explore.search.client.SearchClient;
import com.dnastack.ddap.explore.search.model.SearchResourceResponseModel;
import com.dnastack.ddap.explore.search.model.SearchTablesResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/realm/{realm}/search")
public class SearchController {

    private SearchClient searchClient;

    @Autowired
    public SearchController(SearchClient searchClient) {
        this.searchClient = searchClient;
    }

    @GetMapping
    public Mono<List<SearchResourceResponseModel>> getSearchResources(@PathVariable String realm) {
        SearchResourceResponseModel resource = SearchResourceResponseModel.builder()
            .resourceUrl("https://ga4gh-search-adapter-presto-public.staging.dnastack.com")
            .ui(Map.of(
                "label", "GA4GH Search Adapter Presto",
                "description", "Presto Search"
            ))
            .build();
        return Mono.just(asList(resource));
    }

    @GetMapping("/tables")
    public Mono<SearchTablesResponseModel> getTables(@PathVariable String realm,
                                                     @RequestParam("resource") String resourceUrl) {
        URI prestoTablesUri = URI.create(URLDecoder.decode(resourceUrl + "/tables", Charset.defaultCharset()));
        return searchClient.getTables(prestoTablesUri);
    }

    @PostMapping("/query")
    public Mono<Object> query(@RequestParam("resource") String resourceUrl,
                              @RequestBody Map<String, String> queryData) {
        URI prestoSearchUri = URI.create(URLDecoder.decode(resourceUrl + "/search", Charset.defaultCharset()));
        return searchClient.query(prestoSearchUri, queryData);
    }
}
