import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { realmIdPlaceholder } from 'ddap-common-lib';
import { Observable, Subscriber } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../../../../environments/environment';
import { TableInfo } from '../../search/table-info.model';
import { SPIAppSearchSimpleListResponseModel } from '../app-simple-search/models/app-search-simple-list-response.model';

@Injectable({
  providedIn: 'root',
})
export class AppSearchService {
  private urlToResponseMap: Map<string, any> = new Map<string, any>();

  constructor(private http: HttpClient) {
  }

  getTableInfo(tableName: string, interfaceId: string): Observable<TableInfo> {
    const url = `${this.baseUri}/table/${tableName}/info?resource=${interfaceId}`;
    return this.makeCacheableRequest<TableInfo>(url);
  }

  get baseUri() {
    return `${environment.ddapApiUrl}/${realmIdPlaceholder}/apps/search`;
  }

  /**
   * @deprecated
   *
   * FIXME re-implement this with Store
   */
  private makeCacheableRequest<T>(url: string, body?: any, cacheKey?: string, cacheReusable: boolean = true): Observable<T> {
    cacheKey = cacheKey || url;

    if (cacheReusable && this.urlToResponseMap.has(cacheKey)) {
      return new Observable<T>(observer => {
        observer.next(this.urlToResponseMap.get(cacheKey));
        observer.complete();
      });
    }

    return this.http.get<T>(url)
      .pipe(map(response => {
        this.urlToResponseMap.set(cacheKey, response);
        return response;
      }));
  }
}
