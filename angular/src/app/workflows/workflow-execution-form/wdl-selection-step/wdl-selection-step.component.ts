import { AfterViewInit, Component, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MonacoEditorComponent } from '@materia-ui/ngx-monaco-editor';
import { catchError, map } from 'rxjs/operators';

import { CodeEditorEnhancerService } from '../../../shared/code-editor-enhancer.service';
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

  @ViewChild('editor', {static: true}) editorComponent: MonacoEditorComponent;

  inputSchema;

  wdlCurrentContent: string;

  constructor(private workflowService: WorkflowService,
              public codeEditorEnhancer: CodeEditorEnhancerService) {
  }

  ngOnInit(): void {
    // TODO This will be deprecated in favour of the direct integration with Monaco Editor.
    this.form.statusChanges.subscribe(observer => {
      this.wdlCurrentContent = this.form.get('wdl').value;
    });
  }

  onEditorUpdated() {
    this.form.get('wdl').patchValue(this.wdlCurrentContent);
  }

  computeEditorStyle() {
    const topPadding = 0;
    const minHeight = 300;
    const maxHeight = 500;
    const lineHeight = 18;
    const expectedHeight = (this.wdlCurrentContent || '').split(/\n/).length * lineHeight;
    return {
      height: (Math.min(Math.max(minHeight, expectedHeight), maxHeight) + topPadding) + 'px',
    };
  }

  generateForm() {
    console.warn('generateForm: invoked');
    this.workflowService.getJsonSchemaFromWdl(this.form.get('wdl').value)
      .pipe(
        catchError(e => {
          console.error('Failed while trying to get the script validated:', e);

          throw new Error(e);
        }),
        map(response => {
          if (response.valid !== undefined && response.valid === false) {
            console.error('Fallback: Failed while trying to get the script validated:', response.errors);
          }

          return response;
        })
      )
      .subscribe(({ input_schema: inputSchema }) => {
        console.warn('generateForm: workflowService.getJsonSchemaFromWdl: inputSchema=', inputSchema);
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
