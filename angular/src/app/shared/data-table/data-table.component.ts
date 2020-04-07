import { Component, Input, OnInit } from '@angular/core';
import { ViewControllerService } from 'ddap-common-lib';

import { DataTableController } from './data-table-controller';

@Component({
  selector: 'ddap-data-table',
  templateUrl: './data-table.component.html',
  styleUrls: ['./data-table.component.scss'],
})
export class DataTableComponent implements OnInit {
  grid: any;
  selectedRow: any;

  selfBoundNavigateToCell: CallableFunction;

  @Input()
  controller: DataTableController;

  @Input()
  hiddenFieldIds: string[];

  // TODO need proper type reference
  gridApi: any;
  gridColumnApi: any;

  constructor(viewController: ViewControllerService) {
    this.grid = {
      animateRows: false,
      multiSortKey: 'ctrl',
      defaultColumnDefinition: {
        sortable: true,
        resizable: true,
        filter: true,
      },
      makeFullWidth: false,
      pagination: !this.isMobile(),
      domLayout: 'normal',
      enableStatusBar: true,
      suppressCellSelection: true,
      rowSelection: 'single',
    };

    this.selfBoundNavigateToCell = this.navigateToCell.bind(this);
  }

  ngOnInit() {
    this.controller.initialize();
  }

  onGridReady(event) {
    this.gridApi = event.api;
    this.gridColumnApi = event.columnApi;

    if (this.grid.makeFullWidth) {
      event.api.sizeColumnsToFit();
      window.addEventListener('resize', function () {
        setTimeout(function () {
          event.api.sizeColumnsToFit();
        });
      });
    }

    this.hideAndResizeColumns();
  }

  onRowDataChanged(event) {
    if (!this.gridApi) {
      return;
    }
    this.hideAndResizeColumns();
  }

  onSelectionChanged(event) {
    this.selectedRow = this.gridApi.getSelectedRows()[0];
    this.controller.onSelectionChanged(this.selectedRow);
  }

  hasEverLoaded() {
    return !(this.controller.resultList === null || this.controller.resultList === undefined);
  }

  enablePagination(enabled: boolean) {
    this.grid.pagination = enabled;
  }

  // FIXME need generalization and customization
  hideAndResizeColumns() {
    if (!this.hasEverLoaded()) {
      return;
    }

    const hiddenFieldIds = this.hiddenFieldIds;

    const allColumnIds = [];
    const hiddenColumnIds = [];

    this.gridColumnApi.getAllColumns().forEach(function (column) {
      allColumnIds.push(column.colId);
      if (hiddenFieldIds.includes(column.userProvidedColDef.field)) {
        hiddenColumnIds.push(column.colId);
      }
    });

    this.gridColumnApi.setColumnsVisible(hiddenColumnIds, false);
    this.gridColumnApi.autoSizeColumns(allColumnIds);
  }

  // FIXME need generalization and customization
  navigateToCell(params) {
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

  // FIXME move to the view controller
  isMobile() {
    return this.isMobileWidth(window.innerWidth);
  }

  // FIXME normalize duplicate code
  private isMobileWidth(width: number) {
    return width <= 760;
  }
}
