import { Injectable } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import _get from 'lodash.get';

import { BeaconInfoResourcePair } from '../../../shared/apps/app-discovery/app-discovery.model';

@Injectable({
  providedIn: 'root',
})
export class BeaconInfoFormBuilder {

  constructor(private formBuilder: FormBuilder) {
  }

  buildForm(beacons?: BeaconInfoResourcePair[]): FormGroup {
    const selectedBeaconDatasets = _get(beacons, '[0].beaconInfo.datasets', []);
    const datasetsIds = selectedBeaconDatasets ? selectedBeaconDatasets.map((dataset) => dataset.id) : [];
    return this.formBuilder.group({
      beacon: [_get(beacons, '[0]'), [Validators.required]],
      datasets: [datasetsIds, []],
    });
  }

}
