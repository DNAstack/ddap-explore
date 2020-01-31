package com.dnastack.ddap.explore.dataset.controller;

import com.dnastack.ddap.explore.dataset.client.TableErrorException;
import com.dnastack.ddap.explore.dataset.client.ReactiveTableClient;
import com.dnastack.ddap.explore.dataset.model.TableData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/realm/{realm}/table")
public class TableController {

    private final ReactiveTableClient datasetClient;

    @Autowired
    public TableController(ReactiveTableClient datasetClient) {
        this.datasetClient = datasetClient;
    }

    @GetMapping
    public Mono<TableData> fetchDataset(
        @RequestParam("dataset_url") String sourceUrl,
        @RequestParam(value = "access_token", required = false) String accessToken
    ) {
        return getDatasetResult(sourceUrl, accessToken);
    }

    private Mono<TableData> getDatasetResult(String sourceUrl, String token) {
        return datasetClient.fetchSingleDataset(sourceUrl, token)
            .onErrorResume((error) -> {
                if (!(error instanceof TableErrorException)) {
                    throw new TableErrorException(500, error.getMessage());
                }
                return Mono.error(error);
        });
    }

}
