import { Injectable } from '@angular/core';
import { ViewControllerService } from 'ddap-common-lib';

import { FrontendApp } from '../../app-config/app-config.model';

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
        requiredFeatureFlags: [FrontendApp.data],
        routerLink: 'data',
        isApp: true,
        isSidebarEnabled: true,
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
        key: 'discovery',
        name: 'Discovery',
        iconName: 'trending_up',
        requiredFeatureFlags: [FrontendApp.discovery],
        routerLink: 'discovery',
        isApp: true,
        isSidebarEnabled: false,
      });

    this.viewController
      .registerModule({
        key: 'analytics',
        name: 'Analytics',
        requiredFeatureFlags: [FrontendApp.workflows],
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
        requiredFeatureFlags: [FrontendApp.search],
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
