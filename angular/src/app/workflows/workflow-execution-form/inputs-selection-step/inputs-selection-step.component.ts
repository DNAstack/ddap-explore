import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  ViewEncapsulation,
} from '@angular/core';
import { FormGroup } from '@angular/forms';
import { NoneComponent } from 'angular7-json-schema-form';
import _set from 'lodash.set';

import { WorkflowService } from '../../workflows.service';

import { AutocompleteInputComponent } from './widget/autocomplete-input.component';


@Component({
  selector: 'ddap-inputs-selection-step',
  templateUrl: './inputs-selection-step.component.html',
  styleUrls: ['./inputs-selection-step.component.scss'],
  encapsulation: ViewEncapsulation.None,
  entryComponents: [AutocompleteInputComponent],
})
export class InputsSelectionStepComponent implements OnChanges {

  @Input()
  form: FormGroup;
  @Input()
  datasetColumns: string[] = [];
  @Input()
  inputSchema;

  widgets = {
    text: AutocompleteInputComponent,
    submit: NoneComponent,
  };
  options = {
    defautWidgetOptions: {
      typeahead: {
        source: [],
      },
    },
  };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.datasetColumns) {
      _set(this.options, 'defautWidgetOptions.typeahead.source', changes.datasetColumns.currentValue);
    }
  }

  inputFormChange(inputs) {
    this.form.get('inputs').patchValue(inputs);
  }

}
