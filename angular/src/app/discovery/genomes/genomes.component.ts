import { AfterViewInit, Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ViewControllerService } from 'ddap-common-lib';
import { SearchService } from 'src/app/search/search.service';
import { AppConfigModel } from 'src/app/shared/app-config/app-config.model';
import { AppConfigService } from 'src/app/shared/app-config/app-config.service';

import { DiscoveryConfigService } from '../discovery-config.service';

@Component({
    selector: 'ddap-genomes',
    templateUrl: './genomes.component.html',
    styleUrls: [],
  })
  export class GenomesComponent implements OnInit, AfterViewInit {

    appConfig: AppConfigModel;

    view: {
        showLeftSidebar: boolean,
        showRightSidebar: boolean,
    };

    service = 'search';
    table = 'search_cloud.coronavirus.sequences';


    constructor(private router: Router,
                private appConfigService: AppConfigService,
                private route: ActivatedRoute,
                private searchService: SearchService,
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

interface ParsedBackendError {
    errorName: string;
    message: string;
  }

