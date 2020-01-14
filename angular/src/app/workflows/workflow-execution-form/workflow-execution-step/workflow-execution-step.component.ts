import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import _cloneDeep from 'lodash.clonedeep';
import _get from 'lodash.get';

import { dam } from '../../../shared/proto/dam-service';
import { ResourceService } from '../../../shared/resource/resource.service';
import { flatten } from '../../../shared/util';
import { SimplifiedWesResourceViews } from '../../workflow.model';
import { WorkflowService } from '../../workflows.service';
import { WorkflowsStateService } from '../workflows-state.service';

import IResourceTokens = dam.v1.IResourceTokens;
import { WorkflowExecution } from './workflow-execution.model';
import IResourceToken = dam.v1.ResourceTokens.IResourceToken;

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
  resourceTokens: {[key: string]: IResourceToken};

  constructor(private route: ActivatedRoute,
              private resourceService: ResourceService,
              private workflowService: WorkflowService,
              private workflowsStateService: WorkflowsStateService) {
  }

  getWorkflowExecutionModels(): WorkflowExecution[] {
    const wdl = this.form.get('wdl').value;
    const tokens = JSON.stringify(this.getTokensModel());

    return this.selectedRows
      .map((row) => {
        const inputs = _cloneDeep(this.form.get('inputs').value);
        this.substituteColumnNamesWithValues(inputs, row);

        return {
          wdl,
          inputsJson: JSON.stringify(inputs),
          tokensJson: tokens,
        };
      });
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

  private getTokensModel(): object {
    const tokensModel = {};
    if (!this.resourceTokens || !this.selectedRows || !this.selectedColumns) {
      return tokensModel;
    }

    const columnData: string[] = this.extractColumnData(this.selectedColumns);
    const { columnDataMappedToViews } = this.workflowsStateService.getMetaInfoForWorkflow(this.workflowId);
    const accessTokens: any[] = [];
    // Add as many time access token as there is file -> 1 token per file
    columnData.forEach((extractedColumnData) => {
      const damIdResourcePathPairs: string[] = columnDataMappedToViews[extractedColumnData];
      damIdResourcePathPairs.forEach((damIdResourcePathPair) => {
        const resourcePath = damIdResourcePathPair.split(';')[1];
        const resourceToken = this.resourceService.lookupResourceTokenFromAccessMap(this.resourceTokens, resourcePath);
        accessTokens.push({ file: extractedColumnData, token: resourceToken['access_token'] });
      });
    });

    accessTokens.forEach((token)  => {
      tokensModel[token.file] = token.token;
    });
    return tokensModel;
  }

  private extractColumnData(columnNames: string[]): string[] {
    return flatten(this.selectedRows
      .map((rowData) => columnNames.map((columnName) => rowData[columnName]))
    ).filter((columnData) => columnData);
  }

}
