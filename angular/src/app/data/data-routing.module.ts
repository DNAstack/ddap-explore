import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'collections',
    loadChildren: () => import('./collection/collections.module')
      .then(mod => mod.CollectionsModule),
  },
  {
    path: 'explorer',
    loadChildren: () => import('./explorer/explorer.module')
      .then(mod => mod.ExplorerModule),
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
  ],
  exports: [RouterModule],
})
export class DataRoutingModule {
}
