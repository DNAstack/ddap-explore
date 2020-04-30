import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { realmIdPlaceholder } from 'ddap-common-lib';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

import { TokensRequestModel, TokensResponseModel } from './app-explore.model';

@Injectable({
  providedIn: 'root',
})
export class AppExploreService {

  constructor(private http: HttpClient) {
  }

  getTokens(resourceInterfaceIds: string[], params?: TokensRequestModel): Observable<TokensResponseModel> {
    const resources = resourceInterfaceIds.map((resource) => {
      return `resource=${resource}`;
    });

    return this.http.get<TokensResponseModel>(
      `${environment.ddapApiUrl}/${realmIdPlaceholder}/apps/explore/tokens?${resources.join('&')}`,
      { params }
    );
  }

}
