import { Injectable } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import _get from 'lodash.get';

import { BeaconQueryAlleleRequestModel } from '../../../shared/beacon/beacon-search.model';

import { defaultState } from './beacon-search-form.model';

@Injectable({
  providedIn: 'root',
})
export class BeaconSearchFormBuilder {

  constructor(private formBuilder: FormBuilder) {
  }

  buildForm(isVirusInterface: boolean, sampleQueryRequest?: BeaconQueryAlleleRequestModel): FormGroup {
    return this.formBuilder.group({
      assemblyId: [
        this.getAssembly(isVirusInterface, sampleQueryRequest),
        isVirusInterface ? [] : [Validators.required],
      ],
      referenceName: [
        _get(sampleQueryRequest, 'referenceName', defaultState.referenceName),
        isVirusInterface ? [] : [Validators.required],
      ],
      start: [_get(sampleQueryRequest, 'start', defaultState.start), [Validators.required]],
      referenceBases: [_get(sampleQueryRequest, 'referenceBases', defaultState.referenceBases), [Validators.required]],
      alternateBases: [_get(sampleQueryRequest, 'alternateBases', defaultState.alternateBases), [Validators.required]],
    });
  }

  private getAssembly(isVirusInterface: boolean, sampleQueryRequest?: BeaconQueryAlleleRequestModel): string {
    const defaultAssembly = isVirusInterface ? 'virus' : defaultState.assemblyId;
    return _get(sampleQueryRequest, 'assemblyId', defaultAssembly);
  }

}
