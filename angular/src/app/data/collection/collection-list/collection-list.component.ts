import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { CollectionsResponseModel } from '../../../shared/apps/collection.model';
import { ResourceService } from '../../../shared/apps/resource.service';
import { ImagePlaceholderRetriever } from '../../../shared/image-placeholder.service';


@Component({
  selector: 'ddap-collection-list',
  templateUrl: './collection-list.component.html',
  styleUrls: ['./collection-list.component.scss'],
  providers: [ImagePlaceholderRetriever],
})
export class CollectionListComponent implements OnInit {

  collections$: Observable<CollectionsResponseModel>;

  constructor(
    public randomImageRetriever: ImagePlaceholderRetriever,
    private resourceService: ResourceService
  ) {
  }

  ngOnInit() {
    this.collections$ = this.resourceService.getCollections();
  }

  // TODO: Move to common lib
  ellipseIfLongerThan(text: string, maxLength: number): string {
    if (text && text.length > maxLength) {
      return `${text.substring(0, maxLength)}...`;
    }
    return text;
  }

}
