import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { realmIdPlaceholder, RealmStateService } from 'ddap-common-lib';
import _get from 'lodash.get';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { dam } from '../proto/dam-service';
import IView = dam.v1.IView;
import IResourceResults = dam.v1.IResourceResults;
import IResourceAccess = dam.v1.ResourceResults.IResourceAccess;
import IResourceDescriptor = dam.v1.ResourceResults.IResourceDescriptor;

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

  getUrlForObtainingAccessToken(damIdResourcePathPairs: string[], redirectUri: string, ttl: string = '1h'): string {
    const realmId = _get(this.activatedRoute, 'root.firstChild.snapshot.params.realmId', this.realmStateService.getRealm());
    const resources = damIdResourcePathPairs.map((resource) => {
      return `resource=${encodeURIComponent(resource)}`;
    });
    return `${environment.ddapApiUrl}/realm/${realmId}/resources/authorize?${resources.join('&')}`
      + `&redirectUri=${encodeURIComponent(redirectUri)}&ttl=${ttl}`;
  }

  getAccessTokensForAuthorizedResources(damIdResourcePathPairs: string[]): Observable<IResourceResults> {
    const resources = damIdResourcePathPairs.map((resource) => {
      return `resource=${encodeURIComponent(resource)}`;
    });
    return this.http.get<IResourceResults>(
      `${environment.ddapApiUrl}/realm/${realmIdPlaceholder}/resources/checkout?${resources.join('&')}`
    );
  }

  lookupResourceToken(resourceTokens: IResourceResults, resourcePath: string): IResourceAccess {
    if (!resourceTokens) {
      return;
    }
    const resource = this.lookupResourceTokenDescriptor(resourceTokens, resourcePath);
    return resourceTokens.access[resource.access];
  }

  lookupResourceTokenFromAccessMap(accessMap: {[key: string]: IResourceAccess}, resourcePath: string): IResourceAccess {
    if (!accessMap) {
      console.warn('No access map');
      return null;
    }
    const resourceKey = Object.keys(accessMap)
      .find((key) => key.includes(resourcePath));
    const resourceToken: IResourceAccess = accessMap[resourceKey];

    if (!this.validateResourceToken(resourceToken)) {
      console.warn('Detected the token but it is not valid');
      return null;
    }

    return resourceToken;
  }

  toResourceAccessMap(resourceTokens: IResourceResults): {[key: string]: IResourceAccess} {
    const accessMap = {};
    Object.entries(resourceTokens.resources)
      .forEach(([resource, value]) => {
        accessMap[resource] = resourceTokens.access[value.access];
      });
    return accessMap;
  }

  validateResourceToken(resourceToken: IResourceAccess): boolean {
    return this.validateResourceTokenAsOf(resourceToken, Math.floor((new Date()).getTime() / 1000));
  }

  validateResourceTokenAsOf(resourceToken: IResourceAccess, referenceUnixTimestampInSecond: number): boolean {
    if (!resourceToken || !resourceToken.credentials['access_token']) {
      return false;
    }
    try {
      const claims = JSON.parse(atob(resourceToken.credentials['access_token'].split('.')[1]));
      return claims.exp > referenceUnixTimestampInSecond;
    } catch (e) {
      // Returning true for non-jwt access tokens
      console.warn('Token cannot be validated');
      return true;
    }
  }

  private lookupResourceTokenDescriptor(resourceTokens: IResourceResults, resourcePath: string): IResourceDescriptor {
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
