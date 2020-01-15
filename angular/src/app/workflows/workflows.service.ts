import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorHandlerService, realmIdPlaceholder } from 'ddap-common-lib';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../../environments/environment';

import { WorkflowExecution } from './workflow-execution-form/workflow-execution-step/workflow-execution.model';
import { SimplifiedWesResourceViews, WesResourceViews, WorkflowRunsResponse } from './workflow.model';

@Injectable({
  providedIn: 'root',
})
export class WorkflowService {

  constructor(private http: HttpClient,
              private errorHandler: ErrorHandlerService) {
  }

  public getWorkflowRuns(damId: string, view: String, nextPage: string = ''): Observable<WorkflowRunsResponse> {
    return this.http.get<WorkflowRunsResponse>(
      `${environment.ddapApiUrl}/${realmIdPlaceholder}/wes/${damId}/views/${view}/runs?nextPage=${nextPage}`
    );
  }

  public getAllWesViews(): Observable<SimplifiedWesResourceViews[]> {
    return this.http.get<WesResourceViews[]>(`${environment.ddapApiUrl}/${realmIdPlaceholder}/wes/views`)
      .pipe(
        map((wesResources: WesResourceViews[]) => {
          return wesResources.map(SimplifiedWesResourceViews.fromWesResourceViews);
        })
      );
  }

  public runWorkflow(damId: string, view: String, model: WorkflowExecution): Observable<any> {
    return this.http.post(`${environment.ddapApiUrl}/${realmIdPlaceholder}/wes/${damId}/views/${view}/runs`,
      { ...model }
    );
  }

  public workflowRunDetail(damId: string, viewId: string, runId: string): Observable<any> {
    return this.http.get(`${environment.ddapApiUrl}/${realmIdPlaceholder}/wes/${damId}/views/${viewId}/runs/${runId}`)
      .pipe(
        this.errorHandler.notifyOnError()
      );
  }

  public getJsonSchemaFromWdl(wdl: string): Observable<any> {
    return this.http.post(`${environment.ddapApiUrl}/${realmIdPlaceholder}/wes/describe`,
      wdl
    ).pipe(
      this.errorHandler.notifyOnError()
    );
  }

}
