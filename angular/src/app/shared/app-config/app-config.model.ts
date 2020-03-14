export interface AppConfigModel {
  title: string;
  defaultModule: string;
  inStandaloneMode: boolean;
  authorizationOnInitRequired: boolean;
  sidebarEnabled: boolean;
  featureRealmInputEnabled: boolean;
  featureAdministrationEnabled: boolean;
  featureExploreDataEnabled: boolean;
  featureWorkflowsEnabled: boolean;
  featureSearchEnabled: boolean;
  featureWorkflowsTrsIntegrationEnabled: boolean; // deprecated/obsolete
  trsBaseUrl: string;
  trsAcceptedToolClasses: string[];
  trsAcceptedVersionDescriptorTypes: string[];
  listPageSize: number;
}
