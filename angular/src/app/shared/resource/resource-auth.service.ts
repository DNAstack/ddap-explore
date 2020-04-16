import IResourceResults = dam.v1.IResourceResults;
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { realmIdPlaceholder } from 'ddap-common-lib';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { dam } from '../proto/dam-service';

@Injectable({
  providedIn: 'root',
})
export class ResourceAuthService {

  constructor(private http: HttpClient) {
  }

  checkoutAuthorizedResources(resourceAuthorizationIds: string[]): Observable<IResourceResults> {
    const resources = resourceAuthorizationIds.map((resource) => {
      return `resource=${resource}`;
    });

    return this.http.get<IResourceResults>(
      `${environment.ddapApiUrl}/${realmIdPlaceholder}/resources/checkout?${resources.join('&')}`
    );
  }

}
