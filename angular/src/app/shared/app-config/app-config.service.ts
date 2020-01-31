import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorHandlerService } from 'ddap-common-lib';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { AppConfigModel } from './app-config.model';

@Injectable({
    providedIn: 'root',
})
export class AppConfigService {
  constructor(private http: HttpClient,
              private errorHandler: ErrorHandlerService) { }

  get(): Observable<AppConfigModel> {
    return this.http.get<AppConfigModel>(`${environment.ddapApiUrl}/config`)
      .pipe(
        this.errorHandler.notifyOnError(`Unable to initialize`)
      );
  }

  getDefault(): AppConfigModel {
    return {
      title: 'DDAP',
      sidebarEnabled: true,
      featureAdministrationEnabled: true,
      featureExploreDataEnabled: true,
      featureWorkflowsEnabled: true,
    };
  }
}
