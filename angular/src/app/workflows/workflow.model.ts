import { flatten } from '../shared/util';

export interface WorkflowRunsResponse {
  runs: WorkflowRun[];
  damId: string;
  resourceId: string;
  viewId: string;
  error: {
    message: string;
  };
  ui: {
    resource: string;
    view: string;
  };
  next_page_token: string;
}

export interface WorkflowRun {
  run_id: string;
  state: string;
}

export interface WesResourceViews {
  damId: string;
  resource: {[key: string]: any};
  views: {[key: string]: any}[];
}

export class SimplifiedWesResourceViews {
  resourceId: string;
  resource: string;
  damId: string;
  views: {
    name: string;
    label: string;
    url: string;
    resourcePath: string;
  }[];

  static fromWesResourceViews(wesResourceViews: WesResourceViews): SimplifiedWesResourceViews {
    const resourceId = Object.keys(wesResourceViews.resource)[0];
    const views = flatten(wesResourceViews.views
      .map((viewMap) => {
        return Object.entries(viewMap)
          .map(([viewId, value]) => {
            const wesInterface = Object.keys(value.interfaces)[0];
            const wesUri = value.interfaces[wesInterface].uri[0];
            const defaultRole = Object.keys(value.roles)[0];
            const resourcePath = `${resourceId}/views/${viewId}/roles/${defaultRole}`;
            return {
              name: viewId,
              label: value.ui.label,
              url: wesUri,
              resourcePath,
            };
          });
      }));

    return {
      resourceId,
      resource: wesResourceViews.resource[resourceId].ui.label,
      damId: wesResourceViews.damId,
      views,
    };
  }
}
