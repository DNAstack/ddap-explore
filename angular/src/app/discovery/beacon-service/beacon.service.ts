import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { BeaconResponse } from './beacon.model';

@Injectable({
  providedIn: 'root',
})
export class BeaconService {

  apiUrl: String;

  constructor(
    private httpClient: HttpClient
  ) {}

  setApiUrl (url: String) {
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
    : Promise<BeaconResponse[]> {
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
    : Promise<BeaconResponse[]> {

    const params = new HttpParams()
      .set('assemblyId', assembly)
      .set('referenceName', referenceName)
      .set('start', start + '')
      .set('referenceBases', referenceBases)
      .set('alternateBases', alternateBases)
      ;

      // /query?referenceName=1&start=9924&referenceBases=C&alternateBases=T&assemblyId=GRCh38
    return this.httpClient.get<BeaconResponse[]>(`${this.apiUrl}`
    + '/query', { 'params': params, 'headers' : headers }).toPromise();
  }
}
