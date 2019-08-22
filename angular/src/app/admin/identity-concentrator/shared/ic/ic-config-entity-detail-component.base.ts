import { OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { Subscription } from 'rxjs/Subscription';

import { EntityModel } from '../../../shared/entity.model';
import { FormValidationService } from '../../../shared/form/form-validation.service';

import { IcConfigEntityFormComponentBase } from './ic-config-entity-form-component.base';
import { IcConfigEntityStore } from './ic-config-entity-store';
import { IcConfigStore } from './ic-config.store';

export class IcConfigEntityDetailComponentBase<T extends IcConfigEntityStore>
  extends IcConfigEntityFormComponentBase implements OnInit, OnDestroy {

  entity: EntityModel;

  private subscription: Subscription;

  constructor(protected route: ActivatedRoute,
              protected router: Router,
              protected validationService: FormValidationService,
              protected icConfigStore: IcConfigStore,
              protected entityIcConfigStore: T) {
    super(route, router, validationService);
  }

  get entityId() {
    return this.route.snapshot.params.entityId;
  }

  ngOnInit() {
    this.icConfigStore.init();
    this.subscription = this.entityIcConfigStore.state$
      .pipe(
        map((entities) => {
          if (entities) {
            return entities.get(this.entityId);
          }
        })
      ).subscribe((entity) => {
      this.entity = entity;
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

}
