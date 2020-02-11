import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { TrsService } from '../../trs-v2/trs.service';
import { WorkflowService } from '../../workflows.service';
import { WorkflowsStateService } from '../workflows-state.service';

import { callDenovo, md5sum } from './example.wdl';


@Component({
  selector: 'ddap-wdl-selection-step',
  templateUrl: './wdl-selection-step.component.html',
  styleUrls: ['./wdl-selection-step.component.scss'],
})
export class WdlSelectionStepComponent implements OnInit {

  @Input()
  workflowId: string;
  @Input()
  form: FormGroup;

  loadedScript: string | null;

  inputSchema;

  constructor(private workflowService: WorkflowService,
              private route: ActivatedRoute,
              private trsService: TrsService,
              private workflowsStateService: WorkflowsStateService) {
  }

  generateForm() {
    this.workflowService.getJsonSchemaFromWdl(this.form.get('wdl').value)
      .subscribe(({ input_schema: inputSchema }) => {
        this.inputSchema = this.getInputSchemaModel(inputSchema);
      });
  }

  useExample(exampleId: string) {
    if (exampleId === 'md5sum') {
      this.form.get('wdl').patchValue(md5sum);
    } else if (exampleId === 'callDenovo') {
      this.form.get('wdl').patchValue(callDenovo);
    }
  }

  ngOnInit(): void {
    const { toolId, versionId, type } = this.route.snapshot.params;

    if (toolId && versionId && type) {
      this.trsService.getDescriptor(toolId, versionId, type)
        .subscribe(script => this.loadedScript = script);
    }
  }

  private getInputSchemaModel(inputSchema) {
    const { properties, ...rest } = inputSchema;
    for (const key in properties) {
      properties[key].title = key + ' (' + properties[key].title + ')';
    }
    return { properties , ...rest};
  }

}
