import { JsonSchema } from './json-schema.model';

export interface TableInfo {
  name: string;
  description: string;
  data_model: JsonSchema;
}
