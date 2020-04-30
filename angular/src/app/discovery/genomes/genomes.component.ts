import { ArrayDataSource } from '@angular/cdk/collections';
import { FlatTreeControl } from '@angular/cdk/tree';
import { AfterViewInit, Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material';
import { Router } from '@angular/router';
import { ViewControllerService } from 'ddap-common-lib';
import * as ngl from 'ngl';
import { AppConfigModel } from 'src/app/shared/app-config/app-config.model';
import { AppConfigService } from 'src/app/shared/app-config/app-config.service';

import { DiscoveryConfigService } from '../discovery-config.service';

@Component({
    selector: 'ddap-proteins',
    templateUrl: './genomes.component.html',
    styleUrls: [],
  })
  export class GenomesComponent implements OnInit, AfterViewInit {

    appConfig: AppConfigModel;

    grid: any;

    columnDefs: any[];
    rowData: any[];

    view: {
        showLeftSidebar: boolean,
        showRightSidebar: boolean
    };

    constructor(private router: Router,
                private appConfigService: AppConfigService,
                private configService: DiscoveryConfigService,
                private viewController: ViewControllerService
                ) {

                    this.view = {
                        showLeftSidebar: true,
                        showRightSidebar: false,
                    };
    }


    ngAfterViewInit(): void {
    }


    ngOnInit(): void {

    }

}

