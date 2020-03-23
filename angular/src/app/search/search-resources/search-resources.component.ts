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
  styleUrls: ['../../data/data-list/data-list.component.scss'],
  providers: [ImagePlaceholderRetriever],
})
export class SearchResourcesComponent implements OnInit {

  resources: any[];

  constructor(private searchService: SearchService,
              private randomImageRetriever: ImagePlaceholderRetriever,
              private router: Router,
              private resourceService: ResourceService,
              private damInfoService: DamInfoService,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.searchService.getSearchResources()
      .subscribe((resources) => {
        this.resources = resources;
        this.resources.map(resource => {
          this.getUrlForAccessToken(resource).subscribe(url => {
            resource['accessUrl'] = url;
          });
        });
      });
  }

  getUrlForAccessToken(resource: SearchResourceModel) {
    const realmId = this.route.root.firstChild.snapshot.params.realmId;
    const redirectUri = `/${realmId}/search/resource/${resource.resourceName}/views/${resource.viewName}?checkout=true`;
    const resourcesPath = [];
    return this.searchService.getResourceDetail(resource.resourceName)
      .pipe(
        map(views => {
          views.map(view => {
            resourcesPath.push(
              `1;${view.resourceName}/views/` +
              `${view.viewName}/roles/${view.roleName}/interfaces/${view.interfaceName}`
            );
          });
          return this.resourceService.getUrlForObtainingAccessToken(resourcesPath, redirectUri);
        })
      );
  }
}
