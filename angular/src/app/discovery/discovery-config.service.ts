import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class DiscoveryConfigService {
  
  private beaconApiUrl : string;

  constructor(
    ) {
      this.beaconApiUrl = "http://localhost:8088/beacon/public";
  }

  getBeaconApiUrl(): string {
    return this.beaconApiUrl;
  }
}
