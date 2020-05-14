export interface SimpleSearchRequest {
  filters: SearchFilterList;
  order: OrderByFilter[];
}

export interface SearchFilterList {
  [field: string]: SearchFilter;
}

export interface SearchFilter {
  operation: FilterOperation;
  value: any;
}

export interface OrderByFilter {
  field: string;
  direction: OrderBy;
}

export enum FilterOperation {
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

export enum OrderBy {
  DESC = 'DESC',
  ASC = 'ASC',
}
