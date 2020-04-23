export interface JsonSchema {
  $ref?: string;
  description?: string;
  properties?: JsonSchemaProperties;
}

interface JsonSchemaProperties {
  [key: string]: object;
}
