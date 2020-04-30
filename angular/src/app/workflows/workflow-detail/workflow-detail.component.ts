import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { JsonEditorComponent, JsonEditorOptions } from 'ang-jsoneditor';
import { flatDeep } from 'ddap-common-lib';
import { map } from 'rxjs/operators';

import { JsonEditorDefaults } from '../../shared/jsonEditorDefaults';
import { dam } from '../../shared/proto/dam-service';
import { ResourceAuthStateService } from '../../shared/resource-auth-state.service';
import { ResourceService } from '../../shared/resource/resource.service';
import { DatasetService } from '../workflow-execution-form/dataset.service';
import { SimplifiedWesResourceViews } from '../workflow.model';
import { WorkflowService } from '../workflows.service';
import IResourceAccess = dam.v1.ResourceResults.IResourceAccess;

@Component({
  selector: 'ddap-workflow-detail',
  templateUrl: './workflow-detail.component.html',
  styleUrls: ['./workflow-detail.component.scss'],
})
export class WorkflowDetailComponent implements OnInit {

  runDetails;
  runDetailsResponse;
  editorOptions: JsonEditorOptions | any;
  resourceAccess: IResourceAccess;
  fileResourceAuthUrl: string;

  viewAccessible: boolean;

  @ViewChild(JsonEditorComponent, { static: false })
  editor: JsonEditorComponent;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private workflowService: WorkflowService,
              private activatedRoute: ActivatedRoute,
              private datasetService: DatasetService,
              private resourceService: ResourceService,
              private resourceAuthStateService: ResourceAuthStateService) {
    this.editorOptions = new JsonEditorDefaults();
  }

  ngOnInit() {
    this.initialize();
  }

  private initialize() {
    const {damId, viewId, runId} = this.activatedRoute.snapshot.params;
    this.workflowService.getAllWesViews()
      .subscribe((wesResourceViews: SimplifiedWesResourceViews[]) => {
        const resourcePath = this.workflowService.getResourcePathForView(damId, viewId, wesResourceViews);
        const resourceTokens = this.resourceAuthStateService.getAccess();
        this.resourceAccess = this.resourceService.lookupResourceTokenFromAccessMap(resourceTokens, resourcePath);

        if (!this.resourceAccess) {
          this.viewAccessible = false;
          return;
        }

        this.workflowService.workflowRunDetail(damId, viewId, runId, this.resourceAccess.credentials['access_token'])
          .subscribe(runDetails => {
            this.runDetails = this.runDetailsResponse = runDetails;
            const gcsUrl = this.getFlatValues(runDetails)
              .filter(value => value !== undefined)
              .find((value: string) => value.includes('gs://'));
            this.datasetService.getViews([gcsUrl])
              .subscribe((views) => {
                if (!views) {
                  return;
                }

                this.viewAccessible = true;

                const uniqueViews = Object.values(views);

                if (uniqueViews.length === 0) {
                  return;
                }

                const damIdResourcePathPairs: string[] = uniqueViews.reduce((l, r) => l.concat(r));
                this.fileResourceAuthUrl = this.getUrlForObtainingAccessToken(damIdResourcePathPairs);
              });
          });
      });

    this.route.queryParams
      .subscribe(params => {
        if (!params.resource) {
          return;
        }
        const damIdResourcePathPair = params.resource;
        this.resourceService.getAccessTokensForAuthorizedResources([damIdResourcePathPair])
          .pipe(
            map(this.resourceService.toResourceAccessMap)
          )
          .subscribe((access) => {
            this.resourceAccess = this.resourceService.lookupResourceTokenFromAccessMap(access, damIdResourcePathPair.split(';')[1]);
            this.runDetails = this.transformResponse(this.runDetailsResponse);
          });
      });
  }

  private getFlatValues(runDetails: any): string[] {
    return flatDeep(Object.keys(runDetails)
      .map(key => {
        if (typeof runDetails[key] === 'object') {
          return flatDeep(this.getFlatValues(runDetails[key]));
        }
        if (typeof runDetails[key] === 'string') {
          return runDetails[key];
        }
      })
    );
  }

  private transformResponse(runDetails: any) {
    Object.keys(runDetails).forEach(key => {
      if (typeof runDetails[key] === 'object') {
        return this.transformResponse(runDetails[key]);
      }
      if (typeof runDetails[key] === 'string') {
        const gcsBaseUrl = 'https://storage.cloud.google.com/';
        runDetails[key] = runDetails[key].replace('gs://', gcsBaseUrl);
        if (runDetails[key].includes(gcsBaseUrl)) {
          runDetails[key] = `${runDetails[key]}/o?access_token=${this.resourceAccess.credentials['access_token']}`;
        }
      }
    });
    return runDetails;
  }

  private getUrlForObtainingAccessToken(resources: string[]): string {
    const redirectUri = this.getRedirectUrl(resources);
    return this.resourceService.getUrlForObtainingAccessToken(resources, redirectUri);
  }

  private getRedirectUrl(damIdResourcePathPairs: string[]): string {
    const currentUrl = this.router.url;
    return `${currentUrl}?resource=${damIdResourcePathPairs[0]}`; // assuming that there is just one view
  }

}
