import _get from 'lodash.get';
import _startcase from 'lodash.startcase';

import { AlleleResponseInfoModel } from '../../beacon/beacon-search.model';
import { ColumnDef, DataTableModel, RowData } from '../data-table.model';

export class BeaconDataTableModelParser {

  public static parse(info: AlleleResponseInfoModel): DataTableModel {
    const columnDefs: ColumnDef[] = info.data_model ? this.parseColumnDefs(info) : [];
    const rowData: RowData[] = info.data ? this.parseRowData(info) : [];

    return {
      columnDefs,
      rowData,
    };
  }

  private static parseColumnDefs(info: AlleleResponseInfoModel): ColumnDef[] {
    return Object.entries(info.data_model.properties)
      .map(([propertyKey, property]) => {
        return {
          headerName: _startcase(propertyKey),
          headerTooltip: property.description,
          field: propertyKey,
        };
      });
  }

  private static parseRowData(info: AlleleResponseInfoModel): RowData[] {
    return info.data
      .map((rowData: any) => {
        Object.entries(rowData).forEach(([columnName, rowValue]: any) => {
          const columnDataModel = _get(info, `data_model.properties.${columnName}`, {});
          if (columnDataModel.type && columnDataModel.type === 'integer') {
            rowData[columnName] = parseInt(rowValue, 10);
          }
        });
        return rowData;
      });
  }

}


