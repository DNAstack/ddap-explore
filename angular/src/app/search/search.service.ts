import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorHandlerService, realmIdPlaceholder } from 'ddap-common-lib';
import { BehaviorSubject, Observable } from 'rxjs';

import { environment } from '../../environments/environment';

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
      { params: {
          resource: encodeURIComponent(resource),
          accessToken: accessToken,
          connectorKey: connectorDetails['key'],
          connectorToken: connectorDetails['token'],
        }});
  }

  getPublicTables(resource: string): Observable<any> {
    return this.http.get<any>(resource + '/tables');
  }

  search(resource: string, query, accessToken, connectorDetails: object = {}): Observable<any> {
    return this.http.post<any>(`${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/search/query`, query,
      { params: {
          resource: encodeURIComponent(resource),
          accessToken: accessToken,
          connectorKey: connectorDetails['key'],
          connectorToken: connectorDetails['token'],
        }})
      .pipe(
        this.errorHandler.notifyOnError()
      );
  }

  makeDirectSearch(resource: string, query): Observable<any> {
    return this.http.post<any>(resource + '/search', query)
      .pipe(
        this.errorHandler.notifyOnError()
      );
  }

  updateTableData(tableData) {
    this.tableData.next(tableData);
  }

  buildResourcePath(damId: string, view: {resourceName: string, viewName: string, roleName?: string, interfaceName?: string}) {
    return `${damId};${view.resourceName}/views/${view.viewName}/roles/${view.roleName}/interfaces/${view.interfaceName}`;
  }

  authorizeResource(redirectUri: string, resourceName: string) {
    return;
  }
}
