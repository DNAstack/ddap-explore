import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { RealmPlaceholderService } from '../../realm/realm-placeholder.service';

import {
  BeaconInfoRequestModel,
  BeaconInfoResponseModel,
  BeaconQueryRequestModel,
  BeaconQueryResponseModel,
} from './app-discovery.model';

@Injectable({
  providedIn: 'root',
})
export class AppDiscoveryService {

  constructor(
    private http: HttpClient,
    private realmPlaceholderService: RealmPlaceholderService
  ) {
  }

  getBeaconInfo(resourceInterfaceId: string, params?: BeaconInfoRequestModel): Observable<BeaconInfoResponseModel> {
    return this.http.get<BeaconInfoResponseModel>(
      `${environment.ddapApiUrl}/${this.realmPlaceholderService.get()}/discovery/beacon?resource=${resourceInterfaceId}`,
      { params }
    );
  }

  queryBeacon(resourceInterfaceId: string, params?: BeaconQueryRequestModel): Observable<BeaconQueryResponseModel> {
    return this.http.get<BeaconQueryResponseModel>(
      `${environment.ddapApiUrl}/${this.realmPlaceholderService.get()}/discovery/beacon/query?resource=${resourceInterfaceId}`,
      { params }
    );
  }

}
