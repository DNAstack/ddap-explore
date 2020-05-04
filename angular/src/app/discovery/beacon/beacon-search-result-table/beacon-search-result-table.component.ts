import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';

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

  dataTableModels: DataTableModel[];
  tableRowSelection = TableRowSelection.single;
  selectedRowData: any;

  ngOnChanges(changes: SimpleChanges): void {
    this.dataTableModels = this.alleleResponses.map((alleleResponse: BeaconQueryAlleleResponseModel) => {
      return BeaconDataTableModelParser.parse(alleleResponse.info);
    }).map((dataTableModel: DataTableModel) => this.setFieldVisibility(dataTableModel));
  }

  changeRowSelection(selectedRows: any[]) {
    this.selectedRowData = selectedRows[0];
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
