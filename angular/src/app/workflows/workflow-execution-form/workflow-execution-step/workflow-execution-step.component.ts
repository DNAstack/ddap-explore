import { Component, Input } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { flatten } from 'ddap-common-lib';
import _cloneDeep from 'lodash.clonedeep';
import _get from 'lodash.get';

import IResourceAccess = dam.v1.ResourceResults.IResourceAccess;
import { KeyValuePair } from '../../../shared/key-value-pair.model';
import { dam } from '../../../shared/proto/dam-service';
import { ResourceService } from '../../../shared/resource/resource.service';
import { WorkflowService } from '../../workflows.service';
import { WorkflowsStateService } from '../workflows-state.service';

import { CredentialsModel, WorkflowExecutionModel } from './workflow-execution.model';

@Component({
  selector: 'ddap-workflow-execution-step',
  templateUrl: './workflow-execution-step.component.html',
  styleUrls: ['./workflow-execution-step.component.scss'],
})
export class WorkflowExecutionStepComponent {

  @Input()
  form: FormGroup;
  @Input()
  workflowId: string;
  @Input()
  selectedRows: object[];
  @Input()
  selectedColumns: string[];
  @Input()
  resourceAccesses: {[key: string]: IResourceAccess};

  constructor(private route: ActivatedRoute,
              private resourceService: ResourceService,
              private workflowService: WorkflowService,
              private workflowsStateService: WorkflowsStateService) {
  }

  getWorkflowExecutionModels(): WorkflowExecutionModel[] {
    if (!this.selectedRows) {
      return [];  // Default value
    }

    return this.selectedRows
      .map((row) => {
        const inputs = _cloneDeep(this.form.get('inputs').value);
        this.substituteColumnNamesWithValues(inputs, row);

        return this.createWorkflowExecutionModel(inputs);
      });
  }

  createWorkflowExecutionModel(inputs: any): WorkflowExecutionModel {
    const wdl = this.form.get('wdl').value;

    return {
      wdl,
      credentials: this.getCredentialsModel(),
      inputsJson: inputs,
    };
  }

  private substituteColumnNamesWithValues(object: object, row: object) {
    Object.entries(object).forEach(([key, value]) => {
      switch (typeof object[key]) {
        case 'object':
          this.substituteColumnNamesWithValues(object[key], row); break;
        case 'string':
          if (value.startsWith('${') && value.endsWith('}')) {
            object[key] = _get(row, value.substring(2, value.length - 1), '');
          }
      }
    });
  }

  private getCredentialsModel(): KeyValuePair<CredentialsModel> {
    const credentialsModel: KeyValuePair<CredentialsModel> = {};
    if (!this.resourceAccesses || !this.selectedRows || !this.selectedColumns) {
      return credentialsModel;
    }

    const columnData: string[] = this.extractColumnData(this.selectedColumns);
    const { columnDataMappedToViews } = this.workflowsStateService.getMetaInfoForWorkflow(this.workflowId);
    const accessTokens: any[] = [];
    // Add as many time access token as there is file -> 1 token per file
    columnData.forEach((extractedColumnData) => {
      const damIdResourcePathPairs: string[] = columnDataMappedToViews[extractedColumnData];
      if (damIdResourcePathPairs) {
        damIdResourcePathPairs.forEach((damIdResourcePathPair) => {
          const resourcePath = damIdResourcePathPair.split(';')[1];
          const resourceAccess = this.resourceService.lookupResourceTokenFromAccessMap(this.resourceAccesses, resourcePath);
          accessTokens.push({
            file: extractedColumnData,
            accessKeyId: _get(resourceAccess, 'credentials.access_key_id'),
            accessToken: _get(resourceAccess, 'credentials.access_token', _get(resourceAccess, 'credentials.secret')),
            sessionToken: _get(resourceAccess, 'credentials.session_token'),
          });
        });
      }
    });

    accessTokens.forEach((token)  => {
      credentialsModel[token.file] = {
        accessKeyId: token.accessKeyId,
        accessToken: token.accessToken,
        sessionToken: token.sessionToken,
      };
    });
    return credentialsModel;
  }

  private extractColumnData(columnNames: string[]): string[] {
    return flatten(this.selectedRows
      .map((rowData) => columnNames.map((columnName) => rowData[columnName]))
    ).filter((columnData) => columnData);
  }

}
