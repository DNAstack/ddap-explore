import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { defaultRealm } from 'ddap-common-lib';

import { AppConfigModel } from '../shared/app-config/app-config.model';
import { AppConfigService } from '../shared/app-config/app-config.service';

@Component({
  template: '',
})
export class CheckinComponent implements OnInit {
  private appConfig: AppConfigModel;
  constructor(private appConfigService: AppConfigService,
              private router: Router,
              private route: ActivatedRoute) {}

  ngOnInit() {
    const realmId = this.route.root.firstChild.snapshot.params.realmId;

    this.appConfig = this.appConfigService.getDefault();
    this.appConfigService.get().subscribe((data: AppConfigModel) => {
      this.appConfig = data;

      let targetRealm = defaultRealm;

      if (this.appConfig.inStandaloneMode) {
        targetRealm = 'app';
      } else if (realmId) {
        targetRealm = realmId;
      }

      this.router.navigate([`/${targetRealm}/${this.appConfig.defaultModule}`]);
    });
  }
}
