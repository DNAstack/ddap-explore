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

  buildForm(beaconId: string, beacons?: BeaconInfoResourcePair[]): FormGroup {
    let selectedBeaconDatasets;
    let selectedBeacon;
    if (beaconId && beaconId.length) {
      selectedBeacon = beacons.find(beacon => beacon.resource.interfaces[0].id === beaconId);
      selectedBeaconDatasets = selectedBeacon
                                ? selectedBeacon.beaconInfo.datasets
                                : _get(beacons, '[0].beaconInfo.datasets', []);
    } else {
      selectedBeaconDatasets = _get(beacons, '[0].beaconInfo.datasets', []);
    }
    const datasetsIds = selectedBeaconDatasets ? selectedBeaconDatasets.map((dataset) => dataset.id) : [];
    return this.formBuilder.group({
      beacon: [selectedBeacon || _get(beacons, '[0]'), [Validators.required]],
      datasets: [datasetsIds, []],
    });
  }

}
