import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RealmStateService } from 'ddap-common-lib';
import _get from 'lodash.get';
import { Observable } from 'rxjs';

import { CollectionModel, CollectionsResponseModel } from '../apps/collection.model';
import { ResourcesResponseModel } from '../apps/resource.model';

@Injectable({
  providedIn: 'root',
})
export class SPIService {
  constructor(private http: HttpClient,
              private activatedRoute: ActivatedRoute,
              private realmStateService: RealmStateService) {
  }

  getCollections(): Observable<CollectionsResponseModel> {
    return this.http.get<CollectionsResponseModel>(`/api/v1beta/${this.getRealmId()}/collections`);
  }

  getCollection(id: string): Observable<CollectionModel> {
    return this.http.get<CollectionModel>(`/api/v1beta/${this.getRealmId()}/collections/${id}`);
  }

  getResources(interfaceType: string): Observable<ResourcesResponseModel> {
    return this.http.get<ResourcesResponseModel>(`/api/v1beta/${this.getRealmId()}/resources?interface_type=${interfaceType}`);
  }

  private getRealmId() {
    return _get(this.activatedRoute, 'root.firstChild.snapshot.params.realmId', this.realmStateService.getRealm());
  }
}
