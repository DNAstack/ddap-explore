export interface SimpleSearchRequest {
  filters: SearchFilterList;
  order: OrderByFilter[];
}

export interface SearchFilterList {
  [field: string]: SearchFilter;
}

export interface SearchFilter {
  operation: FilterOperation | string;
  value: any;
}

export interface OrderByFilter {
  field: string;
  direction: OrderBy;
}

export enum FilterOperationPresentation {
  LIKE = 'LIKE',
  GT = '>',
  GTE = '>=',
  LT = '<',
  LTE = '<=',
  NEQ = '!=',
  EQ = '=',
  NOT_NULL = 'IS NOT NULL',
  NULL = 'IS NULL',
}

export enum FilterOperation {
  'LIKE' = 'LIKE',
  '>' = 'GT',
  '>=' = 'GTE',
  '<' = 'LT',
  '<=' = 'LTE',
  '!=' = 'NEQ',
  '=' = 'EQ',
  'IS NOT NULL' = 'NOT_NULL',
  'IS NULL' = 'NULL',
}

export enum OrderBy {
  DESC = 'DESC',
  ASC = 'ASC',
}
