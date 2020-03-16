import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorHandlerService, realmIdPlaceholder } from 'ddap-common-lib';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class SearchService {

  constructor( private http: HttpClient,
               private errorHandler: ErrorHandlerService) { }

  getTables() {
    return this.http.get(`${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/search/tables`)
      .pipe(
        this.errorHandler.notifyOnError()
      );
  }

  search(query) {
    return this.http.post(`${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/search/query`, query)
      .pipe(
        this.errorHandler.notifyOnError()
      );
  }
}
