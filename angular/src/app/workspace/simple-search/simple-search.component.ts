import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, Validators } from '@angular/forms';

import { AppSearchService } from '../../shared/apps/app-search/app-search.service';
import { AppSimpleSearchService } from '../../shared/apps/app-simple-search/app-simple-search.service';
import {
  FilterOperation, FilterOperationPresentation, SearchFilterList,
  SimpleSearchRequest
} from '../../shared/apps/app-simple-search/models/app-search-simple-filter-request.model';
import { SPIAppSearchSimple } from '../../shared/apps/app-simple-search/models/app-search-simple.model';
import { ResourceModel } from '../../shared/apps/resource.model';
import { ResourceService } from '../../shared/apps/resource.service';
import { DataTableEventsService } from '../../shared/data-table/data-table-events.service';
import { DataTableModel } from '../../shared/data-table/data-table.model';
import { TableDataTableModelParser } from '../../shared/data-table/table/table-data-table-model.parser';
import { JsonSchema } from '../../shared/search/json-schema.model';
import { TableModel } from '../../shared/search/table.model';

/**
 * Simple Search Component
 *
 * It is a reusable component within the context of Workspace.
 */
@Component({
  selector: 'ddap-simple-search',
  templateUrl: './simple-search.component.html',
  styleUrls: ['./simple-search.component.scss'],
  providers: [DataTableEventsService],
})
export class SimpleSearchComponent implements OnInit, OnChanges {
  @Input()
  resourceId: string;

  readonly fieldOpSuffix = '_op';

  resource: ResourceModel;
  schema: JsonSchema;
  table: TableModel;

  filterForm: FormGroup;

  dataTableModel: DataTableModel;

  // NOTE See FilterOperation for all supported operations.
  propertyTypeToAllowedOperationsMap: {[propertyType: string]: string[]} = {
    string: [
      FilterOperationPresentation.EQ,
      FilterOperationPresentation.NEQ,
      FilterOperationPresentation.NULL,
      FilterOperationPresentation.NOT_NULL,
      FilterOperationPresentation.LIKE,
    ],
    int: [
      FilterOperationPresentation.EQ,
      FilterOperationPresentation.NEQ,
      FilterOperationPresentation.NULL,
      FilterOperationPresentation.NOT_NULL,
      FilterOperationPresentation.GT,
      FilterOperationPresentation.GTE,
      FilterOperationPresentation.LT,
      FilterOperationPresentation.LTE,
    ],
  };

  constructor(private resourceService: ResourceService,
              private appSearchService: AppSearchService,
              private appSimpleSearchService: AppSimpleSearchService) {
  }

  ngOnInit(): void {
    this.initialize();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (!this.dataTableModel) {
      return; // If the data table model is not defined, this means that the component hasn't been initialized.
    }

    this.resource = null;
    this.schema = null;
    this.table = null;
    this.filterForm = null;
    this.dataTableModel = null;

    this.resetDataTable();
  }

  onFilterFormSubmit() {
    // TODO form validation
    this.update();
  }

  getFilterFormFieldNames(): string[] {
    if (!this.schema) {
      return [];
    }
    return Object.keys(this.schema.properties);
  }

  getOperationsByFieldName(fieldName: string): string[] {
    const property = this.schema.properties[fieldName];

    if (!property) {
      throw new Error(`Unknown field ${fieldName} (${Object.keys(this.schema.properties)})`);
    }

    return this.propertyTypeToAllowedOperationsMap[property.type];
  }

  private update() {
    if (!this.interfaceId) {
      return;
    }

    const filter: SimpleSearchRequest = {
      filters: this.compileFilters(),
      order: [],
    }; // default filter

    this.appSimpleSearchService.filter(this.interfaceId, filter)
      .subscribe(response => {
        this.table = response;
        this.dataTableModel = TableDataTableModelParser.parse(this.table);
      });
  }

  private compileFilters(): SearchFilterList {
    if (!this.filterForm) {
      return {};
    }

    const filters: SearchFilterList = {};
    const formData = this.filterForm.getRawValue();

    this.getFilterFormFieldNames()
      .forEach(fieldName => {
        let fieldValue = formData[fieldName];
        const fieldOp = FilterOperation[formData[fieldName + this.fieldOpSuffix]];

        if (!fieldValue) {
          return; // skip this field
        }

        if (this.schema.properties[fieldName].type === 'int') {
          fieldValue = parseInt(fieldValue, 10);
        }

        filters[fieldName] = {
          operation: fieldOp,
          value: fieldValue,
        };
      });

    return filters;
  }

  private retrieveTableInfo() {
    this.appSearchService.getTableInfo(this.resource.metadata.tableName, this.interfaceId)
      .subscribe(response => {
        this.schema = response.data_model;

        // Initialize the filter form.
        const fields: {[key: string]: AbstractControl} = {};

        for (const fieldName in this.schema.properties) {
          const property = this.schema.properties[fieldName];
          const validators = [];
          if (property.type === 'int') {
            validators.push(Validators.pattern(/^\d*$/));
          }
          fields[fieldName] = new FormControl('', validators);
          fields[fieldName + this.fieldOpSuffix] = new FormControl(FilterOperationPresentation.EQ, validators);
        }

        this.filterForm = new FormGroup(fields);

        // Detect changes
        this.filterForm.valueChanges.subscribe(data => {
          this.getFilterFormFieldNames().forEach(fieldName => {
            const fieldOp = data[fieldName + this.fieldOpSuffix];
            const field = this.filterForm.get(fieldName);
            const shouldDisable = fieldOp === FilterOperationPresentation.NULL || fieldOp === FilterOperationPresentation.NOT_NULL;

            if (field.enabled && shouldDisable) {
              field.disable();
            } else if (field.disabled && !shouldDisable) {
              field.enable();
            }
          });
        });
      });
  }

  private initialize() {
    this.resetDataTable();
  }

  private resetDataTable() {
    this.resource = null;

    this.resourceService.getResource(this.resourceId)
      .subscribe(resource => {
        this.resource = resource;

        this.retrieveTableInfo();
        this.update();
      });
  }

  private get interfaceId(): string {
    return this.resource ? this.resource.interfaces[0].id : null;
  }
}
