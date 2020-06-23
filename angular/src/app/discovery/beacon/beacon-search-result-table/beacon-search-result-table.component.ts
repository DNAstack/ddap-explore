import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';

import { BeaconQueryAlleleResponseModel } from '../../../shared/beacon/beacon-search.model';
import {
  ColumnDef,
  DataTableModel,
  DefaultColumnDef,
  TableRowSelection
} from '../../../shared/data-table/data-table.model';
import { DataTableModelParser } from '../../../shared/data-table/table/data-table-model.parser';

@Component({
  selector: 'ddap-beacon-search-result-table',
  templateUrl: './beacon-search-result-table.component.html',
  styleUrls: ['./beacon-search-result-table.component.scss'],
})
export class BeaconSearchResultTableComponent implements OnChanges {

  @Input()
  datasetAlleleResponse: BeaconQueryAlleleResponseModel;
  @Input()
  hiddenFieldIds?: string[] = [];
  @Input()
  tableName?: string;
  @Input()
  noRowsTemplate: string;

  @Output()
  selectedRowChanged: EventEmitter<any> = new EventEmitter<any>();

  clientSidePagination = false;
  dataTableModel: DataTableModel;
  tableRowSelection = TableRowSelection.single;
  defaultColumnDef: DefaultColumnDef = {
    filter: false,
  };

  ngOnChanges(changes: SimpleChanges): void {
    this.dataTableModel = this.setFieldVisibility(DataTableModelParser.parse(this.datasetAlleleResponse.info));
  }

  changeRowSelection(selectedRows: any[]) {
    this.selectedRowChanged.emit(selectedRows[0]);
  }

  private setFieldVisibility(dataTableModel: DataTableModel): DataTableModel {
    if (this.hiddenFieldIds && this.hiddenFieldIds.length > 0) {
      this.hiddenFieldIds.forEach((hiddenFieldId: string) => {
        const columnDefToBeHid = dataTableModel.columnDefs.find((columnDef: ColumnDef) => {
          return columnDef.field.toLowerCase() === hiddenFieldId.toLowerCase();
        });
        if (columnDefToBeHid) {
          columnDefToBeHid.hide = true;
        }
      });
    }
    return dataTableModel;
  }

}
