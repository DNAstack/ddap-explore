import { Injectable } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

import { Assembly } from '../beacon-search.model';

import { BeaconSearchVariantValidator } from './beacon-search-variant.validator';

@Injectable({
  providedIn: 'root',
})
export class BeaconSearchFormBuilder {

  constructor(private formBuilder: FormBuilder) {
  }

  buildForm(): FormGroup {
    return this.formBuilder.group({
      assembly: [Assembly.grch37, [Validators.required]],
      query: ['', [Validators.required, BeaconSearchVariantValidator.variant]],
    });
  }

}
