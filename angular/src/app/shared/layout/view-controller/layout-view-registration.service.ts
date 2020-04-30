import { Injectable } from '@angular/core';
import { ViewControllerService } from 'ddap-common-lib';

@Injectable({
  providedIn: 'root',
})
export class LayoutViewRegistrationService {

  constructor(private viewController: ViewControllerService) {
  }

  registerModules() {
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
        isSidebarEnabled: false,
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
