import { ArrayDataSource } from '@angular/cdk/collections';
import { FlatTreeControl } from '@angular/cdk/tree';
import { AfterViewInit, Component, ElementRef, HostListener, Input, OnInit, ViewChild } from '@angular/core';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { ViewControllerService } from 'ddap-common-lib';
import * as ngl from 'ngl';
import { SearchResourceModel } from 'src/app/search/search-resources/search-resource.model';
import { SearchService } from 'src/app/search/search.service';
import { AppConfigModel } from 'src/app/shared/app-config/app-config.model';
import { AppConfigService } from 'src/app/shared/app-config/app-config.service';
import { TableModel } from 'src/app/shared/search/table.model';

@Component({
    selector: 'ddap-field-filter',
    templateUrl: './field-filter.component.html',
    styleUrls: ['./field-filter.component.scss'],
  })
  export class FieldFilterComponent implements OnInit, AfterViewInit {

    @Input() field: any;
    @Input() operators: Operator[];

    operator: Operator;

    appConfig: AppConfigModel;

    constructor() {
    }

    selectOperator(op) {
      // console.log("Selected " + op);
      this.operator = op;
    }

    ngAfterViewInit(): void {

    }

    ngOnInit(): void {
      this.operator = this.operators[0];
    }
}

export interface Operator {
  name: string;
  arity: Arity;
}

export enum Arity {
  unary,
  binary,
}
