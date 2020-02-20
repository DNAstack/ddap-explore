import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorHandlerService, ViewControllerService } from 'ddap-common-lib';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../../../environments/environment';

import { AppConfigModel } from './app-config.model';
import { AppFilterService } from './app-filter.service';

@Injectable({
  providedIn: 'root',
})
export class AppConfigService {
  private cachedConfig: AppConfigModel;

  constructor(private http: HttpClient,
              private errorHandler: ErrorHandlerService,
              private viewController: ViewControllerService
  ) {
    this.registerModules();
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
          this.viewController.addFilter(new AppFilterService(this.cachedConfig));
          return config;
        }),
        this.errorHandler.notifyOnError(`Unable to initialize`)
      );
  }

  getDefault(): AppConfigModel {
    return this.cachedConfig || {
      title: 'DDAP', // This is a placeholder and the actual value would be set by the backend service.
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

  private registerModules() {
    this.viewController
      .registerModule({
        key: 'data',
        name: 'Data',
        iconClasses: 'icon icon-explore',
        requiredFeatureFlags: ['featureExploreDataEnabled'],
        routerLink: 'data',
        isApp: true,
      })
      .registerModule({
        key: 'data-collections',
        name: 'Collections',
        iconName: 'collections_bookmark',
        routerLink: 'data/collections',
        parentKey: 'data',
        isApp: false,
      })
      .registerModule({
        key: 'data-saved',
        name: 'Saved',
        iconName: 'save_alt',
        routerLink: 'data/saved',
        parentKey: 'data',
        isApp: false,
      });

    this.viewController
      .registerModule({
        key: 'analytics',
        name: 'Analytics',
        requiredFeatureFlags: ['featureWorkflowsEnabled'],
        iconClasses: 'icon icon-rules',
        routerLink: 'analyze',  // FIXME Change to "analytics"
        isApp: true,
      })
      .registerModule({
        key: 'analytics-operations',
        name: 'Operations',
        iconName: 'sync',
        routerLink: 'analyze/operations',
        parentKey: 'analytics',
        isApp: false,
      })
      .registerModule({
        key: 'analytics-registry',
        name: 'Registry',
        iconName: 'bubble_chart',
        routerLink: 'analyze/workflows',
        parentKey: 'analytics',
        isApp: false,
      })
      .registerModule({
        key: 'analytics-run',
        name: 'Run',
        iconName: 'play_arrow',
        routerLink: 'analyze/run',
        parentKey: 'analytics',
        isApp: false,
      })
    ;
  }
}
