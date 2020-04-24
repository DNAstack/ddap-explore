import { AfterViewInit, Component, HostListener, OnInit } from '@angular/core';
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
  export class MoleculesComponent implements OnInit, AfterViewInit {
    appConfig: AppConfigModel;

    grid: any;

    columnDefs: any[];
    rowData: any[];

    molecules: any[];

    selectedMolecule: any;
    selectedSubMolecule: any;

    view: {
        showLeftSidebar: boolean,
        viewer: {
            background: string,
            fov: number,
            cameraType: string
        }
    };

    stage: ngl.stage;

    constructor(private router: Router,
                private appConfigService: AppConfigService,
                private configService: DiscoveryConfigService,
                private viewController: ViewControllerService
                ) {

                    this.molecules = molecules;

                    this.view = {
                        showLeftSidebar: true,
                        viewer : {
                            background: 'black',
                            fov: 80,
                            cameraType: 'persepective',
                        },
                    };
    }
    ngAfterViewInit(): void {
        this.stage = new ngl.Stage('ngl-viewer' );
        this.applyParameters();
    }

    applyParameters() {
        this.stage.setParameters(
            {
                backgroundColor: this.view.viewer.background,
                cameraFov: this.view.viewer.fov,
            }
        );
        // console.log(this.view.viewer);
    }

    @HostListener('window:resize') onResize(event) {
      if (this.stage) {
        this.stage.handleResize();
      }
    }

    ngOnInit(): void {

    }

    selectMolecule(molecule) {
        this.selectedMolecule = molecule;
        this.selectedSubMolecule = null;
        this.selectionChanged();
    }

    selectSubMolecule(subMolecule) {
        this.selectedSubMolecule = subMolecule;
        this.selectionChanged();
    }

    getMoleculeToRender() {
        if (this.selectedSubMolecule != null) {
            return this.selectedSubMolecule;
        } else {
            return this.selectedMolecule;
        }
    }

    autoView() {
        this.stage.autoView();
    }

    selectionChanged() {
        const molecule = this.getMoleculeToRender();
        // console.log("Rendering " + molecule);
        if (molecule.pdbid) {
            this.stage.loadFile( 'rcsb://' + molecule.pdbid ).then( function( o ) {
                o.addRepresentation( 'ribbon' , {colorScheme: 'bfactor'} );
                o.autoView();
            } );
        } else {
            // TODO: fail gracefully
        }


    }
}
