import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { PaginationTypes } from '../../shared/paginator/pagination-type.enum';
import { dam } from '../../shared/proto/dam-service';
import { ResourceAuthStateService } from '../../shared/resource-auth-state.service';
import { ResourceService } from '../../shared/resource/resource.service';
import { SimplifiedWesResourceViews, WorkflowRunsResponse } from '../workflow.model';
import { WorkflowService } from '../workflows.service';
import IResourceAccess = dam.v1.ResourceResults.IResourceAccess;

@Component({
  selector: 'ddap-workflow-list-single',
  templateUrl: './workflow-list-single.component.html',
  styleUrls: ['./workflow-list-single.component.scss'],
})
export class WorkflowListSingleComponent implements OnInit {

  resourceAccess: IResourceAccess;
  workflowRunsResponse: WorkflowRunsResponse;
  newlyCreatedWorkflows?: any[];
  paginationType = PaginationTypes.unidirectional;
  viewAccessible: boolean;

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
      .pipe(
        catchError((response: HttpErrorResponse) => {
          this.viewAccessible = false;
          return throwError(`Unable to fetch the WES views`);
        })
      )
      .subscribe((wesResourceViews: SimplifiedWesResourceViews[]) => {
        const resourcePath = this.workflowService.getResourcePathForView(damId, viewId, wesResourceViews);
        const damIdResourcePathPair = `${damId};${resourcePath}`;

        const accessMap = this.resourceAuthStateService.getAccess();

        if (accessMap && Object.keys(accessMap).length > 0) {
          this.resourceAccess = this.resourceService.lookupResourceTokenFromAccessMap(accessMap, resourcePath);
          this.getWorkflows(this.resourceAccess.credentials['access_token']);
          this.viewAccessible = true;
        } else {
          this.getAccessTokensForAuthorizedResources(damIdResourcePathPair)
            .pipe(
              catchError((response: HttpErrorResponse) => {
                this.viewAccessible = false;
                return throwError(`Unable to check out the necessary access tokens`);
              }),
              map(this.resourceService.toResourceAccessMap)
            )
            .subscribe((response) => {
              this.resourceAuthStateService.storeAccess(response);
              this.resourceAccess = this.resourceService.lookupResourceTokenFromAccessMap(response, resourcePath);
              this.getWorkflows(this.resourceAccess['access_token']);
              this.viewAccessible = true;
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
