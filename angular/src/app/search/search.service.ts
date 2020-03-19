import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorHandlerService, realmIdPlaceholder } from 'ddap-common-lib';
import { BehaviorSubject, Observable } from 'rxjs';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class SearchService {

  tableData: BehaviorSubject<object>;

  constructor(private http: HttpClient,
              private errorHandler: ErrorHandlerService) {
    this.tableData = new BehaviorSubject<object>({});
  }

  getSearchResources(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/search`)
      .pipe(
        this.errorHandler.notifyOnError()
      );
  }

  getTables(resource: string): Observable<any> {
    return this.http.get<any>(`${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/search/tables`,
      { params: {
          resource: encodeURIComponent(resource),
        }})
      .pipe(
        this.errorHandler.notifyOnError()
      );
  }

  search(resource: string, query): Observable<any> {
    return this.http.post<any>(`${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/search/query`, query,
      { params: {
          resource: encodeURIComponent(resource),
        }})
      .pipe(
        this.errorHandler.notifyOnError()
      );
  }

  updateTableData(tableData) {
    this.tableData.next(tableData);
  }
}
