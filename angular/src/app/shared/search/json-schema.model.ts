export interface JsonSchema {
  $ref?: string;
  description?: string;
  properties?: JsonSchemaProperties;
}

interface JsonSchemaProperties {
  $ref?: string;
  type?: string;
  [key: string]: any;
}
