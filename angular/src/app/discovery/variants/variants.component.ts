import { AfterViewInit, Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ViewControllerService } from 'ddap-common-lib';
import { SearchService } from 'src/app/search/search.service';
import { AppConfigModel } from 'src/app/shared/app-config/app-config.model';
import { AppConfigService } from 'src/app/shared/app-config/app-config.service';

import { DiscoveryConfigService } from '../discovery-config.service';

@Component({
    selector: 'ddap-variants',
    templateUrl: './variants.component.html',
    styleUrls: [],
  })
  export class VariantsComponent implements OnInit, AfterViewInit {

    appConfig: AppConfigModel;

    conditions: {};

    view: {
        showLeftSidebar: boolean,
        showRightSidebar: boolean,
    };

    service = 'search';
    table = 'search_cloud.coronavirus.variants';
    fieldMap = {
        '_id' : 'Identifier',
        'name' : 'Name',
        'source' : 'Source',
        'seqid' : 'Sequence Identifier',
        'start' : 'Start',
        'end' : 'End',
        'ref' : 'Reference Allele',
        'alt' : 'Alternate Allele',
        'type' : 'Type',
        'score' : 'Score',
        'strand' : 'Strand',
        'phase' : 'Phase',
        'vep' : 'Variant Effect Predictor (VEP)',
    };

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

    onConditionsChange(map) {
        // console.log("Received");
        // console.log(map);
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

