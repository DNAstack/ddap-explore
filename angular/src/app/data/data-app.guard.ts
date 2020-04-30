import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { AppConfigModel } from '../shared/app-config/app-config.model';
import { AppConfigStore } from '../shared/app-config/app-config.store';

@Injectable({
  providedIn: 'root',
})
export class DataAppGuard implements CanActivate {

  constructor(private appConfigStore: AppConfigStore) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.appConfigStore.state$
      .pipe(
        map((appConfig: AppConfigModel) => {
          return appConfig.featureExploreDataEnabled;
        })
      );
  }

}
