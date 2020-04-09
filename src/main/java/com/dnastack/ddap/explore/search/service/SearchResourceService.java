package com.dnastack.ddap.explore.search.service;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.explore.search.model.SearchResourceResponseModel;
import dam.v1.DamService.GetFlatViewsResponse.FlatView;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class SearchResourceService {

    private Map<String, ReactiveDamClient> damClients;

    @Autowired
    public SearchResourceService(Map<String, ReactiveDamClient> damClients) {
        this.damClients = damClients;
    }

    public Mono<List<SearchResourceResponseModel>> getSearchResources(String realm) {
        return getAllFlattenedViews(realm)
                .filter(this::isSearchView)
                .map((view) -> {
                    var flatView = view.getFlatView();
                    return SearchResourceResponseModel.builder()
                            .damId(view.getDamId())
                            .resourcePath(view.getResourcePath())
                            .viewName(flatView.getViewName())
                            .resourceName(flatView.getResourceName())
                            .roleName(Optional.of(flatView.getRoleName()))
                            .interfaceName(Optional.of(flatView.getInterfaceName()))
                            .interfaceUri(Optional.of(flatView.getInterfaceUri()))
                            .ui(Map.of(
                                    "label", flatView.getViewUiMap().get("label"),
                                    "description", flatView.getViewUiMap().get("description"),
                                    "accessControlType", getAccessControlType(flatView)
                            ))
                            .build();
                })
                .collect(toList());
    }

    private String getAccessControlType(FlatView flatView) {
        return flatView.getLabelsMap().containsKey("accessControlType") ? flatView.getLabelsMap().get("accessControlType") : "protected";
    }

    private Flux<FlatViewMetadata> getAllFlattenedViews(String realm) {
        return Flux.fromStream(damClients.entrySet().stream())
                   .flatMap(clientEntry -> clientEntry.getValue().getFlattenedViews(realm)
                                                      .map(flatViewMap -> flatViewMap.entrySet()
                                                                                     .stream()
                                                                                     .map(e -> new FlatViewMetadata(clientEntry.getKey(), e.getKey(), e.getValue()))))
                   .flatMap(Flux::fromStream);
    }

    private boolean isSearchView(FlatViewMetadata viewMetadata) {
        return isSearchView(viewMetadata.getFlatView());
    }

    private boolean isSearchView(FlatView view) {
        return view.getInterfaceName().equalsIgnoreCase("http:search")
            || view.getInterfaceName().equalsIgnoreCase("https:search");
    }

    public Mono<URI> lookupFirstInterfaceUrlByResourcePath(String realm, String resourcePath) {
        return Mono.from(getAllFlattenedViews(realm)
                                 .filter((view) -> view.getFlatView().getResourcePath().equals(resourcePath))
                                 .map((viewMeta) -> viewMeta.getFlatView().getInterfaceUri())
                                 .map(URI::create)
                                 .take(1));
    }

    public Mono<List<SearchResourceResponseModel>> getSearchResourceViews(String resourceName, String realm) {
        return getAllFlattenedViews(realm)
                .filter((view) -> view.getFlatView().getResourceName().equals(resourceName))
                .map(view -> {
                    var flatView = view.getFlatView();
                    return SearchResourceResponseModel.builder()
                                                      .damId(view.getDamId())
                                                      .resourceName(flatView.getResourceName())
                                                      .viewName(flatView.getViewName())
                                                      .roleName(Optional.of(flatView.getRoleName()))
                                                      .interfaceName(Optional.of(flatView.getInterfaceName()))
                                                      .interfaceUri(Optional.of(flatView.getInterfaceUri()))
                                                      .resourcePath(flatView.getResourcePath())
                                                      .isSearchView(Optional.of(isSearchView(view)))
                                                      .ui(Map.of(
                                                              "label", flatView.getViewUiMap().get("label"),
                                                              "description", flatView.getViewUiMap().get("description"),
                                                              "accessControlType", getAccessControlType(flatView)
                                                      ))
                                                      .build();
                })
                .collect(toList());
    }

    @Value
    private static class FlatViewMetadata {
        private String damId;
        private String resourcePath;
        private FlatView FlatView;
    }
}
