package com.dnastack.ddap.explore.search.client;

import com.dnastack.ddap.common.client.WebClientFactory;
import com.dnastack.ddap.explore.search.model.SearchTablesResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;


@Slf4j
@Component
public class SearchClient {

    public Mono<SearchTablesResponseModel> getTables(URI resourceUri) {
        return WebClientFactory.getWebClient()
                .get()
                .uri(resourceUri)
                .retrieve()
                .bodyToMono(SearchTablesResponseModel.class);
    }

    public Mono<Object> query(URI resourceUri, Map<String, String> queryData) {
        log.info("Search client make query request {}", queryData.get("query"));
        return WebClientFactory.getWebClient()
                .post()
                .uri(resourceUri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(queryData))
                .retrieve()
                .bodyToMono(Object.class);
    }
}
