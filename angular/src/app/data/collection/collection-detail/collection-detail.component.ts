import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { share } from 'rxjs/operators';

import { CollectionModel } from '../../../shared/apps/collection.model';
import { InterfaceModel, ResourceModel, ResourcesResponseModel } from '../../../shared/apps/resource.model';
import { ResourceService } from '../../../shared/apps/resource.service';
import { ImagePlaceholderRetriever } from '../../../shared/image-placeholder.service';

@Component({
  selector: 'ddap-collection-detail',
  templateUrl: './collection-detail.component.html',
  styleUrls: ['./collection-detail.component.scss'],
  providers: [ImagePlaceholderRetriever],
})
export class CollectionDetailComponent implements OnInit {

  collection$: Observable<CollectionModel>;
  collectionResources$: Observable<ResourcesResponseModel>;
  searchOpened = false;
  limitSearch = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private resourceService: ResourceService
  ) {
  }

  ngOnInit() {
    const collectionId = this.route.snapshot.params.collectionId;
    this.collection$ = this.resourceService.getCollection(collectionId)
      .pipe(share());
    this.collectionResources$ = this.resourceService.getResources({ collection: collectionId });
  }

  isPublicResource(resource: ResourceModel): boolean {
    return resource.interfaces.some((resourceInterface: InterfaceModel) => {
      return !resourceInterface.authRequired;
    });
  }

}
