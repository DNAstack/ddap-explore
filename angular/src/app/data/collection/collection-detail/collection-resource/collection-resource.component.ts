import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, flatMap, pluck, share } from 'rxjs/operators';

import { environment } from '../../../../../environments/environment';
import { dam } from '../../../../shared/proto/dam-service';
import { InterfaceModel, ResourceModel } from '../../../../shared/resource.model';
import { ResourceAuthService } from '../../../../shared/resource/resource-auth.service';

import { CollectionResourceFormBuilder } from './collection-resource-form-builder.service';

@Component({
  selector: 'ddap-collection-resource',
  templateUrl: './collection-resource.component.html',
  styleUrls: ['./collection-resource.component.scss'],
})
export class CollectionResourceComponent implements OnInit {

  @Input()
  resource: ResourceModel;
  resourceAccesses$: Observable<{ [k: string]: dam.v1.ResourceResults.IResourceAccess }>; // TODO: replace DAM DTO with Common one
  form: FormGroup;
  downloadCliUrl = `${environment.ddapApiUrlOld}/cli/download`;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private resourceAuthService: ResourceAuthService,
    private resourceAccessFormBuilder: CollectionResourceFormBuilder
  ) {
  }

  get selectedResourceInterface(): InterfaceModel {
    const selectedInterfaceType = this.form.get('interfaceType').value;
    return this.resource.interfaces
      .find((resourceInterface: InterfaceModel) => {
        return resourceInterface.type === selectedInterfaceType;
      });
  }

  ngOnInit(): void {
    this.form = this.resourceAccessFormBuilder.buildForm();

    this.checkoutAuthorizedResources();
  }

  getUrlForResourceAuthorization(): string {
    const clearCheckoutQueryParam = (url: string): string => url.replace('?checkout', '');

    const { ttl: timeToLive, timeUnit } = this.form.value;
    const resource = `${this.selectedResourceInterface.authorizationId}`;
    const redirectUri = `${clearCheckoutQueryParam(this.router.url)}?checkout=${resource}`;
    const ttl = `${timeToLive}${timeUnit}`;
    const realm = this.route.root.firstChild.snapshot.params.realmId;

    return `${environment.ddapApiUrl}/${realm}/resources/authorize`
      + `?resource=${resource}`
      + `&redirect_uri=${encodeURIComponent(redirectUri)}`
      + `&ttl=${ttl}`;
  }

  private checkoutAuthorizedResources() {
    this.resourceAccesses$ = this.route.queryParams
      .pipe(
        filter((params) => params.checkout),
        flatMap((params) => {
          const { checkout: authorizationId } = params;
          return this.resourceAuthService.checkoutAuthorizedResources([authorizationId]);
        }),
        pluck('access'),
        share()
      );
  }

}
