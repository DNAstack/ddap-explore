import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DiscoveryBeaconComponent } from './discovery-beacon/discovery-beacon.component';
import { DiscoverySearchComponent } from './discovery-search/discovery-search.component';

export const routes: Routes = [
  { path: '', redirectTo: 'variants' },
  { path: 'search', component: DiscoverySearchComponent},
  { path: 'variants', component: DiscoveryBeaconComponent},
  // { path: 'operations/:damId/views/:viewId/runs/:runId', component: WorkflowDetailComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DiscoveryRoutingModule { }
