import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppService, ButtonRoute, ErrorHandlerService, ViewControllerService } from 'ddap-common-lib';
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
              private errorHandler: ErrorHandlerService,
              private viewController: ViewControllerService
              ) {

                /**
                 * TODO: load applications based on configuration
                 */
                const dataAppRoutes = [
                  new ButtonRoute(
                    'collections_bookmark',
                    'Collections',
                    '/data/collections'
                  ),
                  new ButtonRoute(
                    'save_alt',
                    'Saved',
                    '/data/saved'
                  ),
                ];

                const dataApp = new AppService(
                  'Data',
                  new ButtonRoute(
                    'collections_bookmark',
                    'Data',
                    '/data'
                  ),
                  dataAppRoutes
                );

                const workflowAppRoutes = [
                  new ButtonRoute(
                    'play_arrow',
                    'Run',
                    '/analyze/run'
                  ),
                  new ButtonRoute(
                    'bubble_chart',
                    'Registry',
                    '/analyze/workflows'
                  ),
                  new ButtonRoute(
                    'sync',
                    'Operations',
                    '/analyze/operations'
                  ),
                ];

                const workflowApp = new AppService(
                  'Workflows',
                  new ButtonRoute(
                    'bubble_chart',
                    'Workflows',
                    '/analyze'
                  ),
                  workflowAppRoutes
                );

                this.viewController.apps = [ dataApp, workflowApp ];
                this.viewController.currentApp = dataApp;
              }

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
      title: '', // This is a placeholder and the actual value would be set by the backend service.
      defaultModule: null,
      inStandaloneMode: false,
      authorizationOnInitRequired: false,
      sidebarEnabled: true,
      featureRealmInputEnabled: true,
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
