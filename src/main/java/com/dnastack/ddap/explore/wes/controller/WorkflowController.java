package com.dnastack.ddap.explore.wes.controller;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.common.config.DamProperties;
import com.dnastack.ddap.common.config.DamsConfig;
import com.dnastack.ddap.common.security.AuthCookieNotPresentInRequestException;
import com.dnastack.ddap.common.security.UserTokenCookiePackager;
import com.dnastack.ddap.explore.wes.client.ReactiveWdlValidatorClient;
import com.dnastack.ddap.explore.wes.model.WesResourceViews;
import com.dnastack.ddap.explore.wes.model.WorkflowExecutionRunModel;
import com.dnastack.ddap.explore.wes.model.WorkflowExecutionRunRequestModel;
import com.dnastack.ddap.explore.wes.model.WorkflowExecutionRunsResponseModel;
import com.dnastack.ddap.explore.wes.service.WesResourceService;
import com.dnastack.ddap.explore.wes.service.WesService;
import dam.v1.DamService;
import dam.v1.DamService.ResourceTokens;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/realm/{realm}/wes")
public class WorkflowController {

    private final Map<String, DamProperties> dams;
    private WesResourceService wesResourceService;
    private WesService wesService;

    private ReactiveWdlValidatorClient wdlValidatorClient;
    private Map<String, ReactiveDamClient> damClients;

    private UserTokenCookiePackager cookiePackager;

    @Autowired
    public WorkflowController(ReactiveWdlValidatorClient wdlValidatorClient,
                              Map<String, ReactiveDamClient> damClients,
                              WesResourceService wesResourceService,
                              WesService wesService,
                              UserTokenCookiePackager cookiePackager,
                              DamsConfig damsConfig) {
        this.wdlValidatorClient = wdlValidatorClient;
        this.wesResourceService = wesResourceService;
        this.wesService = wesService;
        this.damClients = damClients;
        this.cookiePackager = cookiePackager;
        this.dams = Map.copyOf(damsConfig.getStaticDamsConfig());
    }

    @PostMapping(value = "/describe")
    public Mono<Object> getJsonSchemaFromWdl(@RequestBody String wdl) {
        return wdlValidatorClient.getJsonSchema(wdl);
    }

    @GetMapping(value = "/views")
    public Flux<WesResourceViews> getWesResources(@PathVariable String realm) {
        return Flux.merge(damClients.entrySet().stream()
                .map(damClient -> wesResourceService.getResources(damClient, realm))
                .collect(toList())
        );
    }

    @GetMapping(value = "/{damId}/views/{viewId}/runs")
    public Mono<WorkflowExecutionRunsResponseModel> getWorkflowRuns(@PathVariable String realm,
                                                                    @PathVariable String damId,
                                                                    @PathVariable String viewId,
                                                                    @RequestParam String accessToken,
                                                                    @RequestParam(required = false) String nextPage) {
        // TODO: use damClientFactory
        Map.Entry<String, ReactiveDamClient> damClient = damClients.entrySet().stream()
                .filter(damClientEntry -> damClientEntry.getKey().equals(damId))
                .findFirst()
                .get();
        return wesService.getAllWorkflowRunsFromWesServer(damClient, realm, accessToken, viewId, nextPage);
    }

    @Value
    private static class ViewDescriptor {
        private String resourceId;
        private DamService.View view;
    }

    @PostMapping(value = "/{damId}/views/{viewId}/runs")
    public Mono<WorkflowExecutionRunModel> addWorkflowToRun(ServerHttpRequest request,
                                                            @PathVariable String realm,
                                                            @PathVariable String damId,
                                                            @PathVariable String viewId,
                                                            @RequestParam("resource") List<URI> resources,
                                                            @RequestBody WorkflowExecutionRunRequestModel runRequest) {
        // TODO: use damClientFactory
        Map.Entry<String, ReactiveDamClient> damClient = damClients.entrySet().stream()
                .filter(damClientEntry -> damClientEntry.getKey().equals(damId))
                .findFirst()
                .get();
        Mono<ResourceTokens> checkoutResponse = checkoutToken(request, damClient.getValue(), resources);
        Mono<URI> wesUriMono = Mono.from(wesResourceService.getResources(damClient, realm)
                .filter(resourceView -> resourceView.getViews()
                        .stream()
                        .anyMatch(v -> v.getKey().equals(viewId)))
                .map(resourceView -> {
                    Map.Entry<String, DamService.View> view = wesResourceService.getViewById(resourceView, viewId);
                    return new ViewDescriptor(resourceView.getResource().getKey(), view.getValue());
                })
                .take(1))
                .map(viewDescriptor -> {
                    String roleId = viewDescriptor.getView()
                            .getAccessRolesMap()
                            .keySet()
                            .stream()
                            .findFirst()
                            .orElseThrow();
                    String resourceId = viewDescriptor.getResourceId();
                    return getViewUri(damId, realm, resourceId, viewId, roleId);
                });
        Mono<String> accessTokenMono = wesUriMono
                .flatMap(wesResourceUri -> {
                    return checkoutResponse.map(resourceTokens -> {
                        ResourceTokens.Descriptor descriptor = resourceTokens.getResourcesMap()
                                .get(wesResourceUri.toString());
                        String accessId = descriptor.getAccess();
                        ResourceTokens.ResourceToken resourceToken = resourceTokens.getAccessMap()
                                .get(accessId);
                        return resourceToken.getAccessToken();
                    });
                });


        return accessTokenMono
                .flatMap(accessToken -> wesService.executeWorkflow(damClient, realm, accessToken, viewId, runRequest));
    }

    private URI getViewUri(String damId, String realm, String resourceId, String viewId, String roleId) {
        URI damBaseUrl = dams.get(damId).getBaseUrl();
        return damBaseUrl.resolve("/dam/" + realm + "/resources/" + resourceId + "/views/" + viewId + "/roles/" + roleId);
    }

    @GetMapping(value = "/{damId}/views/{viewId}/runs/{runId}")
    public Mono<WorkflowExecutionRunModel> getWorkflowRunDetails(@PathVariable String realm,
                                                                 @PathVariable String damId,
                                                                 @PathVariable String viewId,
                                                                 @PathVariable String runId,
                                                                 @RequestParam String accessToken) {
        // TODO: use damClientFactory
        Map.Entry<String, ReactiveDamClient> damClient = damClients.entrySet().stream()
                .filter(damClientEntry -> damClientEntry.getKey().equals(damId))
                .findFirst()
                .get();
        return wesService.getWorkflowRunDetails(damClient, realm, accessToken, viewId, runId);
    }

    private Mono<ResourceTokens> checkoutToken(ServerHttpRequest request, ReactiveDamClient damClient, Collection<URI> resources) {
        final UserTokenCookiePackager.CartTokenCookieName cartCookieName = new UserTokenCookiePackager.CartTokenCookieName(Set.copyOf(resources));
        final Optional<String> extractedCartToken = cookiePackager.extractTokenIgnoringInvalid(request, cartCookieName)
                .map(UserTokenCookiePackager.CookieValue::getClearText);

        return extractedCartToken
                .map(s -> damClient.checkoutCart(s))
                .orElseGet(() -> Mono.error(new AuthCookieNotPresentInRequestException(cartCookieName.cookieName())));
    }

}
