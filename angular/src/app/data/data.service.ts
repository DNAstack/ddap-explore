import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorHandlerService, realmIdPlaceholder } from 'ddap-common-lib';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { CollectionModel, CollectionsRequestModel, CollectionsResponseModel } from '../shared/collection.model';
import { ResourceModel, ResourcesRequestModel, ResourcesResponseModel } from '../shared/resource.model';

@Injectable({
  providedIn: 'root',
})
export class DataService {

  constructor(
    private http: HttpClient,
    private errorHandler: ErrorHandlerService
  ) {
  }

  getCollections(params?: CollectionsRequestModel): Observable<CollectionsResponseModel> {
    return this.http.get<CollectionsResponseModel>(
      `${environment.ddapApiUrl}/${realmIdPlaceholder}/collections`,
      { params }
    ).pipe(
      this.errorHandler.notifyOnError(`Can't load collections.`)
    );
  }

  getCollection(collectionId: string): Observable<CollectionModel> {
    return this.http.get<CollectionModel>(
      `${environment.ddapApiUrl}/${realmIdPlaceholder}/collections/${collectionId}`
    ).pipe(
      this.errorHandler.notifyOnError(`Can't load collection ${collectionId}.`)
    );
  }

  getResources(params: ResourcesRequestModel): Observable<ResourcesResponseModel> {
    return this.http.get<ResourcesResponseModel>(
      `${environment.ddapApiUrl}/${realmIdPlaceholder}/resources`,
      { params }
    ).pipe(
      this.errorHandler.notifyOnError(`Can't load resources.`)
    );
  }

  getResource(resourceId: string): Observable<ResourceModel> {
    return this.http.get<ResourceModel>(
      `${environment.ddapApiUrl}/${realmIdPlaceholder}/resources/${resourceId}`
    ).pipe(
      this.errorHandler.notifyOnError(`Can't load resource ${resourceId}.`)
    );
  }

}
