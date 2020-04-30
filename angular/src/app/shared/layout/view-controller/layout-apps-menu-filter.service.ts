import { ModuleMetadata } from 'ddap-common-lib';
import { ViewFilterInterface } from 'ddap-common-lib/lib/view-controller/view-filter.interface';

import { AppConfigModel } from '../../app-config/app-config.model';

export class LayoutAppsMenuFilterService implements ViewFilterInterface {

  readonly config: AppConfigModel;

  constructor(config: AppConfigModel) {
    this.config = config;
  }

  isVisible(moduleMetadata: ModuleMetadata): boolean {
    const eligibleScore = moduleMetadata.requiredFeatureFlags.reduce(
      (previousValue, requiredFeatureFlag) => {
        const enabledApps: string[] = Object.values(this.config.enabledApps);
        return previousValue + (enabledApps.includes(requiredFeatureFlag) ? 1 : 0);
      }, 0
    );

    return eligibleScore >= moduleMetadata.requiredFeatureFlags.length;
  }
}
