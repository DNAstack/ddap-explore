import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import * as Search from './ga4gh-discovery-search.models';

@Injectable({
  providedIn: 'root',
})
export class Ga4ghDiscoverySearchService {

  apiUrl: String;

  constructor(
    private httpClient: HttpClient
  ) {}

  setApiUrl (url: String) {
    this.apiUrl = url;
  }

  getApiUrl() {
    return this.apiUrl;
  }

  getTables(headers?: HttpHeaders): Promise<Search.Tables> {
    return this.route(
      () => this.httpClient.get<Search.Table[]>(`${this.apiUrl}`
      + '/tables', { 'headers' : headers }).toPromise(),
      () => this.httpClient.get<Search.Datasets>(`${this.apiUrl}`
      + '/datasets', { 'headers' : headers }).toPromise().then(
        data => {
          const tables = data.datasets.map(dataset => {
            return {
              'name': dataset.id,
              'description': dataset.description,
              'schema': {
                'description': dataset.schema.description,
                'schemaJson': dataset.schema.properties,
              },
            };
          });
          return { 'tables': tables };
        }
      ),
      headers
    );
  }

  getTableInfo(tableName: String, headers?: HttpHeaders): Promise<Search.Table> {
    return this.httpClient.get<Search.Table>(`${this.apiUrl}`
    + '/datasets/' + tableName + '/info', { 'headers' : headers }).toPromise();
  }

  getTableData(tableName: String, headers?: HttpHeaders): Promise<Search.TableData> {
    return this.route(
      () => this.httpClient.get<Search.TableData>(`${this.apiUrl}`
      + '/table/' + tableName + '/data', { 'headers' : headers }).toPromise(),
      () => this.httpClient.get<Search.TableData>(`${this.apiUrl}`
      + '/table/' + tableName + '/data', { 'headers' : headers }).toPromise().then(
        data => {
          return {
            'objects': data.data,
            'pagination': data.pagination,
            'schema': {
              'description': data.data_model.description,
              'schemaJson': data.data_model.properties,
            },
          };
        }
      ),
      headers
    );
  }

  search(query: Search.Query, headers?: HttpHeaders): Promise<Search.TableData> {
    return this.route(
      () => this.httpClient.post<Search.TableData>(`${this.apiUrl}`
      + '/search', query, { 'headers' : headers }).toPromise(),
      () => this.httpClient.post<Search.TableData>(`${this.apiUrl}`
      + '/search', query, { 'headers' : headers }).toPromise().then(
        data => {
          return {
            'objects': data.data,
            'pagination': data.pagination,
            'data_model': {
              'id': data.data_model.id,
              'description': data.data_model.description,
              'properties': data.data_model.properties,
            },
          };
        }
      ),
      headers
    );
  }

  private route<T>(withNewEndpoints, withOldEndpoints, headers?: HttpHeaders) {
    return new Promise<T>((resolve, reject) => this.httpClient.options(`${this.apiUrl}`
    + '/tables', { 'headers' : headers, 'observe': 'response' }).subscribe(
      response => {
        withNewEndpoints().then(data => resolve(data)).catch(err => reject(err));
      },
      () => {
        withOldEndpoints().then(data => resolve(data)).catch(err => reject(err));
      }
    ));
  }
}
