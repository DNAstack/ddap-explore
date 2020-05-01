package com.dnastack.ddap.explore.apps.search.client;

import com.dnastack.ddap.common.client.WebClientFactory;
import com.dnastack.ddap.explore.apps.search.exception.SearchResourceException;
import com.dnastack.ddap.explore.apps.search.model.SearchTablesResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;


@Slf4j
@Component
public class SearchClient {

    public Mono<SearchTablesResponseModel> getTables(URI resourceUri) {
        return handleResponse(WebClientFactory.getWebClient()
                .get()
                .uri(resourceUri)
                .retrieve());
    }

    public Mono<SearchTablesResponseModel> getTables(URI resourceUri, String accessToken, String connectorKey, String connectorToken) {
        return handleResponse(WebClientFactory.getWebClient()
                .get()
                .uri(resourceUri)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .header("GA4GH-Search-Authorization", connectorKey + "="+ connectorToken)
                .retrieve());
    }

    private Mono<SearchTablesResponseModel> handleResponse(WebClient.ResponseSpec responseSpec) {
        return responseSpec.bodyToMono(SearchTablesResponseModel.class)
                .onErrorMap(ex -> {
                    try {
                        throw ex;
                    } catch (WebClientResponseException wcre) {
                        return new SearchResourceException(wcre.getResponseBodyAsString(), wcre.getRawStatusCode(), wcre);
                    } catch (Exception e) {
                        return new SearchResourceException(e.getMessage(), INTERNAL_SERVER_ERROR.value(), e);
                    } catch (Throwable t) {
                        return t;
                    }
                });
    }

    public Mono<Object> query(URI resourceUri, Map<String, String> queryData,
                              String accessToken, String connectorKey, String connectorToken) {
        log.info("Search client make query request {}", queryData.get("query"));
        return WebClientFactory.getWebClient()
                .post()
                .uri(resourceUri)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .header("GA4GH-Search-Authorization", connectorKey + "="+ connectorToken)
                .body(BodyInserters.fromObject(queryData))
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorMap(ex -> {
                    try {
                        throw ex;
                    } catch (WebClientResponseException wcre) {
                        return new SearchResourceException(wcre.getResponseBodyAsString(), wcre.getRawStatusCode(), wcre);
                    } catch (Exception e) {
                        return new SearchResourceException(e.getMessage(), INTERNAL_SERVER_ERROR.value(), e);
                    } catch (Throwable t) {
                        return t;
                    }
                });
    }
}
