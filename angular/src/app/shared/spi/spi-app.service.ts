import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RealmStateService } from 'ddap-common-lib';
import _get from 'lodash.get';
import { Observable } from 'rxjs';

import { SPICollectionListResponseModel } from './collection-list-response.model';
import { ResourceListResponseModel } from './resource-list-response.model';

@Injectable({
  providedIn: 'root',
})
export class SPIAppService {
  constructor(private http: HttpClient,
              private activatedRoute: ActivatedRoute,
              private realmStateService: RealmStateService) {
  }

  getBeaconResources(collectionId: string): Observable<any> {
    return this.http.get<any>(`/api/v1beta/${this.getRealmId()}/apps/discovery/beacon/resources?collection=${collectionId}`);
  }

  getSimpleSearchResources(collectionId: string): Observable<any> {
    return this.http.get<any>(`/api/v1beta/${this.getRealmId()}/apps/search/simple/resources?collection=${collectionId}`);
  }

  private getRealmId() {
    return _get(this.activatedRoute, 'root.firstChild.snapshot.params.realmId', this.realmStateService.getRealm());
  }
}
