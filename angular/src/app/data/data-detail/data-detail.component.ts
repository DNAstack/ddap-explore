import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EntityModel } from 'ddap-common-lib';
import { Observable, of } from 'rxjs';
import { share } from 'rxjs/operators';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import { AppConfigService } from '../../shared/app-config/app-config.service';
import { ResourceBeaconService } from '../../shared/beacon-search/resource-beacon.service';
import { CollectionModel } from '../../shared/collection.model';
import { ImagePlaceholderRetriever } from '../../shared/image-placeholder.service';
import { ResourcesResponseModel } from '../../shared/resource.model';
import { DataService } from '../data.service';

@Component({
  selector: 'ddap-resource-detail',
  templateUrl: './data-detail.component.html',
  styleUrls: ['./data-detail.component.scss'],
  providers: [ImagePlaceholderRetriever, ResourceBeaconService],
})
export class DataDetailComponent implements OnInit {

  collectionResources$: Observable<ResourcesResponseModel>;
  collection$: Observable<CollectionModel>;

  resourceLabel$: Observable<string>;
  searchOpened = false;
  views: any;
  resource: EntityModel;
  limitSearch = true;
  damId: string;

  constructor(
    private route: ActivatedRoute,
    private appConfigService: AppConfigService,
    private router: Router,
    private dataService: DataService
  ) {
  }

  ngOnInit() {
    // Ensure that the user can only access this component when it is enabled.
    // FIXME: causing multiple subscriptions
    this.appConfigService.get().subscribe((data: AppConfigModel) => {
      if (data.featureExploreDataEnabled) {
        const collectionId = this.route.snapshot.params.collectionId;
        this.collection$ = this.dataService.getCollection(collectionId)
          .pipe(share());
        this.collectionResources$ = this.dataService.getResources({ collection: collectionId });
      } else {
        this.router.navigate(['/']);
      }
    });
  }

  searchOpenedChange($event) {
    this.searchOpened = $event;
  }

  toggleLimitSearch() {
    this.limitSearch = !this.limitSearch;
  }

}
