import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { map } from 'rxjs/operators';

import { PaginationTypes } from '../../shared/paginator/pagination-type.enum';
import { dam } from '../../shared/proto/dam-service';
import IResourceToken = dam.v1.ResourceTokens.IResourceToken;
import { ResourceAuthStateService } from '../../shared/resource-auth-state.service';
import { ResourceService } from '../../shared/resource/resource.service';
import { SimplifiedWesResourceViews, WorkflowRunsResponse } from '../workflow.model';
import { WorkflowService } from '../workflows.service';

@Component({
  selector: 'ddap-workflow-list-single',
  templateUrl: './workflow-list-single.component.html',
  styleUrls: ['./workflow-list-single.component.scss'],
})
export class WorkflowListSingleComponent implements OnInit {

  resourceToken: IResourceToken;
  workflowRunsResponse: WorkflowRunsResponse;
  newlyCreatedWorkflows?: any[];
  paginationType = PaginationTypes.unidirectional;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private resourceService: ResourceService,
              private workflowService: WorkflowService,
              private resourceAuthStateService: ResourceAuthStateService) {
    const navigation = this.router.getCurrentNavigation();
    if (navigation && navigation.extras.state) {
      this.newlyCreatedWorkflows = navigation.extras.state.runs;
    }
  }

  ngOnInit(): void {
    const { damId, viewId } = this.route.snapshot.params;

    this.workflowService.getAllWesViews()
      .subscribe((wesResourceViews: SimplifiedWesResourceViews[]) => {
        const resourcePath = this.workflowService.getResourcePathForView(damId, viewId, wesResourceViews);
        const damIdResourcePathPair = `${damId};${resourcePath}`;

        const accessMap = this.resourceAuthStateService.getAccess();
        if (accessMap && Object.keys(accessMap).length > 0) {
          this.resourceToken = this.resourceService.lookupResourceTokenFromAccessMap(accessMap, resourcePath);
          this.getWorkflows(this.resourceToken['access_token']);
        } else {
          this.getAccessTokensForAuthorizedResources(damIdResourcePathPair)
            .pipe(
              map(this.resourceService.toResourceAccessMap)
            )
            .subscribe((response) => {
              this.resourceAuthStateService.storeAccess(response);
              this.resourceToken = this.resourceService.lookupResourceTokenFromAccessMap(response, resourcePath);
              this.getWorkflows(this.resourceToken['access_token']);
            });
        }
      });
  }

  getWorkflows(accessToken: string, pageToken?: string) {
    const { damId, viewId } = this.route.snapshot.params;
    this.workflowService.getWorkflowRuns(damId, viewId, accessToken, pageToken)
      .subscribe((workflowRunsResponse: WorkflowRunsResponse) => {
        this.workflowRunsResponse = workflowRunsResponse;
      });
  }

  private getAccessTokensForAuthorizedResources(damIdResourcePathPair: string) {
    return this.resourceService.getAccessTokensForAuthorizedResources([damIdResourcePathPair]);
  }

}
