import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { share } from 'rxjs/operators';

import { CollectionModel } from '../../../shared/collection.model';
import { ImagePlaceholderRetriever } from '../../../shared/image-placeholder.service';
import { InterfaceModel, ResourceModel, ResourcesResponseModel } from '../../../shared/resource.model';
import { DataService } from '../../data.service';

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
    private dataService: DataService
  ) {
  }

  ngOnInit() {
    const collectionId = this.route.snapshot.params.collectionId;
    this.collection$ = this.dataService.getCollection(collectionId)
      .pipe(share());
    this.collectionResources$ = this.dataService.getResources({ collection: collectionId });
  }

  isPublicResource(resource: ResourceModel): boolean {
    return resource.interfaces.some((resourceInterface: InterfaceModel) => {
      return !resourceInterface.authRequired;
    });
  }

}
