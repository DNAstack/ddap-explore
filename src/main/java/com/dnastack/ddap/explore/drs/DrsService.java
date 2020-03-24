package com.dnastack.ddap.explore.drs;

import com.dnastack.ddap.explore.wes.client.ReactiveDrsClient;
import com.dnastack.ddap.explore.wes.model.DrsObjectModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class DrsService {

    private final ReactiveDrsClient drsClient;

    public boolean isDrsUri(String uri) {
        return uri.contains("/ga4gh/drs/");
    }

    public Mono<List<String>> resolveAccessMethods(URI drsUri, String accessMethodType) {
        if (!isDrsUri(drsUri.toString())) {
            return Mono.just(List.of());
        } else {
            return drsClient.getDrsObject(drsUri)
                            .map(drsObject -> {
                                // Skip if there is no access method for GS
                                if (drsObject.getAccessMethods() == null) {
                                    log.debug("No access methods for DRS Object with id {}", drsObject.getId());
                                }

                                return Optional.ofNullable(drsObject.getAccessMethods())
                                               .stream()
                                               .flatMap(Collection::stream)
                                               .filter((accessMethod) -> accessMethod.getType().equalsIgnoreCase(accessMethodType))
                                               //
                                               .map(DrsObjectModel.AccessMethod::getAccessUrl)
                                               .map(DrsObjectModel.AccessUrl::getUrl)
                                               .collect(toList());
                            });
        }
    }
}