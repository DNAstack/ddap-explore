import { Injectable } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import { BeaconSearchVariantValidator } from '../../../../shared/beacon/beacon-search-variant.validator';
import { Assembly } from '../../../../shared/beacon/beacon-search.model';

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
