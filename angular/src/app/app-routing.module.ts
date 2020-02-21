import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { defaultRealm } from 'ddap-common-lib';

import { CheckinComponent } from './checkin/checkin.component';
import { LayoutComponent } from './shared/layout/layout.component';

const routes: Routes = [
  // { path: '', pathMatch: 'full', redirectTo: `/${defaultRealm}/data` },  // handled by CheckinComponent with feature flags
  // { path: ':realmId', pathMatch: 'full', redirectTo: `/:realmId/data` },  // handled by CheckinComponent with feature flags
  {
    path: '',
    component: CheckinComponent,
  },
  {
    path: ':realmId',
    component: CheckinComponent,
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
        path: 'workflows',
        loadChildren: () => import('./workflows/workflows.module')
          .then(mod => mod.WorkflowsModule),
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
