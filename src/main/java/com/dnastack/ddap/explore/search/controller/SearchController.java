package com.dnastack.ddap.explore.search.controller;

import com.dnastack.ddap.common.controller.GlobalExceptionHandler;
import com.dnastack.ddap.explore.search.client.SearchClient;
import com.dnastack.ddap.explore.search.model.SearchTablesResponseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/realm/{realm}/search")
public class SearchController {

    private SearchClient searchClient;

    @Autowired
    public SearchController(SearchClient searchClient) {
        this.searchClient = searchClient;
    }

    @GetMapping("/tables")
    public Mono<SearchTablesResponseModel> getTables(@PathVariable String realm) throws URISyntaxException {
        URI prestoTablesUri = new URI("https://ga4gh-search-adapter-presto-public.staging.dnastack.com/tables");
        return searchClient.getTables(prestoTablesUri);
    }

    @PostMapping("/query")
    public Mono<Object> query(@RequestBody Map<String, String> queryData) throws URISyntaxException {
        URI prestoSearchUri = new URI("https://ga4gh-search-adapter-presto-public.staging.dnastack.com/search");
        return searchClient.query(prestoSearchUri, queryData);
    }
}
