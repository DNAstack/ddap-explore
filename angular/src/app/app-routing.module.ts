import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DataAppGuard } from './data/data-app.guard';
import { DiscoveryAppGuard } from './discovery/discovery-app.guard';
import { LobbyComponent } from './lobby/lobby.component';
import { SearchAppGuard } from './search/search-app.guard';
import { CheckinComponent } from './shared/checkin/checkin.component';
import { LayoutComponent } from './shared/layout/layout.component';
import { WorkflowAppGuard } from './workflows/workflow-app.guard';

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
        canActivate: [DataAppGuard],
        loadChildren: () => import('./data/data.module')
          .then(mod => mod.DataModule),
      },
      {
        path: 'analyze',
        canActivate: [WorkflowAppGuard],
        loadChildren: () => import('./workflows/workflows.module')
          .then(mod => mod.WorkflowsModule),
      },
      {
        path: 'discovery',
        canActivate: [DiscoveryAppGuard],
        loadChildren: () => import('./discovery/discovery.module')
          .then(mod => mod.DiscoveryModule),
      },
      {
        path: 'search',
        canActivate: [SearchAppGuard],
        loadChildren: () => import('./search/search.module')
          .then(mod => mod.SearchModule),
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
