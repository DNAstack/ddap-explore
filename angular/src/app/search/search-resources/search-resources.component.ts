import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { ImagePlaceholderRetriever } from '../../shared/image-placeholder.service';
import { SearchService } from '../search.service';

@Component({
  selector: 'ddap-search-resources',
  templateUrl: './search-resources.component.html',
  styleUrls: ['../../data/data-list/data-list.component.scss'],
  providers: [ImagePlaceholderRetriever],
})
export class SearchResourcesComponent implements OnInit {

  resources: object[];
  constructor(private searchService: SearchService,
              private randomImageRetriever: ImagePlaceholderRetriever,
              private router: Router) { }

  ngOnInit() {
    this.searchService.getSearchResources()
      .subscribe(resources => this.resources = resources);
  }

}
