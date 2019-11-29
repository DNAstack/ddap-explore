import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorHandlerService, realmIdPlaceholder } from 'ddap-common-lib';
import { Observable } from 'rxjs';
import { flatMap, map } from 'rxjs/operators';

import { DamInfoService } from '../dam/dam-info.service';
import { HttpParamsService } from '../http-params.service';
import { dam } from '../proto/dam-service';
import IGetTokenRequest = dam.v1.IGetTokenRequest;
import ResourceToken = dam.v1.ResourceTokens.ResourceToken;

@Injectable({
  providedIn: 'root',
})
export class ResourceService {

  constructor(private http: HttpClient,
              private httpParamsService: HttpParamsService,
              private errorHandler: ErrorHandlerService,
              private damInfoService: DamInfoService) {
  }

  getAccessRequestToken(damId: string, resourceId: string, viewId: string, tokenRequest: IGetTokenRequest): Observable<ResourceToken> {
    return this.damInfoService.getDamUrls()
      .pipe(
        flatMap(damApiUrls => {
          const damApiUrl = damApiUrls.get(damId);
          return this.http.get<ResourceToken>(
            `${damApiUrl}/${realmIdPlaceholder}/resources/${resourceId}/views/${viewId}/token`,
            {
              params: this.httpParamsService.getHttpParamsFrom(tokenRequest),
            }
          ).pipe(
            this.errorHandler.notifyOnError(`Can't get access token.`),
            map(ResourceToken.create)
          );
        })
      );
  }

}
