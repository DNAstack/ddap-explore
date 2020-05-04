import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DiscoveryBeaconComponent } from './discovery-beacon/discovery-beacon.component';
import { GenomesComponent } from './genomes/genomes.component';
import { MoleculesComponent } from './molecules/molecules.component';
import { VariantsComponent } from './variants/variantscomponent';

export const routes: Routes = [
  { path: 'virus/genomes', component: GenomesComponent},
  { path: 'virus/variants', component: VariantsComponent},
  { path: 'virus/beacon', component: DiscoveryBeaconComponent},
  { path: 'virus/molecules', component: MoleculesComponent},
  { path: '', redirectTo: 'virus/beacon'},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DiscoveryRoutingModule { }
