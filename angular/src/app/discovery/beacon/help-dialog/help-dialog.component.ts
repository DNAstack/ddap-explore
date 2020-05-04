import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'ddap-help-dialog',
  templateUrl: './help-dialog.html',
})
export class HelpDialogComponent {

  constructor(public dialog: MatDialogRef<HelpDialogComponent>) {
  }

}
