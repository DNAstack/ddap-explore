package com.dnastack.ddap.explore.dataset.controller;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.common.security.UserTokenCookiePackager;
import com.dnastack.ddap.explore.dataset.client.DatasetErrorException;
import com.dnastack.ddap.explore.dataset.client.ReactiveDatasetClient;
import com.dnastack.ddap.explore.dataset.client.model.DatasetResult;
import com.dnastack.ddap.explore.wes.service.ViewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1alpha/{realm}/dataset")
public class DatasetController {

    private final ReactiveDatasetClient datasetClient;
    private final UserTokenCookiePackager cookiePackager;
    private final ViewsService viewsService;

    @Autowired
    public DatasetController(Map<String, ReactiveDamClient> damClients, UserTokenCookiePackager cookiePackager,
                             ReactiveDatasetClient datasetClient, ViewsService viewsService) {
        this.datasetClient = datasetClient;
        this.cookiePackager = cookiePackager;
        this.viewsService = viewsService;
    }


    @GetMapping(params = "dataset_url")
    public Mono<DatasetResult> fetchDataset(@RequestParam("dataset_url") String datasetUrl,
                                            @RequestParam(value = "access_token", required = false) String accessToken,
                                            ServerHttpRequest request,
                                            @PathVariable String realm) {
        return getDatasetResult(datasetUrl, accessToken, request, realm);
    }

    private Mono<DatasetResult> getDatasetResult(String datasetUrl, String token, ServerHttpRequest request, String realm) {
        return datasetClient.fetchSingleDataset(datasetUrl, token).onErrorResume(e -> {
            if (!DatasetErrorException.class.isAssignableFrom(e.getClass())) {
                throw new DatasetErrorException(500, e.getMessage());
            } else if(((DatasetErrorException) e).getStatus() == 403) {
//                return getAccess(datasetUrl, request, realm);
                return Mono.empty();
            }else {
                throw (DatasetErrorException) e;
            }
        });
    }

//    private Mono<DatasetResult> getAccess(String datasetUrl, ServerHttpRequest request, String realm) {
//        Map<CookieKind, UserTokenCookiePackager.CookieValue> tokens = cookiePackager.extractRequiredTokens(request, Set.of(CookieKind.DAM, CookieKind.REFRESH));
//        return viewsService.getRelevantViewsForUrlInAllDams(realm, datasetUrl, tokens).flatMap(viewsForUrl -> {
//            if(!viewsForUrl.isEmpty()) {
//                List<String> uniqueViews = new ArrayList<>(new HashSet<>(viewsForUrl));
//                return viewsService.authorizeViews(uniqueViews, tokens, realm)
//                    .collectList()
//                    .flatMap(viewAuthorizationResponses -> {
//                        // assuming that there is only one token for a bucket
//                        if (!viewAuthorizationResponses.isEmpty()) {
//                            ResourceToken tokenResponse = viewAuthorizationResponses.get(0).getLocationAndToken();
//                            String accessToken = "";
//                            if (tokenResponse != null) {
//                                accessToken = tokenResponse.getToken();
//                            }
//                            return getDatasetResult(datasetUrl, accessToken, request, realm);
//                        } else {
//                            return Mono.error(new DatasetErrorException(403, "You are not allowed to access this resource"));
//                        }
//                });
//            } else {
//                return Mono.error(new DatasetErrorException(500, "No views associated with the resource"));
//            }
//        }
//        );
//    }
}
