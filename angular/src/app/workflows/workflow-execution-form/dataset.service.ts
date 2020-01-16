import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorHandlerService, realmIdPlaceholder } from 'ddap-common-lib';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { Dataset } from './dataset-selection-step/dataset.model';

@Injectable({
  providedIn: 'root',
})
export class DatasetService {

  constructor(private http: HttpClient,
              private errorHandler: ErrorHandlerService) { }

  fetchDataset(url: string, accessToken: string): Observable<Dataset> {
    return this.http.get<Dataset>(`${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/dataset?dataset_url=${url}&access_token=${accessToken}`)
      .pipe(
        this.errorHandler.notifyOnError(`Can't fetch dataset list.`)
      );
  }

  getViews(urls: string[]): Observable<any> {
    return this.http.post(`${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/views/lookup`, urls)
      .pipe(
        this.errorHandler.notifyOnError()
      );
  }

}
