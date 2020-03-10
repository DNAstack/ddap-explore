import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';

import { CodeEditorEnhancerService } from '../../../shared/code-editor-enhancer.service';
import { ToolVersion } from '../../trs-v2/tool-version.model';
import { Client } from '../../trs-v2/trs.service';

@Component({
  selector: 'ddap-trs-descriptor',
  templateUrl: './trs-descriptor.component.html',
  styleUrls: ['./trs-descriptor.component.scss'],
})
export class TrsDescriptorComponent implements OnInit {
  descriptorContent: string;
  originalDescriptorContent: string;
  feedbackList: FeedbackItem[];
  private feedbackLastId: number;

  constructor(public dialogRef: MatDialogRef<TrsDescriptorComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData,
              private router: Router,
              private route: ActivatedRoute,
              public codeEditorEnhancer: CodeEditorEnhancerService) {
    this.feedbackList = [];
  }

  hasFeedback(): boolean {
    return !this.data.editable || this.feedbackList.length > 0;
  }

  canSave(): boolean {
    return this.data.editable && (this.descriptorContent !== this.originalDescriptorContent);
  }

  canRun(): boolean {
    return !this.canSave();
  }

  getEditableConfig() {
    return {
      language: 'wdl',
      theme: this.data.editable ? 'vs' : 'vs-dark',
      minimap: {enabled: true},
      readOnly: !this.data.editable,
    };
  }

  addFeedback(feedback: FeedbackItem) {
    feedback.id = ++this.feedbackLastId;
    this.feedbackList.push(feedback);
  }

  clearAllFeedback() {
    this.feedbackList = [];
  }

  getSourceUrl() {
    return `${this.data.version.url}/${this.data.type}/descriptor`;
  }

  ngOnInit() {
    this.data.client.getDescriptorFrom(this.getSourceUrl()).subscribe(content => {
      this.descriptorContent = content;
      this.originalDescriptorContent = content;
    });
  }

  onSaveButtonClick() {
    if (!this.data.editable) {
      this.addFeedback({
        message: 'Saving is disabled.',
        level: 'error',
      });
      return;
    }

    this.addFeedback({
      message: 'Saving is not currently supported.',
      level: 'warn',
    });
  }

  onRunButtonClick() {
    this.addFeedback({
      message: 'One second...',
      level: 'info',
    });

    setTimeout(() => {
      this.dialogRef.close({
        sourceUrl: this.getSourceUrl(),
      });
    }, 1000);
  }

  onIndividualFeedbackClearButtonClick() {
    this.feedbackList = [];
  }

  onCloseButtonClick() {
    this.dialogRef.close();
  }
}

interface FeedbackItem {
  id?: number;
  message: string;
  level: string;
}

export interface DialogData {
  client: Client;
  version: ToolVersion;
  type: string;
  editable: boolean;
}
