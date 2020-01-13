import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { JsonEditorComponent, JsonEditorOptions } from 'ang-jsoneditor';
import IResourceToken = dam.v1.ResourceTokens.IResourceToken;
import { map } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import { JsonEditorDefaults } from '../../shared/jsonEditorDefaults';
import { dam } from '../../shared/proto/dam-service';
import { ResourceAuthStateService } from '../../shared/resource-auth-state.service';
import { ResourceService } from '../../shared/resource/resource.service';
import { SimplifiedWesResourceViews } from '../workflow.model';
import { WorkflowService } from '../workflows.service';

@Component({
  selector: 'ddap-workflow-detail',
  templateUrl: './workflow-detail.component.html',
  styleUrls: ['./workflow-detail.component.scss'],
})
export class WorkflowDetailComponent implements OnInit {

  runDetails;
  editorOptions: JsonEditorOptions | any;

  @ViewChild(JsonEditorComponent, { static: false })
  editor: JsonEditorComponent;

  constructor(private workflowService: WorkflowService,
              private activatedRoute: ActivatedRoute,
              private resourceService: ResourceService,
              private resourceAuthStateService: ResourceAuthStateService) {
    this.editorOptions = new JsonEditorDefaults();
  }

  ngOnInit() {
    const {damId, viewId, runId} = this.activatedRoute.snapshot.params;
    this.workflowService.getAllWesViews()
      .subscribe((wesResourceViews: SimplifiedWesResourceViews[]) => {
        const resourcePath = this.workflowService.getResourcePathForView(damId, viewId, wesResourceViews);
        const resourceTokens = this.resourceAuthStateService.getAccess();
        const resourceToken = this.resourceService.lookupResourceToken(resourceTokens, resourcePath);

        this.workflowService.workflowRunDetail(damId, viewId, runId, resourceToken['access_token'])
          .subscribe(runDetails => {
            this.runDetails = this.transformResponse(runDetails);
          });
      });
  }

  private transformResponse(runDetails: any) {
    const realmId = this.activatedRoute.root.firstChild.snapshot.params.realmId;
    Object.keys(runDetails).forEach(key => {
      if (typeof runDetails[key] === 'object') {
        return this.transformResponse(runDetails[key]);
      }
      if (typeof runDetails[key] === 'string') {
        runDetails[key] = runDetails[key]
          .replace('gs://', `${window.location.origin}/api/v1alpha/${realmId}/access/gcs/`);
      }
    });
    return runDetails;
  }

}
