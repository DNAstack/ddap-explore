import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ViewControllerService } from 'ddap-common-lib';
import * as ngl from 'ngl';
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
    /**
     * Stage class, central for creating molecular scenes with NGL.
     *
     * @example
     * var stage = new Stage( "elementId", { backgroundColor: "white" } );
     */
    const stage = new ngl.Stage('ngl-viewer', { backgroundColor: 'black'});
    stage.loadFile('rcsb://1crn.mmtf', {defaultRepresentation: true});
    function handleResize () {
      stage.handleResize();
    }
    window.addEventListener('orientationchange', handleResize, false);
    window.addEventListener('resize', handleResize, false);
    }

    getMoleculeToRender() {
        if (this.selectedSubMolecule != null) {
            return this.selectedSubMolecule;
        } else {
            return this.selectedMolecule;
        }
    }
}
