import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorHandlerService, realmIdPlaceholder } from 'ddap-common-lib';
import { BehaviorSubject, Observable, Subscriber } from 'rxjs';

import { environment } from '../../environments/environment';
import { Table } from '../shared/table.model';

import { SearchResourceModel } from './search-resources/search-resource.model';

@Injectable({
  providedIn: 'root',
})
export class SearchService {

  tableData: BehaviorSubject<object>;

  constructor(private http: HttpClient,
              private errorHandler: ErrorHandlerService) {
    this.tableData = new BehaviorSubject<object>({});
  }

  getSearchResources(): Observable<SearchResourceModel[]> {
    return this.http.get<SearchResourceModel[]>(`${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/search/resources`)
      .pipe(
        this.errorHandler.notifyOnError()
      );
  }

  getResourceDetail(resourceName: string): Observable<SearchResourceModel[]> {
    return this.http.get<SearchResourceModel[]>(
      `${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/search/resource/${resourceName}`);
  }

  getTables(resource: string, accessToken, connectorDetails: object = {}): Observable<any> {
    if (!accessToken) {
      console.warn('No access token');
      return;
    }
    return this.http.get<any>(`${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/search/tables`,
      {
        params: {
          resource: encodeURIComponent(resource),
          accessToken: accessToken,
          connectorKey: connectorDetails['key'],
          connectorToken: connectorDetails['token'],
        },
      });
  }

  getPublicTables(resource: string): Observable<any> {
    return this.http.get<any>(resource + '/tables');
  }

  search(resource: string, query, accessToken, connectorDetails: object = {}): Observable<Table> {
    return this.http.post<Table>(`${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/search/query`, query,
      {
        params: {
          resource: encodeURIComponent(resource),
          accessToken: accessToken,
          connectorKey: connectorDetails['key'],
          connectorToken: connectorDetails['token'],
        },
      })
      .pipe(
        this.errorHandler.notifyOnError()
      );
  }

  observableSearch(resource: string, query, accessToken?: string, connectorDetails: ConnectorDetails = {}): Observable<Table> {
    const params = this.buildSearchParameters(
      {
        resource: encodeURIComponent(resource),
      },
      accessToken,
      connectorDetails
    );

    return new Observable<Table>(subscriber => {
      const observable = this.http.post<Table>(
        this.buildSearchUrl(accessToken ? null : resource),
        query,
        {params: params}
      ).pipe(
        this.errorHandler.notifyOnError()
      );

      observable.subscribe(table => {
        if (table.data.length === 0 && table.pagination.next_page_url) {
          this.followUp(table.pagination.next_page_url, accessToken, connectorDetails, subscriber);
        } else {
          subscriber.next(table);
          subscriber.complete();
        }
      });
    });
  }

  followUp(url: string, accessToken: string, connectorDetails: ConnectorDetails = {}, subscriber: Subscriber<Table>, delay: number = 1) {
    if (delay > 32) {
      this.errorHandler.openSnackBar('The server took too long to respond. No more follow-up requests.');
      subscriber.next({data: []});
      subscriber.complete();
      return;
    }

    // FIXME We need to figure out how to make a follow-up request on a protected search service.
    const observable = this.http.get<Table>(
      url,
      {params: this.buildSearchParameters({}, accessToken, connectorDetails)}
    ).pipe(
      this.errorHandler.notifyOnError()
    );

    observable.subscribe(table => {
      if (table.data.length === 0 && table.pagination.next_page_url) {
        setTimeout(
          () => this.followUp(
            table.pagination.next_page_url,
            accessToken,
            connectorDetails,
            subscriber,
            Math.pow(delay, 2)  // next delay
          ),
          delay
        );
      } else {
        subscriber.next(table);
        subscriber.complete();
      }
    });
  }

  makeDirectSearch(resource: string, query): Observable<Table> {
    return this.observableSearch(resource, query);
  }

  updateTableData(tableData) {
    this.tableData.next(tableData);
  }

  buildResourcePath(damId: string, view: { resourceName: string, viewName: string, roleName?: string, interfaceName?: string }) {
    return `${damId};${view.resourceName}/views/${view.viewName}/roles/${view.roleName}/interfaces/${view.interfaceName}`;
  }

  authorizeResource(redirectUri: string, resourceName: string) {
    return;
  }

  /**
   * Build the URL to the search endpoint.
   *
   * If resourceUrl is NOT provided, it will assume that the client needs to make a query via the backend service.
   */
  private buildSearchUrl(resourceUrl: string): string {
    if (resourceUrl) {
      return `${resourceUrl}/search`;
    } else {
      return `${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/search/query`;
    }
  }

  /**
   * Build the search request parameters.
   *
   * If accessToken is provided, connectorDetails will be used.
   */
  private buildSearchParameters(commonParameters: object, accessToken?: string, connectorDetails: ConnectorDetails = {}): any {
    const params = commonParameters;

    if (accessToken) {
      params['accessToken'] = accessToken;
      params['connectorKey'] = connectorDetails['key'];
      params['connectorToken'] = connectorDetails['token'];
    }

    return params;
  }
}

interface ConnectorDetails {
  key?: string;
  token?: string;
}
