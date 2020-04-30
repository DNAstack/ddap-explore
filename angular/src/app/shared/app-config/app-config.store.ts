import { Injectable } from '@angular/core';
import { Store } from 'ddap-common-lib';

import { AppConfigModel } from './app-config.model';
import { AppConfigService } from './app-config.service';

@Injectable({
  providedIn: 'root',
})
export class AppConfigStore extends Store<AppConfigModel> {

  constructor(private appConfigService: AppConfigService) {
    super(null);
    this.init();
  }

  private init() {
    if (!this.state) {
      this.appConfigService.getConfig()
        .subscribe((appConfig: AppConfigModel) => {
          this.setState(appConfig);
        });
    }
  }

}
