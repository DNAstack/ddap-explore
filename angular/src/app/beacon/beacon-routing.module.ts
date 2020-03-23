import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { BeaconListComponent } from './beacons-list/beacons-list.component';
import { BeaconSearchComponent } from './beacons-search/beacon-search.component';

export const routes: Routes = [
  { path: '', redirectTo: 'network' },
  { path: 'network', component: BeaconListComponent},
  { path: 'search', component: BeaconSearchComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class BeaconsRoutingModule { }
