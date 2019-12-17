package com.dnastack.ddap.explore.wes.controller;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.common.security.UserTokenCookiePackager;
import com.dnastack.ddap.explore.wes.client.ReactiveWdlValidatorClient;
import com.dnastack.ddap.explore.wes.model.WesResourceViews;
import com.dnastack.ddap.explore.wes.model.WorkflowExecutionRunModel;
import com.dnastack.ddap.explore.wes.model.WorkflowExecutionRunRequestModel;
import com.dnastack.ddap.explore.wes.model.WorkflowExecutionRunsResponseModel;
import com.dnastack.ddap.explore.wes.service.WesResourceService;
import com.dnastack.ddap.explore.wes.service.WesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/{realm}/wes")
public class WorkflowController {

    private UserTokenCookiePackager cookiePackager;
    private WesResourceService wesResourceService;
    private WesService wesService;

    private ReactiveWdlValidatorClient wdlValidatorClient;
    private Map<String, ReactiveDamClient> damClients;

    @Autowired
    public WorkflowController(UserTokenCookiePackager cookiePackager,
                              ReactiveWdlValidatorClient wdlValidatorClient,
                              Map<String, ReactiveDamClient> damClients,
                              WesResourceService wesResourceService,
                              WesService wesService) {
        this.cookiePackager = cookiePackager;
        this.wdlValidatorClient = wdlValidatorClient;
        this.wesResourceService = wesResourceService;
        this.wesService = wesService;
        this.damClients = damClients;
    }

    @PostMapping(value = "/describe")
    public Mono<Object> getJsonSchemaFromWdl(@PathVariable String realm, @RequestBody String wdl) {
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
    public Mono<WorkflowExecutionRunsResponseModel> getWorkflowRuns(ServerHttpRequest request,
                                                                    @PathVariable String realm,
                                                                    @PathVariable String damId,
                                                                    @PathVariable String viewId,
                                                                    @RequestParam(required = false) String nextPage) {
        Optional<UserTokenCookiePackager.CookieValue> foundDamToken = cookiePackager.extractToken(request, UserTokenCookiePackager.CookieKind.DAM);
        Optional<UserTokenCookiePackager.CookieValue> foundRefreshToken = cookiePackager.extractToken(request, UserTokenCookiePackager.CookieKind.REFRESH);
        UserTokenCookiePackager.CookieValue damToken = foundDamToken.orElseThrow(() -> new IllegalArgumentException("Authorization dam token is required."));
        UserTokenCookiePackager.CookieValue refreshToken = foundRefreshToken.orElseThrow(() -> new IllegalArgumentException("Authorization refresh token is required."));

        // TODO: use damClientFactory
        Map.Entry<String, ReactiveDamClient> damClient = damClients.entrySet().stream()
                .filter(damClientEntry -> damClientEntry.getKey().equals(damId))
                .findFirst()
                .get();
        return wesService.getAllWorkflowRunsFromWesServer(damClient, realm, damToken.getClearText(), refreshToken.getClearText(), viewId, nextPage);
    }

    @PostMapping(value = "/{damId}/views/{viewId}/runs")
    public Mono<WorkflowExecutionRunModel> addWorkflowToRun(ServerHttpRequest request,
                                                            @PathVariable String realm,
                                                            @PathVariable String damId,
                                                            @PathVariable String viewId,
                                                            @RequestBody WorkflowExecutionRunRequestModel runRequest) {
        Optional<UserTokenCookiePackager.CookieValue> foundDamToken = cookiePackager.extractToken(request, UserTokenCookiePackager.CookieKind.DAM);
        Optional<UserTokenCookiePackager.CookieValue> foundRefreshToken = cookiePackager.extractToken(request, UserTokenCookiePackager.CookieKind.REFRESH);
        UserTokenCookiePackager.CookieValue damToken = foundDamToken.orElseThrow(() -> new IllegalArgumentException("Authorization dam token is required."));
        UserTokenCookiePackager.CookieValue refreshToken = foundRefreshToken.orElseThrow(() -> new IllegalArgumentException("Authorization refresh token is required."));

        // TODO: use damClientFactory
        Map.Entry<String, ReactiveDamClient> damClient = damClients.entrySet().stream()
                .filter(damClientEntry -> damClientEntry.getKey().equals(damId))
                .findFirst()
                .get();
        return wesService.executeWorkflow(damClient, realm, damToken.getClearText(), refreshToken.getClearText(), viewId, runRequest);
    }

    @GetMapping(value = "/{damId}/views/{viewId}/runs/{runId}")
    public Mono<WorkflowExecutionRunModel> getWorkflowRunDetails(ServerHttpRequest request,
                                                                          @PathVariable String realm,
                                                                          @PathVariable String damId,
                                                                          @PathVariable String viewId,
                                                                          @PathVariable String runId) {

        Optional<UserTokenCookiePackager.CookieValue> foundDamToken = cookiePackager.extractToken(request, UserTokenCookiePackager.CookieKind.DAM);
        Optional<UserTokenCookiePackager.CookieValue> foundRefreshToken = cookiePackager.extractToken(request, UserTokenCookiePackager.CookieKind.REFRESH);
        UserTokenCookiePackager.CookieValue damToken = foundDamToken.orElseThrow(() -> new IllegalArgumentException("Authorization dam token is required."));
        UserTokenCookiePackager.CookieValue refreshToken = foundRefreshToken.orElseThrow(() -> new IllegalArgumentException("Authorization refresh token is required."));

        // TODO: use damClientFactory
        Map.Entry<String, ReactiveDamClient> damClient = damClients.entrySet().stream()
                .filter(damClientEntry -> damClientEntry.getKey().equals(damId))
                .findFirst()
                .get();
        return wesService.getWorkflowRunDetails(damClient, realm, damToken.getClearText(), refreshToken.getClearText(), viewId, runId);
    }

}
