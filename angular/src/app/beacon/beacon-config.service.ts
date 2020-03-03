import { Injectable } from '@angular/core';

import { BeaconRegistry } from './beacon-service/beacon.model';

@Injectable({
  providedIn: 'root',
})
export class BeaconConfigService {
  private registries;

  constructor(
    ) {

      this.registries = [
        {
          'id' : 'beacon-network',
          'name' : 'Beacon Network',
          'url' : 'https://beacon-network.org',
          'apiUrl' : 'https://beacon-network.org/api',
        },
      ];
  }

  getRegistries(): BeaconRegistry[] {
    return this.registries;
  }
}
