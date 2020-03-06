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
  trsBaseUrl: string;
  trsAcceptedToolClasses: string[];
  trsAcceptedVersionDescriptorTypes: string[];
  listPageSize: number;
}
