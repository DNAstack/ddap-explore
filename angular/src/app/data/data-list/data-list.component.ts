import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import { AppConfigService } from '../../shared/app-config/app-config.service';
import { CollectionsResponseModel } from '../../shared/collection.model';
import { ImagePlaceholderRetriever } from '../../shared/image-placeholder.service';
import { DataService } from '../data.service';


@Component({
  selector: 'ddap-data-list',
  templateUrl: './data-list.component.html',
  styleUrls: ['./data-list.component.scss'],
  providers: [ImagePlaceholderRetriever],
})
export class DataListComponent implements OnInit {

  collections$: Observable<CollectionsResponseModel>;

  constructor(
    public randomImageRetriever: ImagePlaceholderRetriever,
    private dataService: DataService,
    private route: ActivatedRoute,
    private router: Router,
    private appConfigService: AppConfigService
  ) {
  }

  ngOnInit() {
    // Ensure that the user can only access this component when it is enabled.
    // FIXME: causing multiple subscriptions
    this.appConfigService.get()
      .subscribe((data: AppConfigModel) => {
      if (data.featureExploreDataEnabled) {
        this.collections$ = this.dataService.getCollections();
      } else {
        this.router.navigate(['/']);
      }
    });
  }

  // TODO: Move to common lib
  ellipseIfLongerThan(text: string, maxLength: number): string {
    if (text && text.length > maxLength) {
      return `${text.substring(0, maxLength)}...`;
    }
    return text;
  }

}
