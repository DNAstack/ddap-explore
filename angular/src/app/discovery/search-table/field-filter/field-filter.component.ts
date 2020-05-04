import { ArrayDataSource } from '@angular/cdk/collections';
import { FlatTreeControl } from '@angular/cdk/tree';
import { AfterViewInit, Component, ElementRef, EventEmitter, HostListener, Input, OnInit, Output, ViewChild } from '@angular/core';
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
    @Output() conditionChanged = new EventEmitter<Condition>();
    @Output() enter = new EventEmitter();

    operatorNames: string[];

    operator: Operator;

    appConfig: AppConfigModel;

    value: string;

    constructor() {
      this.value = '';
    }

    clearInput() {
      this.value = '';
    }

    onEnter() {
      this.enter.emit();
    }

    onChange(value) {
      this.value = value;
      this.fireConditionChanged();
    }

    fireConditionChanged() {
      this.conditionChanged.emit({
        values: [this.value],
        operator: this.operator,
      });
    }

    selectOperator(op) {
      this.operator = op;
      if (this.value !== '') {
        this.fireConditionChanged();
      }
    }

    ngAfterViewInit(): void {
    }

    ngOnInit(): void {
      this.operator = this.operators[0];
      this.operatorNames = this.operators.map(function(op) {
        return op['name'];
      });
    }

    getOperatorByName(name) {
      return this.operators.filter(x => x.name === name)[0];
    }
}

export interface Condition {
  values: string[];
  operator: Operator;
}

export interface Operator {
  name: string;
  arity: Arity;
}

export enum Arity {
  unary,
  binary,
}
