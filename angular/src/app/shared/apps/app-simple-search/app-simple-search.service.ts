import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { realmIdPlaceholder } from 'ddap-common-lib';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../../../../environments/environment';
import { TableModel } from '../../search/table.model';

import { SimpleSearchRequest } from './models/app-search-simple-filter-request.model';
import { SPIAppSearchSimpleListResponseModel } from './models/app-search-simple-list-response.model';

@Injectable({
  providedIn: 'root',
})
export class AppSimpleSearchService {
  private urlToResponseMap: Map<string, any> = new Map<string, any>();

  constructor(private http: HttpClient) {
  }

  getSimpleSearchResources(collectionId: string, cacheable: boolean = true): Observable<SPIAppSearchSimpleListResponseModel> {
    const url = `${this.baseUri}/apps/search/simple/resources?collection=${collectionId}`;
    return this.makeCacheableRequest<SPIAppSearchSimpleListResponseModel>('get', url);
  }

  submitSimpleSearchFilter(interfaceId: string, request: SimpleSearchRequest): Observable<TableModel> {
    const url = `${this.baseUri}/apps/search/simple/filter?resource=${interfaceId}`;
    return this.http.post<TableModel>(url, request);
  }

  get baseUri() {
    return `${environment.ddapApiUrl}/${realmIdPlaceholder}`;
  }

  /**
   * @deprecated
   *
   * FIXME reimplement this with Store
   */
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
