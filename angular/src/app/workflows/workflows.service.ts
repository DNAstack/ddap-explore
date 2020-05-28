import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorHandlerService, realmIdPlaceholder } from 'ddap-common-lib';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../../environments/environment';
import { dam } from '../shared/proto/dam-service';

import { WorkflowExecutionModel } from './workflow-execution-form/workflow-execution-step/workflow-execution.model';
import { SimplifiedWesResourceViews, WesResourceViews, WorkflowRunsResponse } from './workflow.model';

@Injectable({
  providedIn: 'root',
})
export class WorkflowService {

  constructor(private http: HttpClient,
              private errorHandler: ErrorHandlerService) {
  }

  public getWorkflowRuns(damId: string, viewId: String, wesAccessToken: string, nextPage: string = ''): Observable<WorkflowRunsResponse> {
    if (!wesAccessToken) {
      throw new Error('Undefined Access Token');
    }

    return this.http.get<WorkflowRunsResponse>(
      this.resolveUrl(`${damId}/views/${viewId}/runs`, {accessToken: wesAccessToken, nextPage: nextPage})
    );
  }

  public getAllWesViews(): Observable<SimplifiedWesResourceViews[]> {
    return this.http.get<WesResourceViews[]>(`${environment.ddapAlphaApiUrl}/realm/${realmIdPlaceholder}/wes/views`)
      .pipe(
        map((wesResources: WesResourceViews[]) => {
          return wesResources.map(SimplifiedWesResourceViews.fromWesResourceViews);
        })
      );
  }

  public runWorkflow(damId: string, viewId: String, model: WorkflowExecutionModel, wesAccessToken: string): Observable<any> {
    if (!wesAccessToken) {
      throw new Error('Undefined Access Token');
    }

    return this.http.post(
      this.resolveUrl(`${damId}/views/${viewId}/runs`, {accessToken: wesAccessToken}),
      { ...model }
    );
  }

  public workflowRunDetail(damId: string, viewId: string, runId: string, wesAccessToken: string): Observable<any> {
    if (!wesAccessToken) {
      throw new Error('Undefined Access Token');
    }

    return this.http.get(
      this.resolveUrl(`${damId}/views/${viewId}/runs/${runId}`, {accessToken: wesAccessToken})
    ).pipe(
      this.errorHandler.notifyOnError()
    );
  }

  public getJsonSchemaFromWdl(wdl: string): Observable<any> {
    return this.http.post(this.resolveUrl('describe'),
      wdl
    ).pipe(
      this.errorHandler.notifyOnError()
    );
  }

  public getResourcePathForView(damId: string, viewId: string, wesResourceViews: SimplifiedWesResourceViews[]): string {
    const resourcePaths: string[] = wesResourceViews
      .filter((wesResourceView) => wesResourceView.damId === damId)
      .map((wesResourceView) => {
        const view = wesResourceView.views.find((resourceView) => {
          return resourceView.name === viewId;
        });
        if (view) {
          return view.resourcePath;
        }
      });
    return resourcePaths[0];
  }

  private getBaseUrl(): string {
    return `${environment.ddapAlphaApiUrl}/realm/${realmIdPlaceholder}/wes`;
  }

  private resolveUrl(path: string, queryStringMap?: any) {
    const url = `${environment.ddapAlphaApiUrl}/realm/${realmIdPlaceholder}/wes/${path}`;

    if (queryStringMap) {
      return url + '?' + Object.keys(queryStringMap).map(key => `${key}=${queryStringMap[key]}`).join('&');
    }

    return url;
  }

}
