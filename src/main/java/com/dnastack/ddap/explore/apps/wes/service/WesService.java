package com.dnastack.ddap.explore.apps.wes.service;

import com.dnastack.ddap.common.client.ReactiveDamClient;
import com.dnastack.ddap.explore.apps.wes.client.ReactiveWesClient;
import com.dnastack.ddap.explore.apps.wes.model.WesResourceViews;
import com.dnastack.ddap.explore.apps.wes.model.WorkflowExecutionRunModel;
import com.dnastack.ddap.explore.apps.wes.model.WorkflowExecutionRunRequestModel;
import com.dnastack.ddap.explore.apps.wes.model.WorkflowExecutionRunsResponseModel;
import dam.v1.DamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

import static dam.v1.DamService.Resource;
import static dam.v1.DamService.View;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@Slf4j
@Component
public class WesService {

    private WesResourceService wesResourceService;
    private ReactiveWesClient wesClient;

    @Autowired
    public WesService(WesResourceService wesResourceService, ReactiveWesClient wesClient) {
        this.wesResourceService = wesResourceService;
        this.wesClient = wesClient;
    }

    public Mono<WorkflowExecutionRunsResponseModel> getAllWorkflowRunsFromWesServer(Map.Entry<String, ReactiveDamClient> damClient,
                                                                                    String realm,
                                                                                    String wesAccessToken,
                                                                                    String viewId,
                                                                                    String nextPage) {
        return wesView(damClient, realm, viewId)
                .flatMap(wesResourceViews -> {
                    Map.Entry<String, DamService.View> view = wesResourceService.getViewById(wesResourceViews, viewId);
                    return getAllWorkflowRunsFromWesServer(damClient, wesAccessToken, view, wesResourceViews.getResource(), nextPage);
                });
    }

    private Mono<WorkflowExecutionRunsResponseModel> getAllWorkflowRunsFromWesServer(Map.Entry<String, ReactiveDamClient> damClient,
                                                                                     String wesAccessToken,
                                                                                     Map.Entry<String, View> view,
                                                                                     Map.Entry<String, Resource> resource,
                                                                                     String nextPage) {
        URI wesServerUri = wesResourceService.getWesServerUri(view.getValue());
        return wesClient.getRuns(wesServerUri, wesAccessToken, nextPage)
                .doOnSuccess((runsResponse) -> setWorkflowRunMetadata(damClient.getKey(), view, resource, runsResponse))
                .onErrorResume(throwable -> {
                    log.error("Failed to load workflow runs with error", throwable);
                    return Mono.just(buildWorkflowRunMetadata(throwable, view, resource));
                });
    }

    private void setWorkflowRunMetadata(String damId,
                                        Map.Entry<String, View> view,
                                        Map.Entry<String, Resource> resource,
                                        WorkflowExecutionRunsResponseModel runsResponse) {
        WorkflowExecutionRunsResponseModel.WorkflowUi ui = new WorkflowExecutionRunsResponseModel.WorkflowUi();
        ui.setResource(resource.getValue().getUiOrDefault("label", resource.getKey()));
        ui.setView(view.getValue().getUiOrDefault("label", view.getKey()));
        runsResponse.setDamId(damId);
        runsResponse.setResourceId(resource.getKey());
        runsResponse.setViewId(view.getKey());
        runsResponse.setUi(ui);
    }

    private WorkflowExecutionRunsResponseModel buildWorkflowRunMetadata(Throwable throwable,
                                                                        Map.Entry<String, View> view,
                                                                        Map.Entry<String, Resource> resource) {
        WorkflowExecutionRunsResponseModel.WorkflowUi ui = new WorkflowExecutionRunsResponseModel.WorkflowUi();
        ui.setResource(resource.getValue().getUiOrDefault("label", resource.getKey()));
        ui.setView(view.getValue().getUiOrDefault("label", view.getKey()));
        WorkflowExecutionRunsResponseModel runsResponse = new WorkflowExecutionRunsResponseModel();
        WorkflowExecutionRunsResponseModel.WorkflowRequestError error = new WorkflowExecutionRunsResponseModel.WorkflowRequestError();
        error.setMessage(throwable.getMessage());
        runsResponse.setError(error);
        runsResponse.setUi(ui);
        return runsResponse;
    }

    public Mono<WorkflowExecutionRunModel> executeWorkflow(Map.Entry<String, ReactiveDamClient> damClient,
                                                           String realm,
                                                           String wesAccessToken,
                                                           String viewId,
                                                           WorkflowExecutionRunRequestModel runRequest) {
        return wesView(damClient, realm, viewId)
                .flatMap(wesResourceViews -> executeWorkflow(viewId, wesAccessToken, runRequest, wesResourceViews));
    }

    private Mono<WesResourceViews> wesView(Map.Entry<String, ReactiveDamClient> damClient,
                                           String realm,
                                           String viewId) {
        Flux<WesResourceViews> wesResources = wesResourceService.getResources(damClient, realm);
        return wesResources
                .filter(wesResourceViews -> wesResourceViews.getViews().stream()
                        .anyMatch(stringViewEntry -> stringViewEntry.getKey().equals(viewId)))
                .single();
    }

    public Mono<WorkflowExecutionRunModel> getWorkflowRunDetails(Map.Entry<String, ReactiveDamClient> damClient,
                                                                          String realm,
                                                                 String wesAccessToken,
                                                                          String viewId,
                                                                          String runId) {
        return wesView(damClient, realm, viewId)
                .flatMap(wesResourceViews -> workflowRunDetails(viewId, runId, wesAccessToken, wesResourceViews));
    }

    private Mono<WorkflowExecutionRunModel> workflowRunDetails(String viewId,
                                                               String runId,
                                                               String wesAccessToken,
                                                               WesResourceViews wesResourceViews) {
        Map.Entry<String, DamService.View> view = wesResourceService.getViewById(wesResourceViews, viewId);
        URI wesServerUri = wesResourceService.getWesServerUri(view.getValue());
        return wesClient.getRun(wesServerUri, wesAccessToken, runId);
    }

    private Mono<WorkflowExecutionRunModel> executeWorkflow(String viewId,
                                                            String wesAccessToken,
                                                            WorkflowExecutionRunRequestModel runRequest,
                                                            WesResourceViews wesResourceViews) {
        Map.Entry<String, DamService.View> view = wesResourceService.getViewById(wesResourceViews, viewId);
        URI wesServerUri = wesResourceService.getWesServerUri(view.getValue());
        return wesClient.addRun(wesServerUri, wesAccessToken, getMultipartBody(runRequest));
    }

    private MultiValueMap<String, HttpEntity<?>> getMultipartBody(WorkflowExecutionRunRequestModel runRequest) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("workflow_url", "workflow.wdl")
                .header(CONTENT_DISPOSITION, "form-data; name=\"workflow_url\"");
        builder.part("workflow_params", runRequest.getInputsJson(), MediaType.APPLICATION_JSON_UTF8)
                .header(CONTENT_DISPOSITION, "form-data; name=\"workflow_params\"; filename=\"inputs.json\"");
        builder.part("workflow_attachment", runRequest.getWdl(), MediaType.TEXT_PLAIN)
                .header(CONTENT_DISPOSITION, "form-data; name=\"workflow_attachment\"; filename=\"workflow.wdl\"");
        builder.part("workflow_attachment", runRequest.getTokensJson(), MediaType.APPLICATION_JSON_UTF8)
                .header(CONTENT_DISPOSITION, "form-data; name=\"workflow_attachment\"; filename=\"tokens.json\"");
        return builder.build();
    }

}
