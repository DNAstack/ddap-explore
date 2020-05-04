import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { BeaconSearchComponent } from './beacon/beacon-search.component';

export const routes: Routes = [
  { path: '', component: BeaconSearchComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DiscoveryRoutingModule {
}
