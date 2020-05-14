import { Component, OnInit } from '@angular/core';

import { CollectionModel } from '../../shared/apps/collection.model';
import { ResourceModel } from '../../shared/apps/resource.model';
import { ResourceService } from '../../shared/apps/resource.service';
import { ImagePlaceholderRetriever } from '../../shared/image-placeholder.service';

interface WorkspaceMetadata {
  alias: string;
  name: string;
  logo: string;
  description: string;
  authRequired: boolean;
  simpleSearchList: SimpleSearchMetadata[];
}

interface SimpleSearchMetadata {
  name: string;
  resource: ResourceModel;
  tableName: string;
}

@Component({
  selector: 'ddap-workspace-list',
  templateUrl: './workspace-list.component.html',
  styleUrls: ['./workspace-list.component.scss'],
})
export class WorkspaceListComponent implements OnInit {
  collections: CollectionModel[];

  constructor(private spiService: ResourceService,
              private randomImageRetriever: ImagePlaceholderRetriever) {
  }

  ngOnInit() {
    this.initialize();
  }

  initialize() {
    this.spiService.getCollections().subscribe(collectionListResponseModel => {
      this.collections = collectionListResponseModel.data;
    });
  }
}
