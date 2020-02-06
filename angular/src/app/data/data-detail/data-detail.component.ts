import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EntityModel } from 'ddap-common-lib';
import { Observable, of } from 'rxjs';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import { AppConfigService } from '../../shared/app-config/app-config.service';
import { ResourceBeaconService } from '../../shared/beacon-search/resource-beacon.service';
import { ImagePlaceholderRetriever } from '../../shared/image-placeholder.service';
import { DataService } from '../data.service';

@Component({
  selector: 'ddap-resource-detail',
  templateUrl: './data-detail.component.html',
  styleUrls: ['./data-detail.component.scss'],
  providers: [ImagePlaceholderRetriever, ResourceBeaconService],
})
export class DataDetailComponent implements OnInit {

  resourceLabel$: Observable<string>;
  searchOpened = false;
  views: any;
  resource: EntityModel;
  limitSearch = true;
  damId: string;

  constructor(private route: ActivatedRoute,
              private appConfigService: AppConfigService,
              private router: Router,
              private dataService: DataService) {
  }

  ngOnInit() {
    // Ensure that the user can only access this component when it is enabled.
    this.appConfigService.get().subscribe((data: AppConfigModel) => {
      if (data.featureExploreDataEnabled) {
        this.initialize();
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

  private initialize() {
    const resourceName = this.route.snapshot.params.resourceName;
    const realmId = this.route.root.firstChild.snapshot.params.realmId;
    this.damId = this.route.snapshot.params.damId;

    this.dataService.getResource(this.damId, resourceName, realmId)
      .subscribe((resource) => {
        this.resource = resource;
        this.resourceLabel$ = of(this.resource.dto.ui.label);
        this.views = this.getViews(this.resource);
      });
  }

  private getViews(resource: EntityModel): EntityModel[] {
    return Object
      .keys(resource.dto.views)
      .map((key) => new EntityModel(key, resource.dto.views[key]));
  }
}
