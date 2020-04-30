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

    treeControl: any;

    treeFlattener: any;

    dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);

    constructor(private router: Router,
                private appConfigService: AppConfigService,
                private configService: DiscoveryConfigService,
                private viewController: ViewControllerService
                ) {

                    this.treeControl = new FlatTreeControl<MoleculeNode>(
                        node => node.level, node => node.expandable);
                    this.treeFlattener = new MatTreeFlattener(
                        this._transformer, node => node.level, node => node.expandable, node => node.parts);

                    this.dataSource.data = <Molecule[]> molecules;

                    this.molecules = molecules;

                    /*
                    this.nglRepresentations = [
                        { id: 'ribbon', name: 'Ribbon', params: { colorScheme: 'bfactor' }},
                        { id: 'ball+stick', name: 'Ball and Stick', params: { multipleBond: true }},
                        { id: 'cartoon', name: 'Cartoon', params: {}},
                    ];*/

                    this.nglRepresentations = ngl.RepresentationRegistry._dict;
                    this.selectedRepresentation = 'ribbon';

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

    hasChild = (_: number, node: MoleculeNode) => node.expandable;

    ngAfterViewInit(): void {
        this.stage = new ngl.Stage('ngl-viewer' );
        this.applyParameters();
    }

    prettyRepresentation(representationName: string) {
        return representationName.replace('Representation', '');
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

    onRepresentationSelectionChanged(event) {
        this.selectionChanged();
    }

    selectMolecule(molecule) {
        this.selectedMolecule = molecule;
        this.selectionChanged();
    }

    autoView() {
        this.stage.autoView();
    }

    selectionChanged() {

        this.resizeStage();

        if (this.selectedMolecule.pdbid) {
            const that = this;
            this.stage.removeAllComponents();
            this.stage.loadFile( 'rcsb://' + this.selectedMolecule.pdbid ).then( function( o ) {
                o.addRepresentation( that.selectedRepresentation , {} );
                o.autoView();
            } );
        } else {
            // TODO: fail more gracefully
            this.selectMolecule(null);
        }


    }


    private _transformer = (molecule: Molecule, level: number) => {
        return {
          expandable: !!molecule.parts && molecule.parts.length > 0,
          name: molecule.name,
          molecule: molecule,
          level: level,
        };
      }





}

interface MoleculeNode {
    expandable: boolean;
    name: string;
    molecule: Molecule;
    level: number;
  }

interface Molecule {
    name: string;
    id: string;
    pdbid: string;
    parts: Molecule[];
}
