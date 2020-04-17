import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorHandlerService, realmIdPlaceholder } from 'ddap-common-lib';
import { Observable, of } from 'rxjs';

import { environment } from '../../../../environments/environment';

import { BeaconResponse } from './beacon-response.model';
import { DnaChangeQueryParser } from './dna-change-query.parser';

@Injectable({
  providedIn: 'root',
})
export class ResourceBeaconService {

  constructor(private http: HttpClient,
              private errorHandler: ErrorHandlerService) {
  }

  query(queryValue: BeaconServiceQuery, accessToken?: string): Observable<BeaconResponse[]> {
    const {query, assembly, resource, damId} = queryValue;

    if (!DnaChangeQueryParser.validate(query)) {
      return of([]);
    }

    const params = DnaChangeQueryParser.parseParams(query);
    params.assemblyId = assembly;
    if (accessToken) {
      params.accessToken = accessToken;
    }

    if (resource && damId) {
      return this.queryBeacon(damId, resource, params);
    }

    return this.queryAll(params);
  }

  private queryBeacon(damId: string, resourceId: string, params?): Observable<BeaconResponse[]> {
    return this.http.get<BeaconResponse[]>(
      `${environment.ddapApiUrlOld}/realm/${realmIdPlaceholder}/resources/${damId}/${resourceId}/search`,
      {params}
    ).pipe(
      this.errorHandler.notifyOnError(`Can't query beacon for resource ${resourceId}.`)
    );
  }

  private queryAll(params = {}): Observable<BeaconResponse[]> {
    return this.http.get<BeaconResponse[]>(
      `${environment.ddapApiUrlOld}/realm/${realmIdPlaceholder}/resources/search`,
      {params}
    ).pipe(
      this.errorHandler.notifyOnError(`Can't query beacons.`)
    );
  }
}

export interface BeaconServiceQuery {
  query: string;
  assembly: string;
  resource?: string;
  damId?: string;
  limitSearch?: boolean;
}
