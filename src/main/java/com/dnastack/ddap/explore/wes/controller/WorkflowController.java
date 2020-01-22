package com.dnastack.ddap.explore.wes.controller;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.explore.wes.client.ReactiveWdlValidatorClient;
import com.dnastack.ddap.explore.wes.model.WesResourceViews;
import com.dnastack.ddap.explore.wes.model.WorkflowExecutionRunModel;
import com.dnastack.ddap.explore.wes.model.WorkflowExecutionRunRequestModel;
import com.dnastack.ddap.explore.wes.model.WorkflowExecutionRunsResponseModel;
import com.dnastack.ddap.explore.wes.service.WesResourceService;
import com.dnastack.ddap.explore.wes.service.WesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static java.util.stream.Collectors.toList;

@Slf4j
@RestController
@RequestMapping("/api/v1alpha/realm/{realm}/wes")
public class WorkflowController {

    private WesResourceService wesResourceService;
    private WesService wesService;

    private ReactiveWdlValidatorClient wdlValidatorClient;
    private Map<String, ReactiveDamClient> damClients;

    @Autowired
    public WorkflowController(ReactiveWdlValidatorClient wdlValidatorClient,
                              Map<String, ReactiveDamClient> damClients,
                              WesResourceService wesResourceService,
                              WesService wesService) {
        this.wdlValidatorClient = wdlValidatorClient;
        this.wesResourceService = wesResourceService;
        this.wesService = wesService;
        this.damClients = damClients;
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

    @PostMapping(value = "/{damId}/views/{viewId}/runs")
    public Mono<WorkflowExecutionRunModel> addWorkflowToRun(@PathVariable String realm,
                                                            @PathVariable String damId,
                                                            @PathVariable String viewId,
                                                            @RequestParam String accessToken,
                                                            @RequestBody WorkflowExecutionRunRequestModel runRequest) {
        // TODO: use damClientFactory
        Map.Entry<String, ReactiveDamClient> damClient = damClients.entrySet().stream()
                                                                   .filter(damClientEntry -> damClientEntry.getKey().equals(damId))
                                                                   .findFirst()
                                                                   .get();
        return wesService.executeWorkflow(damClient, realm, accessToken, viewId, runRequest);
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

}
