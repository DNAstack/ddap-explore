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

    return this.http.get<AppConfigModel>(`${environment.ddapAlphaApiUrl}/config`)
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
      title: '', // This is a placeholder and the actual value would be set by the backend service.
      logoUrl: null,
      googleAnalyticsId: null,
      theme: null,
      defaultModule: null,
      tosUrl: 'https://dnastack.com/#/legal/terms',
      inStandaloneMode: false,
      authorizationOnInitRequired: false,
      sidebarEnabled: true,
      featureRealmInputEnabled: true,
      featureAdministrationEnabled: true,
      featureTermsEnabled: true,
      featureExploreDataEnabled: true,
      featureBeaconsEnabled: true,
      featureDiscoveryEnabled: true,
      featureWorkflowsEnabled: true,
      featureSearchEnabled: true,
      trsBaseUrl: null,
      trsAcceptedToolClasses: [],
      trsAcceptedVersionDescriptorTypes: [],
      listPageSize: 14,
      covidBeaconUrl: null,
      search: {
        defaultQuery: null,
      },
    };
  }

  private registerModules() {
    this.viewController
      .registerModule({
        key: 'data',
        name: 'Data',
        iconName: 'search',
        requiredFeatureFlags: ['featureExploreDataEnabled'],
        routerLink: 'data',
        isApp: true,
        isSidebarEnabled: true,
      })
      .registerModule({
        key: 'data-explorer',
        name: 'Explorer',
        iconName: 'navigation',
        routerLink: 'data/explorer',
        parentKey: 'data',
        isApp: false,
        isExperimental: true,
        expFlag: 'demo',
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
        expFlag: 'saved',
      });

    this.viewController
      .registerModule({
        key: 'beacon',
        name: 'Beacon',
        iconName: 'wifi_tethering',
        requiredFeatureFlags: ['featureBeaconsEnabled'],
        routerLink: 'beacon',
        isApp: true,
        isSidebarEnabled: true,
      })
      .registerModule({
        key: 'network',
        name: 'Network',
        iconName: 'public',
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
        iconName: 'trending_up',
        requiredFeatureFlags: ['featureDiscoveryEnabled'],
        routerLink: 'discovery',
        isApp: true,
        isSidebarEnabled: true,
      })
      .registerModule({
        key: 'genomes',
        name: 'Genomes',
        iconName: 'sync_alt',
        routerLink: 'discovery/genomes',
        parentKey: 'discovery',
        isApp: false,
        isExperimental: true,
        expFlag: 'demo',
      })
      .registerModule({
        key: 'files',
        name: 'Files',
        iconName: 'layers',
        routerLink: 'discovery/sequences',
        parentKey: 'discovery',
        isApp: false,
        isExperimental: true,
        expFlag: 'demo',
      })
      .registerModule({
        key: 'variants',
        name: 'Variants',
        iconName: 'wifi_tethering',
        routerLink: 'discovery/beacon',
        parentKey: 'discovery',
        isApp: false,
        isExperimental: true,
        expFlag: 'demo',
      })
      .registerModule({
        key: 'molecules',
        name: 'Molecules',
        iconName: 'scatter_plot',
        routerLink: 'discovery/molecules',
        parentKey: 'discovery',
        isApp: false,
        isExperimental: true,
        expFlag: 'demo',
      });

    this.viewController
      .registerModule({
        key: 'analytics',
        name: 'Analytics',
        requiredFeatureFlags: ['featureWorkflowsEnabled'],
        iconName: 'memory',
        routerLink: 'analyze',  // FIXME Change to "analytics"
        isApp: true,
        isSidebarEnabled: true,
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

    this.viewController
      .registerModule({
        key: 'search',
        name: 'Search',
        iconName: 'search',
        requiredFeatureFlags: ['featureSearchEnabled'],
        routerLink: 'search',
        isApp: true,
        isExperimental: false,
        isSidebarEnabled: false,
      })
      .registerModule({
        key: 'search-resources',
        name: 'Resources',
        iconClasses: 'icon icon-resources',
        routerLink: 'search/resources',
        parentKey: 'search',
        isApp: false,
        isExperimental: false,
      });
  }
}
