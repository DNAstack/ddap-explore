export interface AppConfigModel {
  title: string;
  logoUrl: string;
  defaultModule: string;
  inStandaloneMode: boolean;
  authorizationOnInitRequired: boolean;
  sidebarEnabled: boolean;
  featureRealmInputEnabled: boolean;
  featureAdministrationEnabled: boolean;
  featureTermsEnabled: boolean;
  featureExploreDataEnabled: boolean;
  featureBeaconsEnabled: boolean;
  featureDiscoveryEnabled: boolean;
  featureWorkflowsEnabled: boolean;
  featureWorkflowsTrsIntegrationEnabled: boolean; // deprecated/obsolete
  trsBaseUrl: string;
  trsAcceptedToolClasses: string[];
  trsAcceptedVersionDescriptorTypes: string[];
  listPageSize: number;
  covidBeaconUrl: string;
}
