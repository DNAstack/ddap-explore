import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import VersionPickerModel from './version-picker.model';

@Component({
  selector: 'ddap-registered-workflow-version-picker',
  templateUrl: 'version-picker.component.html',
})
export class VersionPickerComponent {

  constructor(
    public dialogRef: MatDialogRef<VersionPickerComponent>,
    @Inject(MAT_DIALOG_DATA) public data: VersionPickerModel) {}

  onNoClick(): void {
    this.dialogRef.close();
  }

}
