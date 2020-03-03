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

  getDamResourcePath(damId: string, resourceId: string, viewId: string, roleId: string, interfaceId: string): string {
    return `${damId};${resourceId}/views/${viewId}/roles/${roleId}/interfaces/${interfaceId}`;
  }

  getUrlForObtainingAccessToken(damIdResourcePathPairs: string[], redirectUri: string): string {
    const realmId = _get(this.activatedRoute, 'root.firstChild.snapshot.params.realmId', this.realmStateService.getRealm());
    const resources = damIdResourcePathPairs.map((resource) => {
      return `resource=${encodeURIComponent(resource)}`;
    });
    return `${environment.ddapApiUrl}/realm/${realmId}/resources/authorize?${resources.join('&')}`
      + `&redirectUri=${encodeURIComponent(redirectUri)}`;
  }

  getAccessTokensForAuthorizedResources(damIdResourcePathPairs: string[]): Observable<IResourceTokens> {
    const resources = damIdResourcePathPairs.map((resource) => {
      return `resource=${encodeURIComponent(resource)}`;
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
    const resourceToken: IResourceToken = accessMap[resourceKey];
    return this.validateResourceToken(resourceToken) ? resourceToken : null;
  }

  toResourceAccessMap(resourceTokens: IResourceTokens): {[key: string]: IResourceToken} {
    const accessMap = {};
    Object.entries(resourceTokens.resources)
      .forEach(([resource, value]) => {
        accessMap[resource] = resourceTokens.access[value.access];
      });
    return accessMap;
  }

  validateResourceToken(resourceToken: IResourceToken): boolean {
    return this.validateResourceTokenAsOf(resourceToken, Math.floor((new Date()).getTime() / 1000));
  }

  validateResourceTokenAsOf(resourceToken: IResourceToken, referenceUnixTimestampInSecond: number): boolean {
    if (!resourceToken || !resourceToken['access_token']) {
      return false;
    }
    try {
      const claims = JSON.parse(atob(resourceToken['access_token'].split('.')[1]));
      return claims.exp > referenceUnixTimestampInSecond;
    } catch (e) {
      // TODO returning true for non-jwt access tokens
      console.warn('Token cannot be validated');
      return true;
    }
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
