import { JsonSchema } from '../search/json-schema.model';

import { SPIResource } from './resource.model';

export interface SPIAppSearchSimple {
  data_model: JsonSchema;
  name: string;
  requiresAdditionalAuth: boolean;
  resource: SPIResource;
}
