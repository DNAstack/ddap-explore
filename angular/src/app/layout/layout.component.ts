import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { LoadingBarService } from '@ngx-loading-bar/core';
import { interval, Observable } from 'rxjs';
import { repeatWhen } from 'rxjs/operators';

import { IdentityService } from '../identity/identity.service';
import { IdentityStore } from '../identity/identity.store';
import { Profile } from '../identity/profile.model';
import { DamInfoStore } from '../shared/dam/dam-info.store';
import { DamInfo } from '../shared/dam/dams-info';

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
  damsInfo: DamInfo[] = [];
  icPath: string;

  constructor(public loader: LoadingBarService,
              private activatedRoute: ActivatedRoute,
              private identityService: IdentityService,
              private identityStore: IdentityStore,
              private damInfoStore: DamInfoStore) {
  }

  ngOnInit() {
    this.damInfoStore.getDamsInfo().subscribe(damsInfo => {
      for (const damId in damsInfo) {
        this.damsInfo.push(damsInfo[damId]);
      }
    });
    this.identityStore.getIdentity()
      .subscribe(({ account, sandbox }) => {
        this.isSandbox = sandbox;
        this.profile = account.profile;
      });
    this.identityService.getIcInfo().subscribe(icInfo => {
      this.icPath = icInfo.uiUrl;
    });

    this.activatedRoute.root.firstChild.params.subscribe((params) => {
      this.realm = params.realmId;
      this.loginPath = `/api/v1alpha/realm/${this.realm}/identity/login`;
    });

    // Workaround to get fresh cookies
    this.periodicallyRefreshTokens()
      .subscribe();
  }

  logout() {
    this.identityService.invalidateTokens()
      .subscribe(() => {
        window.location.href = `${this.loginPath}`;
      });
  }

  private periodicallyRefreshTokens(): Observable<any> {
    return this.identityService.refreshTokens()
      .pipe(
        repeatWhen(() => interval(refreshRepeatTimeoutInMs))
      );
  }

}
