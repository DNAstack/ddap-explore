import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Beacon, BeaconResponse } from './beacon.model';

@Injectable({
  providedIn: 'root',
})
export class BeaconNetworkService {

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

  getBeacons(headers?: HttpHeaders): Promise<Beacon[]> {
    return this.httpClient.get<Beacon[]>(`${this.apiUrl}`
    + '/beacons', { 'headers' : headers }).toPromise();
  }

  getOrganizations(headers?: HttpHeaders): Promise<Beacon[]> {
    return this.httpClient.get<Beacon[]>(`${this.apiUrl}`
    + '/organizations', { 'headers' : headers }).toPromise();
  }

  searchBeacon(beacon: string, allele: string, chrom: string,
    pos: number, ref: string, referenceAllele: string, headers?: HttpHeaders): Promise<BeaconResponse[]> {
    return this.searchBeacons([beacon], allele, chrom, pos, ref, referenceAllele, headers);
  }

  searchBeacons(beacon: string[], allele: string, chrom: string,
    pos: number, ref: string, referenceAllele: string, headers?: HttpHeaders): Promise<BeaconResponse[]> {

    const beacons = beacon.join(',');
    const params = new HttpParams()
      .set('beacon', beacons)
      .set('allele', allele)
      .set('chrom', chrom)
      .set('pos', pos + '')
      .set('ref', ref)
      .set('referenceAllele', referenceAllele);

    return this.httpClient.get<BeaconResponse[]>(`${this.apiUrl}`
    + '/responses', { 'params': params, 'headers' : headers }).toPromise();
  }
}
