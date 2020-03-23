import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { DamInfoService } from '../../shared/dam/dam-info.service';
import { ImagePlaceholderRetriever } from '../../shared/image-placeholder.service';
import { ResourceService } from '../../shared/resource/resource.service';
import { SearchService } from '../search.service';

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
      .subscribe((resources) => this.resources = resources);
  }

  getUrlForAccessToken(resource) {
    const realmId = this.route.root.firstChild.snapshot.params.realmId;
    const redirectUri = `/${realmId}/search/resource/${resource['resourceName']}?checkout=true`;
    const resourcePath = `1;${resource['resourceName']}/views/` +
    `${resource['viewName']}/roles/${resource['roleName']}/interfaces/${resource['interfaceName']}`;
    return this.resourceService.getUrlForObtainingAccessToken([resourcePath], redirectUri);
  }
}
