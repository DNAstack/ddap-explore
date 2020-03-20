import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DiscoveryBeaconComponent } from './discovery-beacon/discovery-beacon.component';
import { DiscoverySearchComponent } from './discovery-search/discovery-search.component';

export const routes: Routes = [
  { path: '', component: DiscoveryBeaconComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DiscoveryRoutingModule { }