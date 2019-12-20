import { Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { realmIdPlaceholder, RealmStateService } from "ddap-common-lib";
import _get from 'lodash.get';

import { environment } from '../../../environments/environment';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { dam } from "../proto/dam-service";
import ResourceTokens = dam.v1.ResourceTokens;

@Injectable({
  providedIn: 'root',
})
export class ResourceService {

  constructor(private http: HttpClient,
              private activatedRoute: ActivatedRoute,
              private realmStateService: RealmStateService) {
  }

  getDamResourcePath(damId: string, resourceId: string, viewId: string, roleId: string): string {
    return `${damId};${resourceId}/views/${viewId}/roles/${roleId}`;
  }

  getUrlForObtainingAccessToken(damId: string, resourceId: string, viewId: string, roleId: string, redirectUri: string): string {
    const realmId = _get(this.activatedRoute, 'root.firstChild.snapshot.params.realmId', this.realmStateService.getRealm());
    const resource = this.getDamResourcePath(damId, resourceId, viewId, roleId);
    return `${environment.ddapApiUrl}/${realmId}/resources/authorize?resource=${resource}&redirectUri=${redirectUri}`;
  }

  getAccessTokensForAuthorizedResources(resource: string): Observable<ResourceTokens> {
    return this.http.get<ResourceTokens>(
      `${environment.ddapApiUrl}/${realmIdPlaceholder}/resources/checkout`,
      { params: { resource }}
    );
  }

}
