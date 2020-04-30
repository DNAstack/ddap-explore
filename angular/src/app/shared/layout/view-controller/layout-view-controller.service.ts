import { Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ViewControllerService } from 'ddap-common-lib';
import { filter, tap } from 'rxjs/operators';

import { AppConfigModel } from '../../app-config/app-config.model';
import { AppConfigStore } from '../../app-config/app-config.store';

import { LayoutAppsMenuFilterService } from './layout-apps-menu-filter.service';
import { LayoutViewRegistrationService } from './layout-view-registration.service';
import { LayoutViewStateModel } from './layout-view-state.model';
import { LayoutViewStateService } from './layout-view-state.service';

@Injectable({
  providedIn: 'root',
})
export class LayoutViewControllerService {

  initialized = false;

  constructor(
    private activatedRoute: ActivatedRoute,
    private appConfigStore: AppConfigStore,
    private appViewStateService: LayoutViewStateService,
    private layoutViewRegistrationService: LayoutViewRegistrationService,
    private viewController: ViewControllerService
  ) {
  }

  initView(): void {
    // Do not allow multiple initialization.
    if (this.initialized) {
      return;
    }

    this.setUpFlagsBasedOnQueryParams();
    this.setUpAppsMenuFilter();

    this.setExperimentalFlag();
    this.layoutViewRegistrationService.registerModules();

    this.initialized = true;
  }

  private setUpFlagsBasedOnQueryParams() {
    this.activatedRoute.queryParams
      .pipe(
        filter((params) => 'exp_flag' in params),
        tap((params) => {
          this.appViewStateService.storeViewFlags({ exp_flag: params.exp_flag });
        })
      )
      .subscribe(() => this.setExperimentalFlag());
  }

  private setExperimentalFlag() {
    const flags: LayoutViewStateModel = this.appViewStateService.getViewFlags();
    this.viewController.setExperimentalFlag(flags.exp_flag);
  }

  private setUpAppsMenuFilter() {
    this.appConfigStore.state$
      .subscribe((appConfig: AppConfigModel) => {
        this.viewController.addFilter(new LayoutAppsMenuFilterService(appConfig));
      });
  }

}
