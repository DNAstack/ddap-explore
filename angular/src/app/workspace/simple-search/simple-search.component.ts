import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';

import { DataTableModel } from '../../shared/data-table/data-table.model';
import { TableDataTableModelParser } from '../../shared/data-table/table/table-data-table-model.parser';
import { TableModel } from '../../shared/search/table.model';
import { SimpleSearchRequest } from '../../shared/spi/app-search-simple-filter-request.model';
import { SPIAppSearchSimple } from '../../shared/spi/app-search-simple.model';
import { SPIAppService } from '../../shared/spi/spi-app.service';

/**
 * Simple Search Component
 *
 * It is a reusable component within the context of Workspace.
 */
@Component({
  selector: 'ddap-simple-search',
  templateUrl: './simple-search.component.html',
  styleUrls: ['./simple-search.component.scss'],
})
export class SimpleSearchComponent implements OnInit, OnChanges {
  @Input()
  resource: SPIAppSearchSimple;

  currentResponse: TableModel;
  dataTableModel: DataTableModel;

  constructor(private spiAppService: SPIAppService) {
  }

  ngOnInit(): void {
    this.initialize();
  }

  ngOnChanges(changes: SimpleChanges) {
    this.dataTableModel = null;
    this.update();
  }

  getFields() {
    const fields: Field[] = [];

    for (const fieldName in this.resource.data_model.properties) {
      fields.push({
        name: fieldName,
      });
    }

    return fields;
  }

  update() {
    const interfaceId = this.resource.resource.interfaces[0].id;
    const filter: SimpleSearchRequest = {
      filters: {},
      order: [],
    }; // default filter

    this.spiAppService.submitSimpleSearchFilter(interfaceId, filter)
      .subscribe(response => {
        this.currentResponse = response;
        this.dataTableModel = TableDataTableModelParser.parse(this.currentResponse);
      });
  }

  private initialize() {
    this.update();
  }
}

interface Field {
  name: string;
}
