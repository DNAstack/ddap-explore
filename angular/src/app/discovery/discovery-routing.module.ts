import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DiscoveryBeaconComponent } from './discovery-beacon/discovery-beacon.component';
import { ProteinsComponent } from './proteins/proteins.component';

export const routes: Routes = [
  { path: 'beacon', component: DiscoveryBeaconComponent},
  { path: 'proteins', component: ProteinsComponent},
  { path: '', redirectTo: 'beacon'},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DiscoveryRoutingModule { }
