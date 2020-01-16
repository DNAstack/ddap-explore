package com.dnastack.ddap.explore.dataset.controller;

import com.dnastack.ddap.explore.dataset.client.DatasetErrorException;
import com.dnastack.ddap.explore.dataset.client.ReactiveDatasetClient;
import com.dnastack.ddap.explore.dataset.model.DatasetResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/realm/{realm}/dataset")
public class DatasetController {

    private final ReactiveDatasetClient datasetClient;

    @Autowired
    public DatasetController(ReactiveDatasetClient datasetClient) {
        this.datasetClient = datasetClient;
    }

    @GetMapping(params = "dataset_url")
    public Mono<DatasetResult> fetchDataset(
        @RequestParam("dataset_url") String datasetUrl,
        @RequestParam(value = "access_token", required = false) String accessToken
    ) {
        return getDatasetResult(datasetUrl, accessToken);
    }

    private Mono<DatasetResult> getDatasetResult(String datasetUrl, String token) {
        return datasetClient.fetchSingleDataset(datasetUrl, token)
            .onErrorResume((error) -> {
                if (!(error instanceof DatasetErrorException)) {
                    throw new DatasetErrorException(500, error.getMessage());
                }
                return Mono.error(error);
        });
    }

}
