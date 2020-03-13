import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DiscoverySearchComponent } from './discovery-search/discovery-search.component';
import { DiscoveryBeaconComponent } from './discovery-beacon/discovery-beacon.component';

export const routes: Routes = [
  { path: '', redirectTo: 'beacon' },
  { path: 'search', component: DiscoverySearchComponent},
  { path: 'beacon', component: DiscoveryBeaconComponent},
  // { path: 'operations/:damId/views/:viewId/runs/:runId', component: WorkflowDetailComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DiscoveryRoutingModule { }
