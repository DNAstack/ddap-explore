import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { LoadingBarService } from '@ngx-loading-bar/core';
import { interval, Observable } from 'rxjs';
import { repeatWhen } from 'rxjs/operators';

import { IdentityService } from '../identity/identity.service';
import { IdentityStore } from '../identity/identity.store';
import { Profile } from '../identity/profile.model';
import { AppConfigModel } from '../shared/app-config/app-config.model';
import { AppConfigService } from '../shared/app-config/app-config.service';
import { DamInfoStore } from '../shared/dam/dam-info.store';
import { DamsInfo } from '../shared/dam/dams-info';

const refreshRepeatTimeoutInMs = 600000;

@Component({
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent implements OnInit {

  isSandbox = false;
  profile: Profile = null;
  realm: string;
  loginPath: string;
  appConfig: AppConfigModel = null;

  dataAccessManagersInfo$: Observable<DamsInfo>;
  identityConcentratorInfo$: Observable<any>;

  constructor(public loader: LoadingBarService,
              private titleService: Title,
              private router: Router,
              private http: HttpClient,
              private activatedRoute: ActivatedRoute,
              private appConfigService: AppConfigService,
              private identityService: IdentityService,
              private identityStore: IdentityStore,
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
    this.identityStore.getIdentity()
      .subscribe(({ account, sandbox }) => {
        this.isSandbox = sandbox;
        this.profile = account.profile;
      });

    this.dataAccessManagersInfo$ = this.damInfoStore.getDamsInfo();
    this.identityConcentratorInfo$ = this.identityService.getIdentityConcentratorInfo();

    this.activatedRoute.root.firstChild.params.subscribe((params) => {
      this.realm = params.realmId;
      this.loginPath = this.getLoginPath(this.realm);
    });

    // Workaround to get fresh cookies
    this.periodicallyRefreshTokens()
      .subscribe();

    // TODO Refactoring this to its own service.
    if (this.appConfig.authorizationOnInitRequired) {
      const rawResourceTokens = localStorage.getItem('RESOURCE_TOKENS');
      try {
        const tokens = JSON.parse(JSON.parse(rawResourceTokens));
        for (const v of Object.values(tokens)) {
          const accessToken: string = v['access_token'];
          const unverifiedClaims: Map<string, any> = JSON.parse(atob(accessToken.split('.')[1]));
          if (unverifiedClaims['exp'] < (Date.now() / 1000)) {
            // this.redirectToLoginPage();
          }
        }
      } catch {
        // this.redirectToLoginPage();
      }
    }
  }

  redirectToLoginPage() {
    this.router.navigateByUrl(this.getLoginPath(this.activatedRoute.root.firstChild.snapshot.params.realmId));
  }

  logout() {
    this.identityService.invalidateTokens()
      .subscribe(() => {
        window.location.href = `${this.loginPath}`;
      });
  }

  withRealm(uiUrl: string) {
    if (uiUrl !== null && uiUrl !== undefined) {
      return uiUrl.replace(new RegExp('/$'), '') + '/' + this.realm;
    } else {
      return uiUrl;
    }
  }

  private getLoginPath(realmId: string): string {
    return `/api/v1alpha/realm/${realmId}/identity/login`;
  }

  private periodicallyRefreshTokens(): Observable<any> {
    return this.identityService.refreshTokens()
      .pipe(
        repeatWhen(() => interval(refreshRepeatTimeoutInMs))
      );
  }
}
