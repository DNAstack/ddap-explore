import { AbstractControl } from '@angular/forms';

import { BeaconSearchQueryParser } from '../beacon-search-query.parser';

export class BeaconSearchVariantValidator {

  static variant(control: AbstractControl) {
    if (!BeaconSearchQueryParser.validate(control.value)) {
      return { validLocus: true };
    }

    return {
      // locus - location on a chromosome
      validLocus: true,
    };
  }

}
