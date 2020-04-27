package com.dnastack.ddap.explore.apps.wes.client;

import com.dnastack.ddap.common.client.WebClientFactory;
import com.dnastack.ddap.explore.apps.wes.model.DrsObjectModel;
import com.dnastack.ddap.explore.apps.wes.model.DrsObjectRequestModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Component
public class ReactiveDrsClient {

    public Mono<DrsObjectModel> getDrsObject(URI drsServerUrl) {
        return WebClientFactory.getWebClient()
            .get()
            .uri(drsServerUrl)
            .retrieve()
            .bodyToMono(DrsObjectRequestModel.class)
            .map(DrsObjectRequestModel::getObject);
    }
    
}
