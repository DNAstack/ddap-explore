import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ErrorHandlerService, ViewControllerService } from 'ddap-common-lib';
import { LOCAL_STORAGE, StorageService } from 'ngx-webstorage-service';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../../../environments/environment';

import { AppConfigModel } from './app-config.model';
import { AppFilterService } from './app-filter.service';

const EXP_FLAG = 'exp_flag';
@Injectable({
  providedIn: 'root',
})
export class AppConfigService {
  private inflight = false;
  private cachedConfig: AppConfigModel;

  constructor(private http: HttpClient,
              private errorHandler: ErrorHandlerService,
              private viewController: ViewControllerService,
              private route: ActivatedRoute,
              @Inject(LOCAL_STORAGE) private storage: StorageService
  ) {
    const expFlag = this.route.snapshot.queryParams[EXP_FLAG];
    if (expFlag !== undefined) {
      this.storage.set(EXP_FLAG, expFlag);
    }
    this.viewController.setExperimentalFlag(this.storage.get(EXP_FLAG));
    this.registerModules();
  }

  get(): Observable<AppConfigModel> {
    if (this.cachedConfig) {
      return new Observable(subscriber => {
        subscriber.next(this.cachedConfig);
        subscriber.complete();
      });
    }

    if (this.inflight) {
      const self = this;
      return new Observable<AppConfigModel>(subscriber => {
        function watchForCompletion() {
          if (!self.cachedConfig) {
            setTimeout(watchForCompletion, 1000);

            return;
          }

          subscriber.next(self.cachedConfig);
          subscriber.complete();
        }

        subscriber.next(this.getDefault());
        watchForCompletion();
      });
    }

    this.inflight = true;

    return this.http.get<AppConfigModel>(`${environment.ddapApiUrl}/config`)
      .pipe(
        map(config => {
          this.inflight = false;
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
      featureBeaconsEnabled: true,
      featureDiscoveryEnabled: true,
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
        key: 'data-explorer',
        name: 'Explorer',
        iconName: 'navigation',
        routerLink: 'data/explorer',
        parentKey: 'data',
        isApp: false,
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
        isExperimental: true,
      });
      this.viewController
      .registerModule({
        key: 'beacon',
        name: 'Beacon',
        iconClasses: 'icon icon-explore',
        requiredFeatureFlags: ['featureBeaconsEnabled'],
        routerLink: 'beacon',
        isApp: true,
      })
      .registerModule({
        key: 'network',
        name: 'Network',
        iconName: 'wifi_tethering',
        routerLink: 'beacon/network',
        parentKey: 'beacon',
        isApp: false,
      })
      .registerModule({
        key: 'search',
        name: 'Search',
        iconName: 'search',
        routerLink: 'beacon/search',
        parentKey: 'beacon',
        isApp: false,
      });
      this.viewController
      .registerModule({
        key: 'discovery',
        name: 'Discovery',
        iconClasses: 'icon icon-explore',
        requiredFeatureFlags: ['featureDiscoveryEnabled'],
        routerLink: 'discovery',
        isApp: true,
      })
      .registerModule({
        key: 'beacon',
        name: 'Beacon',
        iconName: 'wifi_tethering',
        routerLink: 'discovery/beacon',
        parentKey: 'discovery',
        isApp: false,
      })
      .registerModule({
        key: 'search',
        name: 'Search',
        iconName: 'search',
        routerLink: 'discovery/search',
        parentKey: 'discovery',
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
