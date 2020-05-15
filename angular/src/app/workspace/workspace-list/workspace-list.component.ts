import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { realmIdPlaceholder } from 'ddap-common-lib';

import { CollectionModel } from '../../shared/apps/collection.model';
import { ResourceModel } from '../../shared/apps/resource.model';
import { ResourceService } from '../../shared/apps/resource.service';
import { ImagePlaceholderRetriever } from '../../shared/image-placeholder.service';

@Component({
  selector: 'ddap-workspace-list',
  templateUrl: './workspace-list.component.html',
  styleUrls: ['./workspace-list.component.scss'],
})
export class WorkspaceListComponent implements OnInit {
  collections: CollectionModel[];

  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private resourceService: ResourceService,
              private randomImageRetriever: ImagePlaceholderRetriever) {
  }

  ngOnInit() {
    this.initialize();
  }

  initialize() {
    this.resourceService.getCollections().subscribe(collectionListResponseModel => {
      this.collections = collectionListResponseModel.data;

      // If there is only one collection, automatically redirect to that collection.
      if (this.collections.length === 1) {
        this.router.navigate(
          ['..', this.collections[0].id],
          {
            relativeTo: this.activatedRoute,
          }
        );
      }
    });
  }
}
