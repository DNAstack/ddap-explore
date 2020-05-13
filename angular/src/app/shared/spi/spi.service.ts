import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RealmStateService } from 'ddap-common-lib';
import _get from 'lodash.get';
import { Observable } from 'rxjs';

import { SPICollectionListResponseModel } from './collection-list-response.model';
import { SPICollection } from './collection.model';
import { ResourceListResponseModel } from './resource-list-response.model';

@Injectable({
  providedIn: 'root',
})
export class SPIService {
  constructor(private http: HttpClient,
              private activatedRoute: ActivatedRoute,
              private realmStateService: RealmStateService) {
  }

  getCollections(): Observable<SPICollectionListResponseModel> {
    return this.http.get<SPICollectionListResponseModel>(`/api/v1beta/${this.getRealmId()}/collections`);
  }

  getCollection(id: string): Observable<SPICollection> {
    return this.http.get<SPICollection>(`/api/v1beta/${this.getRealmId()}/collections/${id}`);
  }

  getResources(interfaceType: string): Observable<ResourceListResponseModel> {
    return this.http.get<ResourceListResponseModel>(`/api/v1beta/${this.getRealmId()}/resources?interface_type=${interfaceType}`);
  }

  private getRealmId() {
    return _get(this.activatedRoute, 'root.firstChild.snapshot.params.realmId', this.realmStateService.getRealm());
  }
}
