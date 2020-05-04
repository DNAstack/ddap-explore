import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Column, ColumnApi, GridApi, NavigateToNextCellParams } from 'ag-grid-community';

import { ColumnDef, DefaultColumnDef, RowData, TableConfig, TableRowSelection } from './data-table.model';

@Component({
  selector: 'ddap-data-table',
  templateUrl: './data-table.component.html',
  styleUrls: ['./data-table.component.scss'],
})
export class DataTableComponent {

  @Input()
  columnDefs: ColumnDef[];
  @Input()
  rowData: RowData[];
  @Input()
  defaultColumnDef: DefaultColumnDef = {
    sortable: true,
    filter: true,
    resizable: true,
  };
  @Input()
  defaultTableConfig: TableConfig = {
    suppressCellSelection: true,
    enableCellTextSelection: true,
    multiSortKey: 'ctrl',
  };
  @Input()
  rowSelection: TableRowSelection = TableRowSelection.single;
  @Input()
  pagination = true;

  @Output()
  readonly selectedRowsChanged: EventEmitter<any | any[]> = new EventEmitter<any | any[]>();

  private gridApi: GridApi;
  private gridColumnApi: ColumnApi;

  constructor() {
    this.navigateToNextCell = this.navigateToNextCell.bind(this);
  }

  onGridReady(params): void {
    this.gridApi = params.api;
    this.gridColumnApi = params.columnApi;

    this.autoSizeColumns();
  }

  onRowDataChanged(params): void {
    if (!this.gridColumnApi) {
      return;
    }
    this.autoSizeColumns();
  }

  onSelectionChanged(params): void {
    if (!this.gridApi) {
      return;
    }
    this.selectedRowsChanged.emit(this.gridApi.getSelectedRows());
  }

  // FIXME need generalization and customization
  navigateToNextCell(params: NavigateToNextCellParams) {
    let previousCell = params.previousCellPosition;
    const suggestedNextCell = params.nextCellPosition;

    const KEY_UP = 38;
    const KEY_DOWN = 40;
    const KEY_LEFT = 37;
    const KEY_RIGHT = 39;

    switch (params.key) {
      case KEY_DOWN:
        previousCell = params.previousCellPosition;
        // set selected cell on current cell + 1
        this.gridApi.forEachNode(function (node) {
          if (previousCell.rowIndex + 1 === node.rowIndex) {
            node.setSelected(true);
          }
        });
        return suggestedNextCell;
      case KEY_UP:
        previousCell = params.previousCellPosition;
        // set selected cell on current cell - 1
        this.gridApi.forEachNode(function (node) {
          if (previousCell.rowIndex - 1 === node.rowIndex) {
            node.setSelected(true);
          }
        });
        return suggestedNextCell;
      case KEY_LEFT:
      case KEY_RIGHT:
        return suggestedNextCell;
      default:
        throw new Error('this will never happen');
    }
  }

  private autoSizeColumns(): void {
    if (!this.gridColumnApi) {
      return;
    }
    const allColumnIds = this.gridColumnApi.getAllColumns()
      .map((column: Column) => column.getColId());
    this.gridColumnApi.autoSizeColumns(allColumnIds);
  }

}
