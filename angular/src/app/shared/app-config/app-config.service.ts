import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorHandlerService } from 'ddap-common-lib';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../../../environments/environment';

import { AppConfigModel } from './app-config.model';

@Injectable({
    providedIn: 'root',
})
export class AppConfigService {
  public cachedConfig: AppConfigModel;

  constructor(private http: HttpClient,
              private errorHandler: ErrorHandlerService) { }

  get(): Observable<AppConfigModel> {
    if (this.cachedConfig) {
      return new Observable(subscriber => {
        subscriber.next(this.cachedConfig);
        subscriber.complete();
      });
    }

    return this.http.get<AppConfigModel>(`${environment.ddapApiUrl}/config`)
      .pipe(
        map(config => {
          this.cachedConfig = config;
          return config;
        }),
        this.errorHandler.notifyOnError(`Unable to initialize`)
      );
  }

  getDefault(): AppConfigModel {
    return this.cachedConfig || {
      title: '',
      defaultModule: null,
      inStandaloneMode: false,
      authorizationOnInitRequired: false,
      sidebarEnabled: true,
      featureAdministrationEnabled: true,
      featureExploreDataEnabled: true,
      featureWorkflowsEnabled: true,
      featureWorkflowsTrsIntegrationEnabled: true,
      trsBaseUrl: null,
      trsAcceptedToolClasses: [],
      trsAcceptedVersionDescriptorTypes: [],
      listPageSize: 14,
    };
  }
}
