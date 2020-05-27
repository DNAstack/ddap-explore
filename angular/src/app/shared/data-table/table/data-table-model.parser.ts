import _get from 'lodash.get';
import _startcase from 'lodash.startcase';

import { AlleleResponseInfoModel } from '../../beacon/beacon-search.model';
import { ColumnDef, DataTableModel, RowData } from '../data-table.model';

export class DataTableModelParser {

  public static parse(table: AlleleResponseInfoModel): DataTableModel {
    if (!table) {
      return {
        columnDefs: [],
        rowData: [],
      };
    }

    const columnDefs: ColumnDef[] = table.data_model ? this.parseColumnDefs(table) : [];
    const rowData: RowData[] = table.data ? this.parseRowData(table) : [];

    return {
      columnDefs,
      rowData,
    };
  }

  private static parseColumnDefs(table: AlleleResponseInfoModel): ColumnDef[] {
    return Object.entries(table.data_model.properties)
      .map(([propertyKey, property]) => {
        return {
          headerName: _startcase(propertyKey).replace(/ id$/i, ' ID'),
          headerTooltip: property.description,
          field: propertyKey,
          resizable: true,
        };
      });
  }

  private static parseRowData(table: AlleleResponseInfoModel): RowData[] {
    return table.data
      .map((rowData: any) => {
        Object.entries(rowData).forEach(([columnName, rowValue]: any) => {
          const columnDataModel = _get(table, `data_model.properties.${columnName}`, {});
          if (columnDataModel.type && columnDataModel.type === 'integer') {
            rowData[columnName] = parseInt(rowValue, 10);
          }
        });
        return rowData;
      });
  }

}


