import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { LoadingBarService } from '@ngx-loading-bar/core';
import { ViewControllerService } from 'ddap-common-lib';
import { Observable } from 'rxjs';

import { IdentityService } from '../../identity/identity.service';
import { AccessControlService, UserAccessGrantStatus } from '../access-control.service';
import { AppConfigModel } from '../app-config/app-config.model';
import { AppConfigService } from '../app-config/app-config.service';
import { DamInfoStore } from '../dam/dam-info.store';
import { DamsInfo } from '../dam/dams-info';

@Component({
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent implements OnInit {

  realm: string;
  appConfig: AppConfigModel = null;

  userIsAuthorized: boolean = null;  // "null" means undecided or on-demand.

  dataAccessManagersInfo$: Observable<DamsInfo>;
  identityConcentratorInfo$: Observable<any>;

  constructor(public loader: LoadingBarService,
              public router: Router,
              private titleService: Title,
              private http: HttpClient,
              private activatedRoute: ActivatedRoute,
              public appConfigService: AppConfigService,
              public accessControlService: AccessControlService,
              private identityService: IdentityService,
              public viewController: ViewControllerService,
              private damInfoStore: DamInfoStore) {
  }

  ngOnInit() {
    this.appConfig = this.appConfigService.getDefault();
    this.appConfigService.get().subscribe((data: AppConfigModel) => {
      this.appConfig = data;
      this.titleService.setTitle(this.appConfig.title);

      this.initialize();
    });
  }

  initialize() {
    this.dataAccessManagersInfo$ = this.damInfoStore.getDamsInfo();
    this.identityConcentratorInfo$ = this.identityService.getIdentityConcentratorInfo();

    this.activatedRoute.root.firstChild.params.subscribe((params) => {
      this.realm = params.realmId;

      this.dataAccessManagersInfo$.subscribe(
        damsInfo => {
          this.accessControlService
            .enforceAuthorizationOnInitIfRequired(Object.keys(damsInfo).map(id => id))
            .then(userAccessGrantState => {
              if (userAccessGrantState === UserAccessGrantStatus.AUTHORIZED) {
                this.userIsAuthorized = true;
              } else if (userAccessGrantState === UserAccessGrantStatus.NOT_AUTHORIZED) {
                this.userIsAuthorized = false;
              }
            });
        });
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

  onSignOut() {
    const realmId = this.activatedRoute.snapshot.params.realmId;
    this.http.get(`/api/v1alpha/realm/${realmId}/resources/deauthorize`)
      .subscribe(observer => {
        this.accessControlService.purgeSession();
        this.router.navigate(['/', realmId, 'lobby'], {queryParams: {after: 'deauthorization'}});
      });
  }

  private changeRealmAndGoToLogin(realm) {
    this.router.navigate(['/', realm]);
  }

}
