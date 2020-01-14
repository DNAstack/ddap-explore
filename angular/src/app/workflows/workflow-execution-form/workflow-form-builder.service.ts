import { Injectable } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FormValidators } from 'ddap-common-lib';
import _get from 'lodash.get';

@Injectable({
  providedIn: 'root',
})
export class WorkflowFormBuilder {

  constructor(private formBuilder: FormBuilder) {
  }

  buildDatasetForm(dataset?: any): FormGroup {
    return this.formBuilder.group({
      url: [_get(dataset, 'url'), [Validators.required, FormValidators.url]],
      selectedColumns: [_get(dataset, 'selectedColumns'), []],
      selectedRows: [_get(dataset, 'selectedRows'), [Validators.required]],
    });
  }

  buildWorkflowForm(workflow?: any): FormGroup {
    return this.formBuilder.group({
      wesView: [_get(workflow, 'wesView'), [Validators.required]],
      wesViewResourcePath: [_get(workflow, 'wesViewResourcePath'), [Validators.required]],
      wdl: [_get(workflow, 'wdl'), [Validators.required]],
      inputs: [_get(workflow, 'inputs'), [Validators.required]],
    });
  }

}
