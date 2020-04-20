import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorHandlerService, realmIdPlaceholder } from 'ddap-common-lib';
import { Observable, of } from 'rxjs';

import { environment } from '../../../../environments/environment';

import { BeaconSearchQueryParser } from './beacon-search-query.parser';
import { BeaconSearchRequestModel, BeaconSearchResponseModel } from './beacon-search.model';

@Injectable({
  providedIn: 'root',
})
export class BeaconSearchService {

  constructor(
    private http: HttpClient,
    private errorHandler: ErrorHandlerService
  ) {
  }

  query(queryValue: BeaconSearchRequestModel, accessToken?: string): Observable<BeaconSearchResponseModel[]> {
    const { query, assembly, resource, damId } = queryValue;

    if (!BeaconSearchQueryParser.validate(query)) {
      return of([]);
    }

    const params = BeaconSearchQueryParser.parseParams(query);
    params.assemblyId = assembly;
    if (accessToken) {
      params.accessToken = accessToken;
    }

    if (resource && damId) {
      return this.queryBeacon(damId, resource, params);
    }

    return this.queryAll(params);
  }

  private queryBeacon(damId: string, resourceId: string, params?: BeaconSearchRequestModel): Observable<BeaconSearchResponseModel[]> {
    return this.http.get<BeaconSearchResponseModel[]>(
      `${environment.ddapApiUrlOld}/realm/${realmIdPlaceholder}/resources/${damId}/${resourceId}/search`,
      { params }
    ).pipe(
      this.errorHandler.notifyOnError(`Can't query beacon for resource ${resourceId}.`)
    );
  }

  private queryAll(params?: BeaconSearchRequestModel): Observable<BeaconSearchResponseModel[]> {
    return this.http.get<BeaconSearchResponseModel[]>(
      `${environment.ddapApiUrlOld}/realm/${realmIdPlaceholder}/resources/search`,
      { params }
    ).pipe(
      this.errorHandler.notifyOnError(`Can't query beacons.`)
    );
  }
}
