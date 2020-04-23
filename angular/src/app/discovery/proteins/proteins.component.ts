import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ViewControllerService } from 'ddap-common-lib';
import { AppConfigModel } from 'src/app/shared/app-config/app-config.model';
import { AppConfigService } from 'src/app/shared/app-config/app-config.service';

import { DiscoveryConfigService } from '../discovery-config.service';

import { proteins } from './proteins';

@Component({
    selector: 'ddap-proteins',
    templateUrl: './proteins.component.html',
    styleUrls: ['./proteins.component.scss'],
  })
  export class ProteinsComponent implements OnInit {

    appConfig: AppConfigModel;

    grid: any;

    columnDefs: any[];
    rowData: any[];

    proteins: any[];

    view: {
        showLeftSidebar: boolean
    };

    constructor(private router: Router,
                private appConfigService: AppConfigService,
                private configService: DiscoveryConfigService,
                private viewController: ViewControllerService
                ) {

                    this.proteins = proteins;

                    this.view = {
                        showLeftSidebar: true,
                    };
    }

    ngOnInit(): void {
    }
}
