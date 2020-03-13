import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { CheckinComponent } from './checkin/checkin.component';
import { LobbyComponent } from './lobby/lobby.component';
import { LayoutComponent } from './shared/layout/layout.component';

const routes: Routes = [
  {
    path: '',
    component: CheckinComponent,
  },
  {
    path: ':realmId',
    component: CheckinComponent,
  },
  {
    path: ':realmId/lobby',
    component: LobbyComponent,
  },
  {
    path: ':realmId',
    component: LayoutComponent,
    children: [
      {
        path: 'data',
        loadChildren: () => import('./data/data.module')
          .then(mod => mod.DataModule),
      },
      {
        path: 'analyze',
        loadChildren: () => import('./workflows/workflows.module')
          .then(mod => mod.WorkflowsModule),
      },
      {
        path: 'beacon',
        loadChildren: () => import('./beacon/beacon.module')
          .then(mod => mod.BeaconModule),
      },
      {
        path: 'discovery',
        loadChildren: () => import('./discovery/discovery.module')
          .then(mod => mod.DiscoveryModule),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {
}
