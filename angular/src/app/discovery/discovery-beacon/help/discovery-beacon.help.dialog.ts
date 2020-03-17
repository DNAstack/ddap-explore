import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'ddap-discovery-beacon-help-dialog',
  templateUrl: 'discovery-beacon-help-dialog.html',
})
export class DiscoveryBeaconHelpDialogComponent {

  constructor(
    public dialog: MatDialogRef<DiscoveryBeaconHelpDialogComponent>) {}

  closeDialog(): void {
    this.dialog.close();
  }

}
