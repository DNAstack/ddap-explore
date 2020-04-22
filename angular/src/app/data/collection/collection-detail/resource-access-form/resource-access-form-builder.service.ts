import { Injectable } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import _get from 'lodash.get';

import { defaultState, ResourceAccessFormModel } from './resource-access-form.model';

@Injectable({
  providedIn: 'root',
})
export class ResourceAccessFormBuilder {

  constructor(private formBuilder: FormBuilder) {
  }

  buildForm(state?: ResourceAccessFormModel): FormGroup {
    return this.formBuilder.group({
      ttl: this.formBuilder.group({
        numericValue: [_get(state, 'ttl.numericValue', defaultState.ttl.numericValue), [Validators.required, Validators.min(1)]],
        timeUnit: [_get(state, 'ttl.timeUnit', defaultState.ttl.timeUnit), [Validators.required]],
      }),
      interfaceType: [_get(state, 'interfaceType', defaultState.interfaceType), [Validators.required]],
    });
  }

}
