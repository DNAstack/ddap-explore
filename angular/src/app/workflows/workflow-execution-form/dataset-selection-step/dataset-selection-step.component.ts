import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import _sampleSize from 'lodash.samplesize';

import { DatasetService } from '../dataset.service';

import { Dataset } from './dataset.model';


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
  @Output()
  readonly datasetColumnsChanged: EventEmitter<string[]> = new EventEmitter<string[]>();

  dataset: Dataset;
  currentDatasetUrl: string;
  error: string;

  constructor(private formBuilder: FormBuilder,
              private datasetService: DatasetService) {
  }

  fetchDataset(url: string) {
    this.datasetService.fetchDataset(url)
      .subscribe((dataset) => {
        this.dataset = dataset;
        this.datasetColumnsChanged.emit(this.getDatasetColumns());
      }, () => {
        this.dataset = null;
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
    const samples: object[] = _sampleSize(this.dataset.objects, 15);

    return columns.filter((column) => {
      return samples.some((sample) => {
        const value = sample[column];
        return value && value.startsWith('gs://');
      });
    });
  }

  useExample(datasetUrl: string) {
    this.form.patchValue({ url: datasetUrl });
    this.fetchDataset(datasetUrl);
  }

  private getDatasetColumns() {
    let schemaProperties = {};
    const schemaObj = this.dataset.schema;
    if (schemaObj.hasOwnProperty('schema')) {
      schemaProperties = schemaObj.schema.properties;
    } else {
      schemaProperties = schemaObj.properties;
    }
    return Object.keys(schemaProperties);
  }

}
