import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { realmIdPlaceholder } from 'ddap-common-lib';
import { Observable, Subscriber } from 'rxjs';
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

  getResources(collectionId: string, cacheable: boolean = true): Observable<SPIAppSearchSimpleListResponseModel> {
    const url = `${this.baseUri}/apps/search/simple/resources?collection=${collectionId}`;
    return this.makeCacheableRequest<SPIAppSearchSimpleListResponseModel>('get', url);
  }

  filter(interfaceId: string, request: SimpleSearchRequest): Observable<TableModel> {
    const url = `${this.baseUri}/apps/search/simple/filter?resource=${interfaceId}`;
    return new Observable<TableModel>(subscriber => {
      this.http.post<TableModel>(url, request)
        .subscribe(table => {
          if (this.isQueryStillInProgress(table)) {
            setTimeout(() => this.followUp(table.pagination.next_page_url, subscriber), 1000);
          } else {
            subscriber.next(table);
            subscriber.complete();
          }
        });
    });
  }

  get baseUri() {
    return `${environment.ddapApiUrl}/${realmIdPlaceholder}`;
  }

  private followUp(url: string, subscriber: Subscriber<TableModel>, delay: number = 1) {
    if (delay > 32) {
      console.error(`The request is taking too long. No follow-up request to ${url}`);

      subscriber.next({data: []});
      subscriber.complete();

      return;
    }

    console.warn(`Still haven't get the result. Making a follow-up request to ${url}`);

    // FIXME We need to figure out how to make a follow-up request on a protected search service.
    this.http.get<TableModel>(
      url
    ).subscribe(table => {
      if (this.isQueryStillInProgress(table)) {
        setTimeout(
          () => {
            this.followUp(
              table.pagination.next_page_url,
              subscriber,
              delay * 2  // the backoff period will be increased exponentially.
            );
          },
          delay * 1000 // convert to millisecond
        );
      } else {
        subscriber.next(table);
        subscriber.complete();
      }
    });
  }

  private isQueryStillInProgress(table: TableModel) {
    return table.data.length === 0 && table.pagination.next_page_url;
  }

  /**
   * @deprecated
   *
   * FIXME re-implement this with Store
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
