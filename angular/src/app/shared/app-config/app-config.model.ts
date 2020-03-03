export interface AppConfigModel {
  title: string;
  defaultModule: string;
  inStandaloneMode: boolean;
  authorizationOnInitRequired: boolean;
  sidebarEnabled: boolean;
  featureRealmInputEnabled: boolean;
  featureAdministrationEnabled: boolean;
  featureExploreDataEnabled: boolean;
  featureBeaconsEnabled: boolean;
  featureWorkflowsEnabled: boolean;
  featureWorkflowsTrsIntegrationEnabled: boolean; // deprecated/obsolete
  trsBaseUrl: string;
  trsAcceptedToolClasses: string[];
  trsAcceptedVersionDescriptorTypes: string[];
  listPageSize: number;
}
