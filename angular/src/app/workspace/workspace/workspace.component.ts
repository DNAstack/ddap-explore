import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { SPIAppService } from '../../shared/spi/spi-app.service';

@Component({
  selector: 'ddap-workspace',
  templateUrl: './workspace.component.html',
  styleUrls: ['./workspace.component.scss'],
})
export class WorkspaceComponent implements OnInit {
  constructor(private activatedRoute: ActivatedRoute,
              private spiAppService: SPIAppService) {
  }

  ngOnInit() {
    this.initialize();
  }

  private initialize() {
    const collectionId = this.activatedRoute.snapshot.paramMap.get('collectionId');
    this.spiAppService.getBeaconResources(collectionId).subscribe(o => {
      console.warn(o);
    });
    this.spiAppService.getSimpleSearchResources(collectionId).subscribe(o => {
      console.warn(o);
    });
  }
}
