import { HttpErrorResponse } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LOCAL_STORAGE, StorageService } from 'ngx-webstorage-service';
import { Observable, throwError, zip } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { SimplifiedWesResourceViews } from '../workflows/workflow.model';
import { WorkflowService } from '../workflows/workflows.service';

import { AppConfigModel } from './app-config/app-config.model';
import { AppConfigService } from './app-config/app-config.service';
import { dam } from './proto/dam-service';
import IResourceToken = dam.v1.ResourceTokens.IResourceToken;
import { ResourceAuthStateService } from './resource-auth-state.service';
import { ResourceService } from './resource/resource.service';

export enum UserAccessGrantStatus {
  UNCHECKED, // The user authorization is unknown.
  CHECKING, // The user authorization is being validated.
  INITIATED_AUTHORIZATION,  // The user is unauthorized and in the process of being redirected to the login page.
  AUTHORIZED,  // The user is authorized.
  ON_DEMAND,  // The user authorization is on demand and there is no need to enforce the authorization on initialization.
  NOT_AUTHORIZED,  // The user is confirmed as unauthorized.
}

@Injectable({
  providedIn: 'root',
})
export class AccessControlService {
  userAccessGrantState: UserAccessGrantStatus = UserAccessGrantStatus.UNCHECKED;

  private cachedDamIdList: string[];
  private finalStatusList: UserAccessGrantStatus[] = [
    UserAccessGrantStatus.AUTHORIZED,
    UserAccessGrantStatus.INITIATED_AUTHORIZATION,
    UserAccessGrantStatus.NOT_AUTHORIZED,
    UserAccessGrantStatus.ON_DEMAND,
  ];

  constructor(
    @Inject(LOCAL_STORAGE) private storage: StorageService,
    private router: Router,
    private appConfigService: AppConfigService,
    private resourceService: ResourceService,
    private resourceAuthStateService: ResourceAuthStateService,
    private workflowService: WorkflowService
  ) {
  }

  enforceAuthorizationOnInitIfRequired(damIdList: string[]): Promise<UserAccessGrantStatus> {
    if (!damIdList) {
      damIdList = this.cachedDamIdList;
    }

    return new Promise<UserAccessGrantStatus>((resolve => {
      this.cachedDamIdList = damIdList;

      this.appConfigService.get().subscribe((data: AppConfigModel) => {
        if (this.userAccessGrantState !== UserAccessGrantStatus.UNCHECKED) {
          this.watchForFinalState(resolve);
          return;
        }

        this.userAccessGrantState = UserAccessGrantStatus.CHECKING;

        if (data.authorizationOnInitRequired) {
          this.enforceAuthorization(damIdList, resolve);
        } else {
          this.userAccessGrantState = UserAccessGrantStatus.ON_DEMAND;
          resolve(this.userAccessGrantState);
        }
      });
    }));
  }

  initiateAuthorizationFlow(damIdResourcePathPairList: string[]) {
    this.resourceService.getAccessTokensForAuthorizedResources(damIdResourcePathPairList)
      .pipe(
        map(this.resourceService.toResourceAccessMap),
        catchError((response: HttpErrorResponse) => {
          if (response.status === 401) {
            if (this.userAccessGrantState !== UserAccessGrantStatus.INITIATED_AUTHORIZATION) {
              this.userAccessGrantState = UserAccessGrantStatus.INITIATED_AUTHORIZATION;
              this.redirectToLoginPage(damIdResourcePathPairList);
            }

            return throwError('Not authenticated');
          } else {
            // return an observable with a user-facing error message
            return throwError('Something bad happened; please try again later.');
          }
        })
      )
      .subscribe((response) => {
        this.resourceAuthStateService.storeAccess(response);
        this.userAccessGrantState = UserAccessGrantStatus.AUTHORIZED;
      });
  }

  redirectToLoginPage(damIdResourcePathPairList: string[]) {
    window.location.href = this.resourceService.getUrlForObtainingAccessToken(
      damIdResourcePathPairList,
      this.router.routerState.snapshot.url
    );
  }

  isUserAuthorized() {
    return this.userAccessGrantState === UserAccessGrantStatus.AUTHORIZED;
  }

  private watchForFinalState(callback: any) {
    if (this.finalStatusList.indexOf(this.userAccessGrantState) > -1) {
      callback(this.userAccessGrantState);
    } else {
      setTimeout(() => this.watchForFinalState(callback), 1000);
    }
  }

  private enforceAuthorization(damIdList: string[], resolve: any) {
    const currentTime = Math.floor(Date.now() / 1000);

    // Fetch the list of potentially required resources.
    this.workflowService.getAllWesViews()
      .subscribe((wesResourceViews: SimplifiedWesResourceViews[]) => {
        const damIdResourcePathPairList = [];
        damIdList.forEach(
          damId => wesResourceViews.forEach(
            wrv => wrv.views.forEach(
              v => damIdResourcePathPairList.push(`${damId};${v.resourcePath}`)
            )
          )
        );

        // Get the access map
        const accessMap = this.resourceAuthStateService.getAccess();

        if (Object.keys(accessMap).length === 0) {
          this.initiateAuthorizationFlow(damIdResourcePathPairList);
        } else {
          let numberOfValidTokens = 0;

          Object.values(accessMap).forEach(access => {
            numberOfValidTokens += this.resourceService.validateResourceTokenAsOf(access, currentTime) ? 1 : 0;
          });

          if (damIdResourcePathPairList.length > numberOfValidTokens) {
            this.initiateAuthorizationFlow(damIdResourcePathPairList);
            // this.redirectToLoginPage(damIdResourcePathPairList);
          } else {
            this.userAccessGrantState = UserAccessGrantStatus.AUTHORIZED;
            resolve(this.userAccessGrantState);
          }
        }
      });
  }
}
