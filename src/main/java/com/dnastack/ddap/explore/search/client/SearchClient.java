package com.dnastack.ddap.explore.search.client;

import com.dnastack.ddap.common.client.WebClientFactory;
import com.dnastack.ddap.explore.search.model.SearchTablesResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Component
public class SearchClient {

    public Mono<SearchTablesResponseModel> getTables(URI resourceUri) {
        log.info("Search client get tables is called");
        return WebClientFactory.getWebClient()
                .get()
                .uri(resourceUri)
                .retrieve()
                .bodyToMono(SearchTablesResponseModel.class);
    }
}
