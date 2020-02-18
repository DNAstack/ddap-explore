import { ModuleMetadata } from 'ddap-common-lib';
import { ViewFilterInterface } from 'ddap-common-lib/lib/view-controller/view-filter.interface';

import { AppConfigModel } from './app-config.model';

export class AppFilterService implements ViewFilterInterface {
  config: AppConfigModel;

  constructor(config: AppConfigModel) {
    this.config = config;
  }

  isVisible(moduleMetadata: ModuleMetadata): boolean {
    const eligibleScore = moduleMetadata.requiredFeatureFlags.reduce(
      (previousValue, requiredFeatureFlag) => previousValue + (this.config[requiredFeatureFlag] ? 1 : 0),
      0
    );

    return eligibleScore >= moduleMetadata.requiredFeatureFlags.length;
  }
}
