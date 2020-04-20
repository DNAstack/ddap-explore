import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { BeaconSearchResponseModel } from '../beacon-search.model';

@Component({
  selector: 'ddap-beacon-result',
  templateUrl: './beacon-result.component.html',
  styleUrls: ['./beacon-result.component.scss'],
})
export class BeaconResultComponent implements OnInit {

  @Input()
  beacon: BeaconSearchResponseModel;

  realm: string;

  constructor(private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.root.firstChild.params
      .subscribe((params) => {
        this.realm = params.realmId;
      });
  }

  getLinkToResource() {
    // FIXME: to be updated
    const { damId, resourceId } = this.beacon.beaconInfo;
    return `/${this.realm}/data/collections/${resourceId}`;
  }

}
