import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ViewControllerService } from 'ddap-common-lib';
import { AppConfigModel } from 'src/app/shared/app-config/app-config.model';
import { AppConfigService } from 'src/app/shared/app-config/app-config.service';

import { DiscoveryConfigService } from '../discovery-config.service';

import { molecules } from './molecules';

@Component({
    selector: 'ddap-proteins',
    templateUrl: './molecules.component.html',
    styleUrls: ['./molecules.component.scss'],
  })
  export class MoleculesComponent implements OnInit {

    appConfig: AppConfigModel;

    grid: any;

    columnDefs: any[];
    rowData: any[];

    molecules: any[];

    selectedMolecule: any;
    selectedSubMolecule: any;

    view: {
        showLeftSidebar: boolean
    };

    constructor(private router: Router,
                private appConfigService: AppConfigService,
                private configService: DiscoveryConfigService,
                private viewController: ViewControllerService
                ) {

                    this.molecules = molecules;

                    this.view = {
                        showLeftSidebar: true,
                    };
    }

    ngOnInit(): void {
    }

    selectMolecule(molecule) {
        this.selectedMolecule = molecule;
        this.selectedSubMolecule = null;
    }

    getMoleculeToRender() {
        if (this.selectedSubMolecule != null) {
            return this.selectedSubMolecule;
        } else {
            return this.selectedMolecule;
        }
    }
}
