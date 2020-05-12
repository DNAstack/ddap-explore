import { Component, OnInit } from '@angular/core';

import { ImagePlaceholderRetriever } from '../../shared/image-placeholder.service';
import { SPICollection } from '../../shared/spi/collection.model';
import { SPIResource } from '../../shared/spi/resource.model';
import { SPIService } from '../../shared/spi/spi.service';

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
  resource: SPIResource;
  tableName: string;
}

@Component({
  selector: 'ddap-workspace-list',
  templateUrl: './workspace-list.component.html',
  styleUrls: ['./workspace-list.component.scss'],
})
export class WorkspaceListComponent implements OnInit {
  collections: SPICollection[];

  constructor(private spiService: SPIService,
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
