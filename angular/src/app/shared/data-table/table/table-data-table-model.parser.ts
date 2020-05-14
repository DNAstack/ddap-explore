import _get from 'lodash.get';
import _startcase from 'lodash.startcase';

import { TableModel } from '../../search/table.model';
import { ColumnDef, DataTableModel, RowData } from '../data-table.model';

export class TableDataTableModelParser {

  public static parse(table: TableModel): DataTableModel {
    const columnDefs: ColumnDef[] = table.data_model ? this.parseColumnDefs(table) : [];
    const rowData: RowData[] = table.data ? this.parseRowData(table) : [];

    return {
      columnDefs,
      rowData,
    };
  }

  private static parseColumnDefs(table: TableModel): ColumnDef[] {
    return Object.entries(table.data_model.properties)
      .map(([propertyKey, property]) => {
        return {
          headerName: _startcase(propertyKey),
          headerTooltip: property.description,
          field: propertyKey,
        };
      });
  }

  private static parseRowData(table: TableModel): RowData[] {
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


