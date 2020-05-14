import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { realmIdPlaceholder } from 'ddap-common-lib';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { BeaconQueryAlleleRequestModel, BeaconQueryResponseModel } from '../../beacon/beacon-search.model';

import { BeaconInfoRequestModel, BeaconInfoResponseModel, BeaconResourcesResponse } from './app-discovery.model';

@Injectable({
  providedIn: 'root',
})
export class AppDiscoveryService {

  constructor(private http: HttpClient) {
  }

  getBeaconResources(collectionId: string): Observable<BeaconResourcesResponse> {
    const url = `${environment.ddapApiUrl}/${realmIdPlaceholder}/apps/discovery/beacon/resources?collection=${collectionId}`;
    return this.http.get<BeaconResourcesResponse>(url);
  }

  getBeaconInfo(interfaceId: string, params?: BeaconInfoRequestModel): Observable<BeaconInfoResponseModel> {
    return this.http.get<BeaconInfoResponseModel>(
      `${environment.ddapApiUrl}/${realmIdPlaceholder}/apps/discovery/beacon?resource=${interfaceId}`,
      { params }
    );
  }

  queryBeacon(
    resourceInterfaceId: string,
    params?: BeaconQueryAlleleRequestModel
  ): Observable<BeaconQueryResponseModel> {
    return this.http.get<BeaconQueryResponseModel>(
      `${environment.ddapApiUrl}/${realmIdPlaceholder}/apps/discovery/beacon/query?resource=${resourceInterfaceId}`,
      { params }
    );
  }

}
