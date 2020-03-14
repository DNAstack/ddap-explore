package com.dnastack.ddap.explore.search.controller;

import com.dnastack.ddap.explore.search.client.SearchClient;
import com.dnastack.ddap.explore.search.model.SearchTablesResponseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

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
        log.info("Tables controller method is called");
        URI prestoTablesUri = new URI("https://ga4gh-search-adapter-presto-public.staging.dnastack.com/tables");
        return searchClient.getTables(prestoTablesUri);
    }
}
