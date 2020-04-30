import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { map, shareReplay } from 'rxjs/operators';

import { DamInfoService } from '../../shared/dam/dam-info.service';
import { ImagePlaceholderRetriever } from '../../shared/image-placeholder.service';
import { ResourceService } from '../../shared/resource/resource.service';
import { SearchService } from '../search.service';

import { SearchResourceModel } from './search-resource.model';

@Component({
  selector: 'ddap-search-resources',
  templateUrl: './search-resources.component.html',
  styleUrls: ['./search-resources.component.scss'],
  providers: [ImagePlaceholderRetriever],
})
export class SearchResourcesComponent implements OnInit {

  resources: SearchResourceModel[];

  constructor(private searchService: SearchService,
              private randomImageRetriever: ImagePlaceholderRetriever,
              private router: Router,
              private resourceService: ResourceService,
              private damInfoService: DamInfoService,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.initialize();
  }

  initialize() {
    this.searchService.getSearchResources()
      .subscribe((resources) => {
        this.resources = resources;
        this.resources.map(resource => {
          if (resource['ui']['accessControlType'] === 'none' && resource['interfaceUri']) {
            resource['accessUrl'] = this.getViewUri(resource);
          } else {
            this.getUrlForAccessToken(resource).subscribe(url => {
              resource['accessUrl'] = url;
            });
          }
        });
      });
  }

  getUrlForAccessToken(resource: SearchResourceModel) {
    const realmId = this.route.root.firstChild.snapshot.params.realmId;
    const redirectUri = this.getViewUri(resource);
    const resourcesPath = [];
    return this.searchService.getResourceDetail(resource.resourceName)
      .pipe(
        map(views => {
          views.map(view => {
            resourcesPath.push(this.searchService.buildResourcePath(resource.damId, view));
          });
          return this.resourceService.getUrlForObtainingAccessToken(resourcesPath, redirectUri);
        })
      );
  }

  getViewUri(resource: SearchResourceModel): string {
    const realmId = this.route.root.firstChild.snapshot.params.realmId;
    return `/${realmId}/search/${resource.damId}/resource/${resource.resourceName}/views/${resource.viewName}?checkout=true`;
  }
}
