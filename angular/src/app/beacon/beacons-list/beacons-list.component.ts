import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import { AppConfigService } from '../../shared/app-config/app-config.service';
import { BeaconConfigService } from '../beacon-config.service';
import { BeaconNetworkService } from '../beacon-service/beacon-network.service';
import { Beacon, BeaconRegistry } from '../beacon-service/beacon.model';

@Component({
  selector: 'ddap-beacon-list',
  templateUrl: './beacons-list.component.html',
  styleUrls: ['./beacons-list.component.scss'],
})
export class BeaconListComponent implements OnInit {
  appConfig: AppConfigModel;
  registries: BeaconRegistry[];
  registry: BeaconRegistry;
  beacons: Beacon[];

  view: {
    isRefreshingBeacons: boolean,
    errorLoadingBeacons: boolean,
    wrapTableContent: boolean
  };

  constructor(private router: Router,
              private appConfigService: AppConfigService,
              private beaconConfigService: BeaconConfigService,
              private beaconNetworkService: BeaconNetworkService
              ) {

                this.view = {
                  isRefreshingBeacons : false,
                  errorLoadingBeacons : false,
                  wrapTableContent : false,
                };
  }

  ngOnInit(): void {
    // Ensure that the user can only access this component when it is enabled.
    this.appConfigService.get().subscribe((data: AppConfigModel) => {
      this.appConfig = data;
      if (this.appConfig.featureBeaconsEnabled) {
        this.initialize();
      } else {
        this.router.navigate(['/']);
      }
    });
  }

  private initialize() {
    this.registries = this.beaconConfigService.getRegistries();
    if (this.registries.length > 0) {
      this.setRegistry(this.registries[0]);
    }
  }

  private setRegistry(r: BeaconRegistry) {
    this.registry = r;
    this.beaconNetworkService.setApiUrl(r.apiUrl);

    // Refresh Beacons
    this.view.errorLoadingBeacons = false;
    this.view.isRefreshingBeacons = true;
    this.beaconNetworkService.getBeacons().then(
      data => {
        this.beacons = data;
        this.view.isRefreshingBeacons = false;
      },
      error => {
        this.view.errorLoadingBeacons = true;
        this.view.isRefreshingBeacons = false;
      }
    );
  }

}
