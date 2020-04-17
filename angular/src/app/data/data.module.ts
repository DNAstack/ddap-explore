import { NgModule } from '@angular/core';

import { DataRoutingModule } from './data-routing.module';
import { DataSharedModule } from './shared/shared.module';

@NgModule({
  imports: [
    DataSharedModule,
    DataRoutingModule,
  ],
})
export class DataModule {
}
