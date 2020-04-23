import { AbstractControl } from '@angular/forms';

import { BeaconSearchQueryParser } from '../beacon-search-query.parser';

export class BeaconSearchVariantValidator {

  static variant(control: AbstractControl) {
    if (!control || !control.value || control.value === '') {
      return null;
    }

    if (BeaconSearchQueryParser.validate(control.value)) {
      return null;
    }

    return {
      // locus - location on a chromosome
      invalidLocus: true,
    };
  }

}
