/**
 * This is one of the area where user
 */
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';

import { AccessControlService, UserAccessGrantStatus } from '../shared/access-control.service';
import { AppConfigService } from '../shared/app-config/app-config.service';
import { DamInfoStore } from '../shared/dam/dam-info.store';

@Component({
  selector: 'ddap-lobby',
  templateUrl: './lobby.component.html',
  styleUrls: ['./lobby.component.scss'],
})
export class LobbyComponent implements OnInit {
  initialized = false;
  userIsAuthorized = false;
  authorizationRequestedOnDemand = false;
  eventType: string;
  realmId: string;

  constructor(
    private titleService: Title,
    private activatedRoute: ActivatedRoute,
    private damInfoStore: DamInfoStore,
    public appConfigService: AppConfigService,
    public accessControlService: AccessControlService
  ) {
  }

  ngOnInit(): void {
    this.realmId = this.activatedRoute.snapshot.params.realmId;
    this.eventType = this.activatedRoute.snapshot.queryParamMap.get('after');
    this.appConfigService
      .get()
      .subscribe(config => {
        this.titleService.setTitle(config.title);
        this.initialize();
      });
  }

  initialize() {
    this.damInfoStore
      .getDamsInfo()
      .subscribe(
        damsInfo => {
          this.accessControlService
            .assertAuthorizationOnInit(Object.keys(damsInfo).map(id => id), null)
            .then(userAccessGrantState => {
              if (userAccessGrantState === UserAccessGrantStatus.AUTHORIZED) {
                this.userIsAuthorized = true;
              } else if (userAccessGrantState === UserAccessGrantStatus.ON_DEMAND) {
                this.authorizationRequestedOnDemand = true;
              }

              this.initialized = true;
            });
        });
  }
}
