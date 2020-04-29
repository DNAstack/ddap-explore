import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatSnackBar, MatSnackBarRef } from '@angular/material/snack-bar';
import { SimpleSnackBar } from '@angular/material/snack-bar/typings/simple-snack-bar';
import { ErrorHandlerService, realmIdPlaceholder } from 'ddap-common-lib';
import { BehaviorSubject, Observable, Subscriber } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { environment } from '../../environments/environment';
import { JsonSchema } from '../shared/search/json-schema.model';
import { TableInfo } from '../shared/search/table-info.model';
import { TableList } from '../shared/search/table-list.model';
import { TableModel } from '../shared/search/table.model';

import { SearchResourceModel } from './search-resources/search-resource.model';

@Injectable({
  providedIn: 'root',
})
export class SearchService {

  tableData: BehaviorSubject<object>;

  // For notification
  snackBarRef: MatSnackBarRef<SimpleSnackBar>;

  constructor(private http: HttpClient,
              private snackBar: MatSnackBar,
              private errorHandler: ErrorHandlerService) {
    this.tableData = new BehaviorSubject<object>({});
  }

  getSearchResources(): Observable<SearchResourceModel[]> {
    return this.http.get<SearchResourceModel[]>(`${environment.ddapAlphaApiUrl}/realm/${realmIdPlaceholder}/search/resources`)
      .pipe(
        this.errorHandler.notifyOnError()
      );
  }

  getResourceDetail(resourceName: string): Observable<SearchResourceModel[]> {
    return this.http.get<SearchResourceModel[]>(
      `${environment.ddapAlphaApiUrl}/realm/${realmIdPlaceholder}/search/resource/${resourceName}`);
  }

  getTables(resource: string, accessToken, connectorDetails: object = {}): Observable<TableList> {
    if (!accessToken) {
      console.warn('No access token');
      return;
    }

    return this.http.get<any>(`${environment.ddapAlphaApiUrl}/realm/${realmIdPlaceholder}/search/tables`,
      { params: {
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

  search(resource: string, query, accessToken, connectorDetails: object = {}): Observable<TableModel> {
    return this.http.post<TableModel>(`${environment.ddapAlphaApiUrl}/realm/${realmIdPlaceholder}/search/query`, query,
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

  observableSearch(resource: string, query, accessToken?: string, connectorDetails: ConnectorDetails = {},
                   onError?: any): Observable<TableModel> {
    const params = this.buildSearchParameters(
      {
        resource: encodeURIComponent(resource),
      },
      accessToken,
      connectorDetails
    );

    const errorHandler = onError
      ? catchError(error => {
        onError(error);
        throw error;
      })
      : this.errorHandler.notifyOnError();

    return new Observable<TableModel>(subscriber => {
      this.http.post<TableModel>(
        this.buildSearchUrl(accessToken ? null : resource),
        query,
        {params: params}
      ).pipe(
        errorHandler
      ).subscribe((table: TableModel) => {
        if (table.data.length === 0 && table.pagination.next_page_url) {
          this.followUp(table.pagination.next_page_url, accessToken, connectorDetails, subscriber, errorHandler);
        } else {
          subscriber.next(table);
          subscriber.complete();
        }
      });
    });
  }

  followUp(url: string, accessToken: string, connectorDetails: ConnectorDetails = {}, subscriber: Subscriber<TableModel>,
           onError?: any, delay: number = 1) {
    if (delay > 32) {
      this.notifyUser('The server took too long to respond. No more follow-up requests.');
      subscriber.next({data: []});
      subscriber.complete();
      return;
    }

    // FIXME We need to figure out how to make a follow-up request on a protected search service.
    this.http.get<TableModel>(
      url,
      {params: this.buildSearchParameters({}, accessToken, connectorDetails)}
    ).pipe(
      onError ? onError : this.errorHandler.notifyOnError()
    ).subscribe((table: TableModel) => {
      if (table.data.length === 0 && table.pagination.next_page_url) {
        setTimeout(
          () => {
            this.followUp(
              table.pagination.next_page_url,
              accessToken,
              connectorDetails,
              subscriber,
              onError,
              delay * 2  // next delay
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

  updateTableData(tableData) {
    this.tableData.next(tableData);
  }

  buildResourcePath(damId: string, view: { resourceName: string, viewName: string, roleName?: string, interfaceName?: string }) {
    return `${damId};${view.resourceName}/views/${view.viewName}/roles/${view.roleName}/interfaces/${view.interfaceName}`;
  }

  authorizeResource(redirectUri: string, resourceName: string) {
    return;
  }

  resolveJsonSchemaReference(tableInfo: TableInfo): Observable<TableInfo> {
    const schema = tableInfo.data_model;

    if (!schema.properties && schema.$ref) {
      return this.http.get<TableInfo>(schema.$ref);
    }

    return new Observable<TableInfo>(subscriber => {
      subscriber.next(tableInfo);
      subscriber.complete();
    });
  }

  isTableInfoPropertyListFinal(tableInfo: TableInfo): boolean {
    const schema = tableInfo.data_model;

    if (!schema.properties && schema.$ref) {
      return false;
    }

    return true;
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
      return `${environment.ddapAlphaApiUrl}/realm/${realmIdPlaceholder}/search/query`;
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

  private notifyUser(message: string, delayInSecond: number = 3) {
    const config = {
      panelClass: 'ddap-error',
    };

    if (delayInSecond !== null) {
      config['duration'] = delayInSecond * 1000;
    }

    if (this.snackBarRef) {
      this.snackBarRef.dismiss();
    }

    this.snackBarRef = this.snackBar.open(message, null, config);
  }
}

interface ConnectorDetails {
  key?: string;
  token?: string;
}
