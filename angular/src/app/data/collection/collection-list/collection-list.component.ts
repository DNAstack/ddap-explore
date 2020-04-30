import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { CollectionsResponseModel } from '../../../shared/collection.model';
import { ImagePlaceholderRetriever } from '../../../shared/image-placeholder.service';
import { DataService } from '../../data.service';


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
    private dataService: DataService
  ) {
  }

  ngOnInit() {
    this.collections$ = this.dataService.getCollections();
  }

  // TODO: Move to common lib
  ellipseIfLongerThan(text: string, maxLength: number): string {
    if (text && text.length > maxLength) {
      return `${text.substring(0, maxLength)}...`;
    }
    return text;
  }

}
