import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { LoadingBarService } from '@ngx-loading-bar/core';
import { Observable } from 'rxjs';

import { IdentityService } from '../identity/identity.service';
import { AccessControlService } from '../shared/access-control.service';
import { AppConfigModel } from '../shared/app-config/app-config.model';
import { AppConfigService } from '../shared/app-config/app-config.service';
import { DamInfoStore } from '../shared/dam/dam-info.store';
import { DamsInfo } from '../shared/dam/dams-info';

@Component({
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent implements OnInit {

  realm: string;
  appConfig: AppConfigModel = null;

  dataAccessManagersInfo$: Observable<DamsInfo>;
  identityConcentratorInfo$: Observable<any>;

  constructor(public loader: LoadingBarService,
              private titleService: Title,
              private router: Router,
              private http: HttpClient,
              private activatedRoute: ActivatedRoute,
              private appConfigService: AppConfigService,
              private accessControlService: AccessControlService,
              private identityService: IdentityService,
              private damInfoStore: DamInfoStore) {
  }

  ngOnInit() {
    this.appConfig = this.appConfigService.getDefault();
    this.appConfigService.get().subscribe((data: AppConfigModel) => {
      this.appConfig = data;
      this.titleService.setTitle(this.appConfig.title);

      this.initializeInNormalMode();
    });
  }

  initializeInNormalMode() {
    this.dataAccessManagersInfo$ = this.damInfoStore.getDamsInfo();
    this.identityConcentratorInfo$ = this.identityService.getIdentityConcentratorInfo();

    this.activatedRoute.root.firstChild.params.subscribe((params) => {
      this.realm = params.realmId;

      this.dataAccessManagersInfo$.subscribe(
        damsInfo => this.accessControlService
          .enforceAuthorizationOnInitIfRequired(Object.keys(damsInfo).map(id => id)))
      ;
    });
  }

  withRealm(uiUrl: string) {
    if (uiUrl !== null && uiUrl !== undefined) {
      return uiUrl.replace(new RegExp('/$'), '') + '/' + this.realm;
    } else {
      return uiUrl;
    }
  }

  onAcknowledge(dialogData) {
    if (!dialogData) {
      return;
    }

    if (dialogData.action === 'edit') {
      this.changeRealmAndGoToLogin(dialogData.realm);
    }
  }

  private changeRealmAndGoToLogin(realm) {
    this.router.navigate(['/', realm]);
  }

}
