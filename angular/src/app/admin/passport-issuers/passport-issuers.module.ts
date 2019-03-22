import { NgModule } from '@angular/core';

import { AdminSharedModule } from '../shared/shared.module';

import { PassportIssuerDetailComponent } from './passport-issuer-detail/passport-issuer-detail.component';
import { PassportIssuerListComponent } from './passport-issuer-list/passport-issuer-list.component';
import { PassportIssuerManageComponent } from './passport-issuer-manage/passport-issuer-manage.component';

@NgModule({
  declarations: [
    PassportIssuerListComponent,
    PassportIssuerManageComponent,
    PassportIssuerDetailComponent,
  ],
  imports: [
    AdminSharedModule,
  ],
})
export class PassportIssuersModule { }