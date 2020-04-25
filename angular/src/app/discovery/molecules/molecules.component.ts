import { AfterViewInit, Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
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
    selectedRepresentation: any;

    nglRepresentations: any[];

    view: {
        drawerRolling: boolean,
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

                    this.nglRepresentations = [
                        { id: 'ball+stick', name: 'Ball and Strick', params: { multipleBond: true }},
                        { id: 'ribbon', name: 'Ribbon', params: { colorScheme: 'bfactor' }},
                    ];

                    this.selectedRepresentation = this.nglRepresentations[1];

                    this.view = {
                        drawerRolling: false,
                        showLeftSidebar: true,
                        viewer : {
                            background: '#1f1f1f',
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
    }

    @HostListener('window:resize') onResize(event) {
        this.resizeStage();
    }

    resizeStage() {
        if (this.stage) {
            this.stage.handleResize();
            this.stage.setSize('100%', '100%');
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
        this.resizeStage();
        // console.log(molecule);

        const that = this;

        if (molecule.pdbid) {
            this.stage.loadFile( 'rcsb://' + molecule.pdbid ).then( function( o ) {
                o.addRepresentation( that.selectedRepresentation.id , that.selectedRepresentation.params );
            } );
            this.autoView();
        } else {
            // TODO: fail more gracefully
            this.selectMolecule(null);
        }


    }
}
