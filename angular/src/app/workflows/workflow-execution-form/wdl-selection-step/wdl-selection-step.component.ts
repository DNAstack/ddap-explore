import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';

import { AppConfigService } from '../../../shared/app-config/app-config.service';
import { WorkflowService } from '../../workflows.service';

import { callDenovo, helloWorld, md5sum } from './example.wdl';


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

  inputSchema;

  wdlCurrentContent = '';

  constructor(private workflowService: WorkflowService) {
  }

  ngOnInit(): void {
    this.form.statusChanges.subscribe(observer => {
      this.wdlCurrentContent = this.form.get('wdl').value;
    });
  }

  onEditorUpdated() {
    this.form.get('wdl').patchValue(this.wdlCurrentContent);
  }

  computeEditorStyle() {
    const topPadding = 0;
    const minHeight = 100;
    const maxHeight = 500;
    const lineHeight = 18;
    const expectedHeight = this.wdlCurrentContent.split(/\n/).length * lineHeight;
    return {
      height: (Math.min(Math.max(minHeight, expectedHeight), maxHeight) + topPadding) + 'px',
    };
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
    } else if (exampleId === 'helloWorld') {
      this.form.get('wdl').patchValue(helloWorld);
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
