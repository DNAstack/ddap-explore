import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { EntityModel, ErrorHandlerService, realmIdPlaceholder } from 'ddap-common-lib';
import { Observable, of } from 'rxjs';
import { flatMap, map, pluck, tap } from 'rxjs/operators';

import { environment } from '../../environments/environment';
import { AppConfigService } from '../shared/app-config/app-config.service';
import { CollectionModel, CollectionsRequestModel, CollectionsResponseModel } from '../shared/collection.model';
import { DamInfoService } from '../shared/dam/dam-info.service';
import { ResourceModel, ResourcesRequestModel, ResourcesResponseModel } from '../shared/resource.model';

@Injectable({
  providedIn: 'root',
})
export class DataService {

  constructor(private http: HttpClient,
              private appConfigService: AppConfigService,
              private errorHandler: ErrorHandlerService,
              private damInfoService: DamInfoService) {

  }

  // TODO: to be removed
  getName(damId: string, resourceId: string): Observable<string> {
    return this.getResourceOld(damId, resourceId)
      .pipe(
        map((entity: EntityModel) => entity.dto.ui.label)
      );
  }

  // TODO: to be removed
  get(damId: string, params = {}): Observable<EntityModel[]> {
    return this.damInfoService.getDamUrls()
      .pipe(
        flatMap(damApiUrls => {
          const damApiUrl = damApiUrls.get(damId);

          return this.http.get<any>(`${damApiUrl}/${realmIdPlaceholder}/resources`, {params})
            .pipe(
              this.errorHandler.notifyOnError(`Can't load resources.`),
              pluck('resources'),
              map(EntityModel.objectToMap),
              map(EntityModel.arrayFromMap)
            );
        })
      );
  }

  // TODO: to be removed
  getResourceOld(damId: string, resourceId: string, realmId = null, params = {}): Observable<EntityModel> {
    return this.damInfoService.getDamUrls()
      .pipe(
        flatMap(damApiUrls => {
          const damApiUrl = damApiUrls.get(damId);
          return this.http.get<any>(
            `${damApiUrl}/${realmId || realmIdPlaceholder}/resources/${resourceId}`,
            {params}
          ).pipe(
            this.errorHandler.notifyOnError(`Can't load resource ${resourceId}.`),
            pluck('resource'),
            map((resource) => new EntityModel(resourceId, resource))
          );
        })
      );
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
