export interface AppConfigModel {
  head: {
    title: string;
  };
  ui: {
    title: string;
    logoUrl: string;
    theme: string;
  };
  enabledApps: FrontendApp[];
  enabledFeatures: FrontendFeature[];
  defaultRoute: string;
  googleAnalyticsId: string;
  tosUrl: string;
  privacyPolicyUrl: string;
  inStandaloneMode: boolean;
  listPageSize: number;
  apps: {
    search: FrontendAppSearchConfig;
    workflows: FrontendAppWorkflowsConfig;
  };
}

export interface FrontendAppSearchConfig {
  defaultQuery: string;
}

export interface FrontendAppWorkflowsConfig {
  trsBaseUrl: string;
  trsAcceptedToolClasses: string[];
  trsAcceptedVersionDescriptorTypes: string[];
}

export enum FrontendApp {
  beacon = 'BEACON',
  data = 'DATA',
  discovery = 'DISCOVERY',
  search = 'SEARCH',
  workflows = 'WORKFLOWS',
  workspace = 'WORKSPACE',
}

export enum FrontendFeature {
  administration = 'ADMINISTRATION',
  authOnInitRequired = 'AUTH_ON_INIT_REQUIRED',
  realmInput = 'REALM_INPUT',
  sidebar= 'SIDEBAR',
  terms = 'TERMS',
}
