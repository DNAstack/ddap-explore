import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, Validators } from '@angular/forms';
import { LoadingBarService } from '@ngx-loading-bar/core';

import { AppSearchService } from '../../shared/apps/app-search/app-search.service';
import {
  FilterModel,
  FilterOperation,
  FilterOperationLabel,
  SimpleSearchRequestModel,
} from '../../shared/apps/app-simple-search/app-simple-search.model';
import { AppSimpleSearchService } from '../../shared/apps/app-simple-search/app-simple-search.service';
import { ResourceModel } from '../../shared/apps/resource.model';
import { ResourceService } from '../../shared/apps/resource.service';
import { DataTableEventsService } from '../../shared/data-table/data-table-events.service';
import { DataTableModel } from '../../shared/data-table/data-table.model';
import { TableDataTableModelParser } from '../../shared/data-table/table/table-data-table-model.parser';
import { KeyValuePair } from '../../shared/key-value-pair.model';
import { QuerystringStateService } from '../../shared/querystring-state.service';
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
export class SimpleSearchComponent implements OnChanges {

  @Input()
  resourceId: string;

  FilterOperationLabel = FilterOperationLabel;

  readonly fieldOpSuffix = '_op';

  resource: ResourceModel;
  schema: JsonSchema;
  table: TableModel;
  filterForm: FormGroup;
  dataTableModel: DataTableModel;

  // NOTE See FilterOperation for all supported operations.
  propertyTypeToAllowedOperationsMap: {[propertyType: string]: string[]} = {
    string: [
      FilterOperation.EQ,
      FilterOperation.NEQ,
      FilterOperation.NULL,
      FilterOperation.NOT_NULL,
      FilterOperation.LIKE,
    ],
    int: [
      FilterOperation.EQ,
      FilterOperation.NEQ,
      FilterOperation.NULL,
      FilterOperation.NOT_NULL,
      FilterOperation.GT,
      FilterOperation.GTE,
      FilterOperation.LT,
      FilterOperation.LTE,
    ],
  };

  constructor(private resourceService: ResourceService,
              private appSearchService: AppSearchService,
              private appSimpleSearchService: AppSimpleSearchService,
              private querystringStateService: QuerystringStateService,
              public loader: LoadingBarService ) {
  }

  ngOnChanges(changes: SimpleChanges) {
    this.resource = null;
    this.schema = null;
    this.table = null;
    this.filterForm = null;
    this.dataTableModel = null;

    this.resetDataTable();
  }

  onFilterFormSubmit() {
    // TODO form validation
    this.update(this.defaultFilter());
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

  private defaultFilter(): SimpleSearchRequestModel {
    return {
      filters: this.compileFilters(),
      order: [],
    };
  }

  private update(filter: SimpleSearchRequestModel) {
    if (!this.interfaceId) {
      return;
    }

    filter = filter || this.defaultFilter();

    // TODO The "order" has been temporarily disregarded when the page is initialized
    //      from the last known state. We will need to handle that in the future when
    //      "order" is used.
    this.querystringStateService.save('filter', {data: filter});

    this.appSimpleSearchService.filter(this.interfaceId, filter)
      .subscribe(response => {
        this.table = response;
        this.dataTableModel = TableDataTableModelParser.parse(this.table);
      });
  }

  private compileFilters(): KeyValuePair<FilterModel> {
    if (!this.filterForm) {
      return {};
    }

    const filters: KeyValuePair<FilterModel> = {};
    const formData = this.filterForm.getRawValue();

    this.getFilterFormFieldNames()
      .forEach(fieldName => {
        let fieldValue = formData[fieldName];
        const fieldOp = FilterOperation[formData[fieldName + this.fieldOpSuffix]];
        const allowNullValue = (fieldOp === FilterOperation.NULL || fieldOp === FilterOperation.NOT_NULL);

        if (!allowNullValue && !fieldValue) {
          return; // skip this field, except "IS NULL" and "IS NOT NULL" operations.
        }

        if (allowNullValue) {
          fieldValue = null;
        }

        if (this.schema.properties[fieldName].type === 'int') {
          fieldValue = fieldValue ? parseInt(fieldValue, 10) : 0;
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

        // Restore from the query string.
        const startupRequest: SimpleSearchRequestModel = this.querystringStateService.load('filter') || {filters: {}};

        // Initialize the filter form.
        const fields: {[key: string]: AbstractControl} = {};

        for (const fieldName in this.schema.properties) {
          const property = this.schema.properties[fieldName];
          const validators = [];

          if (property.type === 'int') {
            validators.push(Validators.pattern(/^\d*$/));
          }

          const defaultValue = startupRequest.filters[fieldName] ? startupRequest.filters[fieldName].value : '';

          fields[fieldName] = new FormControl(defaultValue, validators);
          fields[fieldName + this.fieldOpSuffix] = new FormControl(FilterOperation.EQ, validators);
        }

        this.filterForm = new FormGroup(fields);

        // Detect changes
        this.filterForm.valueChanges.subscribe(data => {
          this.getFilterFormFieldNames().forEach(fieldName => {
            const fieldOp = data[fieldName + this.fieldOpSuffix];
            const field = this.filterForm.get(fieldName);
            const shouldDisable = fieldOp === FilterOperation.NULL || fieldOp === FilterOperation.NOT_NULL;

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
        this.update(this.querystringStateService.load('filter'));
      });
  }

  private get interfaceId(): string {
    return this.resource ? this.resource.interfaces[0].id : null;
  }

}
