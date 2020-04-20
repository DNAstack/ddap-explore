import { Injectable } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Injectable({
  providedIn: 'root',
})
export class CollectionResourceFormBuilder {

  constructor(private formBuilder: FormBuilder) {
  }

  buildForm(): FormGroup {
    return this.formBuilder.group({
      ttl: [1, [Validators.required, Validators.min(1)]],
      timeUnit: ['h', [Validators.required]],
      interfaceType: [undefined, [Validators.required]],
    });
  }

}
