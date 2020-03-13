import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { flatten } from 'ddap-common-lib';

import { ResourceService } from '../../../shared/resource/resource.service';
import { DatasetService } from '../dataset.service';
import { WorkflowsStateService } from '../workflows-state.service';


@Component({
  selector: 'ddap-resource-authorization-step',
  templateUrl: './resource-authorization-step.component.html',
  styleUrls: ['./resource-authorization-step.component.scss'],
})
export class ResourceAuthorizationStepComponent implements OnChanges {

  @Input()
  workflowId: string;
  @Input()
  selectedRows: object[];
  @Input()
  selectedColumns: string[];
  @Input()
  damIdWesResourcePathPair: string;

  resourceAuthUrl: string;

  constructor(private router: Router,
              private datasetService: DatasetService,
              private resourceService: ResourceService,
              private workflowsStateService: WorkflowsStateService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.getViewsAndSetResourceAuthUrl();
  }

  getViewsAndSetResourceAuthUrl() {
    if (!this.selectedRows || !this.selectedColumns) {
      this.resourceAuthUrl = this.getUrlForObtainingAccessToken();
      return;
    }

    const columnData: string[] = this.extractColumnData(this.selectedRows, this.selectedColumns);
    this.datasetService.getViews(columnData)
      .subscribe((views: { [p: string]: string[] }) => {
        if (Object.values(views).length === 0) {
          console.warn('No views associated to the selected columns');
          return;
        }
        const resourcePaths: string[] = Object.values(views).reduce((l, r) => l.concat(r));
        this.workflowsStateService.storeMetaInfoForWorkflow(this.workflowId, {
          columnDataMappedToViews: views,
        });
        this.resourceAuthUrl = this.getUrlForObtainingAccessToken(resourcePaths);
      });
  }

  private extractColumnData(selectedRows: object[], columnNames: string[]): string[] {
    return flatten(selectedRows
      .map((rowData) => columnNames.map((columnName) => rowData[columnName]))
    ).filter((columnData) => columnData);
  }

  private getUrlForObtainingAccessToken(resources: string[] = []): string {
    const redirectUri = this.getRedirectUrl();
    resources.push(this.damIdWesResourcePathPair);
    return this.resourceService.getUrlForObtainingAccessToken(resources, redirectUri);
  }

  private getRedirectUrl(): string {
    let currentUrl = this.router.url;

    if (currentUrl.includes('?state=')) {
      currentUrl = currentUrl.split('?')[0];
    }

    return `${currentUrl}?state=${this.workflowId}`;
  }

}
