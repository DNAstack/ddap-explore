import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { share } from 'rxjs/operators';

import { AppConfigModel } from '../../../shared/app-config/app-config.model';
import { AppConfigService } from '../../../shared/app-config/app-config.service';
import { ResourceBeaconService } from '../../../shared/beacon-search/resource-beacon.service';
import { CollectionModel } from '../../../shared/collection.model';
import { ImagePlaceholderRetriever } from '../../../shared/image-placeholder.service';
import { ResourcesResponseModel } from '../../../shared/resource.model';
import { DataService } from '../../data.service';

@Component({
  selector: 'ddap-collection-detail',
  templateUrl: './collection-detail.component.html',
  styleUrls: ['./collection-detail.component.scss'],
  providers: [ImagePlaceholderRetriever, ResourceBeaconService],
})
export class CollectionDetailComponent implements OnInit {

  collectionResources$: Observable<ResourcesResponseModel>;
  collection$: Observable<CollectionModel>;
  searchOpened = false;
  limitSearch = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private appConfigService: AppConfigService,
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

}
