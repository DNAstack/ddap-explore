import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { defaultRealm } from 'ddap-common-lib';
import _get from 'lodash.get';

import { AppConfigModel } from '../app-config/app-config.model';
import { AppConfigStore } from '../app-config/app-config.store';

@Component({
  template: '',
})
export class CheckinComponent implements OnInit {

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private appConfigStore: AppConfigStore
  ) {}

  ngOnInit() {
    this.appConfigStore.state$
      .subscribe((appConfig: AppConfigModel) => {
        if (appConfig.inStandaloneMode) {
          this.router.navigate([`/app/${appConfig.defaultRoute}`]);
        } else {
          const realm = _get(this.activatedRoute, 'root.firstChild.snapshot.params.realmId', defaultRealm);
          this.router.navigate([`/${realm}/${appConfig.defaultRoute}`]);
        }
      });
  }
}
