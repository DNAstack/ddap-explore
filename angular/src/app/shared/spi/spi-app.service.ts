import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RealmStateService } from 'ddap-common-lib';
import _get from 'lodash.get';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { TableModel } from '../search/table.model';

import { SPIAppBeaconListResponseModel } from './app-beacon-list-response.model';
import { SimpleSearchRequest } from './app-search-simple-filter-request.model';
import { SPIAppSearchSimpleListResponseModel } from './app-search-simple-list-response.model';

@Injectable({
  providedIn: 'root',
})
export class SPIAppService {
  private urlToResponseMap: Map<string, any> = new Map<string, any>();

  constructor(private http: HttpClient,
              private activatedRoute: ActivatedRoute,
              private realmStateService: RealmStateService) {
  }

  getBeaconResources(collectionId: string, cacheable: boolean = true): Observable<SPIAppBeaconListResponseModel> {
    const url = `/api/v1beta/${this.getRealmId()}/apps/discovery/beacon/resources?collection=${collectionId}`;
    return this.makeCacheableRequest<SPIAppBeaconListResponseModel>('get', url);
  }

  getSimpleSearchResources(collectionId: string, cacheable: boolean = true): Observable<SPIAppSearchSimpleListResponseModel> {
    const url = `/api/v1beta/${this.getRealmId()}/apps/search/simple/resources?collection=${collectionId}`;
    return this.makeCacheableRequest<SPIAppSearchSimpleListResponseModel>('get', url);
  }

  submitSimpleSearchFilter(interfaceId: string, request: SimpleSearchRequest): Observable<TableModel> {
    const url = `/api/v1beta/${this.getRealmId()}/apps/search/simple/filter?resource=${interfaceId}`;
    return this.http.post<TableModel>(url, request);
  }

  private getRealmId() {
    return _get(this.activatedRoute, 'root.firstChild.snapshot.params.realmId', this.realmStateService.getRealm());
  }

  private makeCacheableRequest<T>(method: string, url: string, body?: any, cacheKey?: string,
                                  cacheReusable: boolean = true): Observable<T> {
    cacheKey = cacheKey || url;

    if (cacheReusable && this.urlToResponseMap.has(cacheKey)) {
      return new Observable<T>(observer => {
        observer.next(this.urlToResponseMap.get(cacheKey));
        observer.complete();
      });
    }

    switch (method.toLowerCase()) {
      case 'get':
        return this.http.get<T>(url)
          .pipe(map(response => {
            this.urlToResponseMap.set(cacheKey, response);
            return response;
          }));
      default:
        throw new Error(`Unknown request method ${method}`);
    }
  }
}
