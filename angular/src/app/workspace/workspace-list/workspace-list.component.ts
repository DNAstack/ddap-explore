import { Component } from '@angular/core';

import { ImagePlaceholderRetriever } from '../../shared/image-placeholder.service';
import { Resource } from '../../shared/spi/resource.model';
import { SPIResourceService } from '../../shared/spi/resource.service';

@Component({
  selector: 'ddap-workspace-list',
  templateUrl: './workspace-list.component.html',
  styleUrls: ['./workspace-list.component.scss'],
})
export class WorkspaceListComponent {
  resources: Resource[];

  constructor(private resourceService: SPIResourceService,
              private randomImageRetriever: ImagePlaceholderRetriever) {
  }

  // ngOnInit() {
  //   this.initialize();
  // }

  initialize() {
    this.resourceService.find('http:search')
      .subscribe((resourceListResponse) => {
      });
  }
}
