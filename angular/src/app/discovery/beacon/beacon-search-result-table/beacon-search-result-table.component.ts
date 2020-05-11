import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs';

import { BeaconQueryAlleleResponseModel } from '../../../shared/beacon/beacon-search.model';
import { BeaconDataTableModelParser } from '../../../shared/data-table/beacon/beacon-data-table-model.parser';
import { ColumnDef, DataTableModel, TableRowSelection } from '../../../shared/data-table/data-table.model';

@Component({
  selector: 'ddap-beacon-search-result-table',
  templateUrl: './beacon-search-result-table.component.html',
  styleUrls: ['./beacon-search-result-table.component.scss'],
})
export class BeaconSearchResultTableComponent implements OnChanges {

  @Input()
  alleleResponses: BeaconQueryAlleleResponseModel[];
  @Input()
  hiddenFieldIds: string[];
  @Input()
  deselectRowsEvents: Subject<void>;

  @Output()
  selectedRowChanged: EventEmitter<any> = new EventEmitter<any>();

  dataTableModels: DataTableModel[];
  tableRowSelection = TableRowSelection.single;

  ngOnChanges(changes: SimpleChanges): void {
    this.dataTableModels = this.alleleResponses.map((alleleResponse: BeaconQueryAlleleResponseModel) => {
      return BeaconDataTableModelParser.parse(alleleResponse.info);
    }).map((dataTableModel: DataTableModel) => this.setFieldVisibility(dataTableModel));
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
