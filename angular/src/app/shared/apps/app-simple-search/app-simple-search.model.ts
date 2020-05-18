import { KeyValuePair } from '../../key-value-pair.model';
import { ResourceModel } from '../resource.model';

export interface SimpleSearchResponseModel {
  data: ResourceModel[];
}

export interface SimpleSearchRequestModel extends KeyValuePair<any> {
  filters: KeyValuePair<FilterModel>;
  order: OrderByFilter[];
}

export interface FilterModel {
  operation: FilterOperation | string;
  value: any;
}

export interface OrderByFilter {
  field: string;
  direction: OrderBy;
}

export enum FilterOperation {
  'LIKE' = 'LIKE',
  'GT' = 'GT',
  'GTE' = 'GTE',
  'LT' = 'LT',
  'LTE' = 'LTE',
  'NEQ' = 'NEQ',
  'EQ' = 'EQ',
  'NOT_NULL' = 'NOT_NULL',
  'NULL' = 'NULL',
}

export enum OrderBy {
  DESC = 'DESC',
  ASC = 'ASC',
}
