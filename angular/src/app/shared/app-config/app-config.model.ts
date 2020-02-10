export interface AppConfigModel {
    title: string;
    defaultModule: string;
    inStandaloneMode: boolean;
    authorizationOnInitRequired: boolean;
    sidebarEnabled: boolean;
    featureAdministrationEnabled: boolean;
    featureExploreDataEnabled: boolean;
    featureWorkflowsEnabled: boolean;
    featureWorkflowsTrsIntegrationEnabled: boolean;
    trsBaseUrl: string;
    trsAcceptedToolClasses: string[];
    trsAcceptedVersionDescriptorTypes: string[];
    listPageSize: number;
}
