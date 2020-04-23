import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DiscoveryBeaconComponent } from './discovery-beacon/discovery-beacon.component';
import { MoleculesComponent } from './molecules/molecules.component';

export const routes: Routes = [
  { path: 'beacon', component: DiscoveryBeaconComponent},
  { path: 'molecules', component: MoleculesComponent},
  { path: '', redirectTo: 'beacon'},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DiscoveryRoutingModule { }
