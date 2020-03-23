import { Component, Input, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { flatten } from 'ddap-common-lib';
import { combineLatest, merge, Observable, of } from 'rxjs';
import { flatMap, map } from 'rxjs/operators';

import { ResourceService } from '../../../shared/resource/resource.service';
import { DatasetService } from '../dataset.service';
import { WorkflowsStateService } from '../workflows-state.service';


@Component({
  selector: 'ddap-resource-authorization-step',
  templateUrl: './resource-authorization-step.component.html',
  styleUrls: ['./resource-authorization-step.component.scss'],
})
export class ResourceAuthorizationStepComponent implements OnInit {

  @Input()
  workflowId: string;
  @Input()
  selectedRows: Observable<object[]>;
  @Input()
  selectedColumns: Observable<string[]>;
  @Input()
  damIdWesResourcePathPair: Observable<string>;

  @Output()
  resourceAuthUrl: Observable<string>;

  constructor(private router: Router,
              private datasetService: DatasetService,
              private resourceService: ResourceService,
              private workflowsStateService: WorkflowsStateService) {
  }

  ngOnInit(): void {
    this.resourceAuthUrl = combineLatest([
      // Merge so there is always a fallback value
      // Workflows don't need to have selected rows or columns
      merge(of([]), this.selectedRows),
      merge(of([]), this.selectedColumns),
      this.damIdWesResourcePathPair,
    ])
      .pipe(
        flatMap(values => {
          const rows = values[0];
          const cols = values[1];
          const wesIdResPair = values[2];

          const columnData: string[] = this.extractColumnData(rows, cols);
          if (!columnData || columnData.length === 0) {
            return of(this.getUrlForObtainingAccessToken(wesIdResPair, []));
          } else {
            return this.datasetService.getViews(columnData)
              .pipe(
                map((views: { [p: string]: string[] }) => {
                  const resourcePaths: string[] = Object.values(views).reduce((l, r) => l.concat(r));
                  this.workflowsStateService.storeMetaInfoForWorkflow(this.workflowId, {
                    columnDataMappedToViews: views,
                  });
                  return this.getUrlForObtainingAccessToken(wesIdResPair, resourcePaths);
                })
              );
          }
        })
      );
  }

  private extractColumnData(selectedRows: object[], columnNames: string[]): string[] {
    return flatten(selectedRows
      .map((rowData) => columnNames.map((columnName) => rowData[columnName]))
    ).filter((columnData) => columnData);
  }

  private getUrlForObtainingAccessToken(wesIdResourcePathPair: string, resources: string[]): string {
      const redirectUri = this.getRedirectUrl();
      resources.push(wesIdResourcePathPair);
      return this.resourceService.getUrlForObtainingAccessToken(resources, redirectUri);
  }

  private getRedirectUrl(): string {
    let currentUrl = this.router.url;

    if (currentUrl.includes('?state=')) {
      currentUrl = currentUrl.split('?')[0];
    } else if (currentUrl.includes('?')) {
      return `${currentUrl}&state=${this.workflowId}`;
    }

    return `${currentUrl}?state=${this.workflowId}`;
  }

}
