import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { BeaconAPIResponse, BeaconResponse } from './beacon.model';

@Injectable({
  providedIn: 'root',
})
export class BeaconService {
  apiUrl: String;

  constructor(
    private httpClient: HttpClient
  ) {
  }

  isReady(): boolean {
    return this.apiUrl !== undefined && this.apiUrl !== null;
  }

  setApiUrl(url: String) {
    this.apiUrl = url;
  }

  getApiUrl() {
    return this.apiUrl;
  }

  searchBeacon(
    assembly: string,
    referenceName: string,
    start: number,
    referenceBases: string,
    alternateBases: string,
    headers?: HttpHeaders)
    : Promise<BeaconAPIResponse> {
    return this.searchBeacons(
      assembly,
      referenceName,
      start,
      referenceBases,
      alternateBases,
      headers);
  }

  searchBeacons(
    assembly: string,
    referenceName: string,
    start: number,
    referenceBases: string,
    alternateBases: string,
    headers?: HttpHeaders)
    : Promise<BeaconAPIResponse> {

    const params = new HttpParams()
      .set('assemblyId', assembly)
      .set('referenceName', referenceName)
      .set('start', start + '')
      .set('referenceBases', referenceBases)
      .set('alternateBases', alternateBases)
    ;

    // /query?referenceName=1&start=9924&referenceBases=C&alternateBases=T&assemblyId=GRCh38
    return this.httpClient.get<BeaconAPIResponse>(`${this.apiUrl}`
      + '/query', {'params': params, 'headers': headers}).toPromise();
  }
}
