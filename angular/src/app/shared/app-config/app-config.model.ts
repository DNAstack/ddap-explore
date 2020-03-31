export interface AppConfigModel {
  title: string;
  logoUrl: string;
  googleAnalyticsId: string;
  theme: string;
  defaultModule: string;
  tosUrl: string;
  inStandaloneMode: boolean;
  authorizationOnInitRequired: boolean;
  sidebarEnabled: boolean;
  featureRealmInputEnabled: boolean;
  featureAdministrationEnabled: boolean;
  featureTermsEnabled: boolean;
  featureExploreDataEnabled: boolean;
  featureDiscoveryEnabled: boolean;
  featureWorkflowsEnabled: boolean;
  trsBaseUrl: string;
  trsAcceptedToolClasses: string[];
  trsAcceptedVersionDescriptorTypes: string[];
  listPageSize: number;
  covidBeaconUrl: string;
}
