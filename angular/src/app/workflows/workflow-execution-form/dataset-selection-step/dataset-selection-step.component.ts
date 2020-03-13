import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import _sampleSize from 'lodash.samplesize';

import { dam } from '../../../shared/proto/dam-service';
import { ResourceAuthStateService } from '../../../shared/resource-auth-state.service';
import { ResourceService } from '../../../shared/resource/resource.service';
import { DatasetService } from '../dataset.service';
import { WorkflowsStateService } from '../workflows-state.service';

import { Dataset } from './dataset.model';
import IResourceAccess = dam.v1.ResourceResults.IResourceAccess;

@Component({
  selector: 'ddap-dataset-selection-step',
  templateUrl: './dataset-selection-step.component.html',
  styleUrls: ['./dataset-selection-step.component.scss'],
})
export class DatasetSelectionStepComponent {

  get datasetUrl() {
    return this.currentDatasetUrl
           ? this.currentDatasetUrl
           : this.form.get('url').value;
  }

  @Input()
  form: FormGroup;
  @Input()
  workflowId: string;
  @Output()
  readonly datasetColumnsChanged: EventEmitter<string[]> = new EventEmitter<string[]>();

  dataset: Dataset;
  currentDatasetUrl: string;
  datasetResourceAuthUrl: string;
  resourceAccess: IResourceAccess;
  error: any;

  constructor(private formBuilder: FormBuilder,
              private router: Router,
              private resourceService: ResourceService,
              private datasetService: DatasetService,
              private workflowsStateService: WorkflowsStateService,
              private resourceAuthStateService: ResourceAuthStateService) {
  }

  fetchDataset(url: string) {
    this.setResourceToken();
    this.datasetService.fetchDataset(url, this.resourceAccess ? this.resourceAccess.credentials['access_token'] : '')
      .subscribe((dataset) => {
        this.dataset = dataset;
        this.datasetColumnsChanged.emit(this.getDatasetColumns());
      }, (error) => {
        this.error = error;
        this.dataset = null;

        if (error.status === 403) {
          this.datasetService.getViews([url])
            .subscribe((views) => {
              if (!views) {
                this.error.message = 'No views associated with dataset';
              }
              const damIdResourcePathPairs: string[] = Object.values(views)
                .reduce((l, r) => l.concat(r));
              const workflowMetaInfo = this.workflowsStateService.getMetaInfoForWorkflow(this.workflowId);
              workflowMetaInfo.datasetDamIdResourcePathPairs = damIdResourcePathPairs;
              this.workflowsStateService.storeMetaInfoForWorkflow(this.workflowId, workflowMetaInfo);
              this.datasetResourceAuthUrl = this.getUrlForObtainingAccessToken(damIdResourcePathPairs);
            });
        }
      });
  }

  pageChange(relativeUrl) {
    const { href: newDatasetUrl } = new URL(relativeUrl, this.datasetUrl);
    this.currentDatasetUrl = newDatasetUrl;
    this.fetchDataset(this.currentDatasetUrl);
  }

  dataSelectionChange(selection) {
    this.form.get('selectedRows').setValue(selection);
  }

  getFileOnlyColumns() {
    const columns: string[] = this.getDatasetColumns();
    const samples: object[] = _sampleSize(this.dataset.data, 15);

    const fileColumns: string[] = columns.filter((column) => {
      return samples.some((sample) => {
        const value = sample[column];
        return value && value.startsWith('gs://');
      });
    });
    if (fileColumns.length > 0) {
      this.form.get('selectedColumns').setValidators(Validators.required);
    }

    return fileColumns;
  }

  useExample(datasetUrl: string) {
    this.form.patchValue({ url: datasetUrl });
    this.fetchDataset(datasetUrl);
  }

  private setResourceToken() {
    const { datasetDamIdResourcePathPairs } = this.workflowsStateService.getMetaInfoForWorkflow(this.workflowId);
    if (datasetDamIdResourcePathPairs) {
      const resourceTokens = this.resourceAuthStateService.getAccess();
      const datasetResourcePath = datasetDamIdResourcePathPairs[0].split(';')[1];
      this.resourceAccess = this.resourceService.lookupResourceTokenFromAccessMap(resourceTokens, datasetResourcePath);
    }
  }

  private getUrlForObtainingAccessToken(resources: string[]): string {
    const redirectUri = this.getRedirectUrl();
    return this.resourceService.getUrlForObtainingAccessToken(resources, redirectUri);
  }

  private getRedirectUrl(): string {
    let currentUrl = this.router.url;
    if (currentUrl.includes('?state=')) {
      currentUrl = currentUrl.split('?')[0];
    }
    return `${currentUrl}?state=${this.workflowId}`;
  }

  private getDatasetColumns() {
    let schemaProperties = {};
    const schemaObj = this.dataset.data_model;
    if (schemaObj.hasOwnProperty('schema')) {
      schemaProperties = schemaObj.schema.properties;
    } else {
      schemaProperties = schemaObj.properties;
    }
    return Object.keys(schemaProperties);
  }

}
