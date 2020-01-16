import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { realmIdPlaceholder, RealmStateService } from 'ddap-common-lib';
import _get from 'lodash.get';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { dam } from '../proto/dam-service';
import ResourceTokens = dam.v1.ResourceTokens;
import IResourceToken = dam.v1.ResourceTokens.IResourceToken;
import IResourceTokens = dam.v1.IResourceTokens;
import IView = dam.v1.IView;

@Injectable({
  providedIn: 'root',
})
export class ResourceService {

  constructor(private http: HttpClient,
              private activatedRoute: ActivatedRoute,
              private realmStateService: RealmStateService) {
  }

  getView(damId: string, viewId: string): Observable<IView> {
    return this.http.get<IView>(
      `${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/dams/${damId}/views/${viewId}`
    );
  }

  getDamResourcePath(damId: string, resourceId: string, viewId: string, roleId: string): string {
    return `${damId};${resourceId}/views/${viewId}/roles/${roleId}`;
  }

  getUrlForObtainingAccessToken(damIdResourcePathPairs: string[], redirectUri: string): string {
    const realmId = _get(this.activatedRoute, 'root.firstChild.snapshot.params.realmId', this.realmStateService.getRealm());
    const resources = damIdResourcePathPairs.map((resource) => {
      return `resource=${resource}`;
    });
    return `${environment.ddapApiUrl}/realm/${realmId}/resources/authorize?${resources.join('&')}&redirectUri=${redirectUri}`;
  }

  getAccessTokensForAuthorizedResources(damIdResourcePathPairs: string[]): Observable<IResourceTokens> {
    const resources = damIdResourcePathPairs.map((resource) => {
      return `resource=${resource}`;
    });
    return this.http.get<IResourceTokens>(
      `${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/resources/checkout?${resources.join('&')}`
    );
  }

  lookupResourceToken(resourceTokens: IResourceTokens, resourcePath: string): IResourceToken {
    if (!resourceTokens) {
      return;
    }
    const resource = this.lookupResourceTokenDescriptor(resourceTokens, resourcePath);
    return resourceTokens.access[resource.access];
  }

  lookupResourceTokenFromAccessMap(accessMap: {[key: string]: IResourceToken}, resourcePath: string): IResourceToken {
    if (!accessMap) {
      return;
    }
    const resourceKey = Object.keys(accessMap)
      .find((key) => key.includes(resourcePath));
    return accessMap[resourceKey];
  }

  toResourceAccessMap(resourceTokens: IResourceTokens): {[key: string]: IResourceToken} {
    const accessMap = {};
    Object.entries(resourceTokens.resources)
      .forEach(([resource, value]) => {
        accessMap[resource] = resourceTokens.access[value.access];
      });
    return accessMap;
  }

  private lookupResourceTokenDescriptor(resourceTokens: IResourceTokens, resourcePath: string): ResourceTokens.IDescriptor {
    if (!resourceTokens) {
      return;
    }
    const resourceKey: any = Object.keys(resourceTokens.resources)
      .find((key) => {
        return key.includes(resourcePath);
      });
    return resourceTokens.resources[resourceKey];
  }

}
