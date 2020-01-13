import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { ResourceService } from '../../shared/resource/resource.service';
import { SimplifiedWesResourceViews } from '../workflow.model';
import { WorkflowService } from '../workflows.service';

@Component({
  selector: 'ddap-workflow-list-multi',
  templateUrl: './workflow-list-multi.component.html',
  styleUrls: ['./workflow-list-multi.component.scss'],
})
export class WorkflowListMultiComponent implements OnInit {

  wesResourceViews: SimplifiedWesResourceViews[];

  constructor(private router: Router,
              private resourceService: ResourceService,
              private workflowService: WorkflowService) {
  }

  ngOnInit(): void {
    this.workflowService.getAllWesViews()
      .subscribe((wesResourceViews: SimplifiedWesResourceViews[]) => {
        this.wesResourceViews = wesResourceViews;
      });
  }

  getResourceAuthUrl(damId: string, view: any) {
    const damIdResourcePathPair = `${damId};${view.resourcePath}`;
    const redirectUri = this.getRedirectUrl(damId, view.name);
    return this.resourceService.getUrlForObtainingAccessToken([damIdResourcePathPair], redirectUri);
  }

  private getRedirectUrl(damId: string, viewId: string): string {
    return `${this.router.url}/${damId}/views/${viewId}/runs`;
  }

}
