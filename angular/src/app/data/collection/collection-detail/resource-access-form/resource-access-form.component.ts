import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { filter, flatMap, tap } from 'rxjs/operators';

import { environment } from '../../../../../environments/environment';
import { AccessModel, TokensResponseModel } from '../../../../shared/apps/app-explore.model';
import { AppExploreService } from '../../../../shared/apps/app-explore.service';
import { InterfaceModel, ResourceModel } from '../../../../shared/resource.model';

import { ResourceAccessFormBuilder } from './resource-access-form-builder.service';
import { ResourceAccessFormStateService } from './resource-access-form-state.service';
import { defaultState } from './resource-access-form.model';

@Component({
  selector: 'ddap-resource-access-form',
  templateUrl: './resource-access-form.component.html',
  styleUrls: ['./resource-access-form.component.scss'],
})
export class ResourceAccessFormComponent implements OnInit {

  @Input()
  resource: ResourceModel;

  resourceAccess: AccessModel;
  form: FormGroup;
  authUrl: string;

  readonly downloadCliUrl = `${environment.ddapApiUrlOld}/cli/download`;
  readonly minimumTokensTtl: string = '30s';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private resourceAccessFormBuilder: ResourceAccessFormBuilder,
    private resourceAccessFormStateService: ResourceAccessFormStateService,
    private appExploreService: AppExploreService
  ) {
  }

  get selectedResourceInterface(): InterfaceModel {
    const selectedInterfaceType = this.form.get('interfaceType').value;
    return this.resource.interfaces
      .find((resourceInterface: InterfaceModel) => {
        return resourceInterface.type === selectedInterfaceType;
      });
  }

  get resourceAuthorizationId(): string {
    return this.selectedResourceInterface.authorizationId;
  }

  get timeToLiveDuration(): string {
    const { numericValue, timeUnit } = this.form.get('ttl').value;
    return `${numericValue}${timeUnit}`;
  }

  ngOnInit(): void {
    const formState = this.resourceAccessFormStateService.getFormState(this.resource.id);
    this.form = this.resourceAccessFormBuilder.buildForm(formState);

    if (formState && Object.values(formState).length > 0) {
      this.getTokensForSelectedResourceInterface();
    }
    this.setUpFormValueChanges();
  }

  resetFormStateToDefaults(): void {
    this.resourceAccessFormStateService.removeFormState(this.resource.id);
    this.form.reset(defaultState);
  }

  private getTokensForSelectedResourceInterface() {
    this.appExploreService.getTokens(
      [this.resourceAuthorizationId],
      { minimum_ttl: this.minimumTokensTtl }
      )
      .subscribe((tokensResponse: TokensResponseModel) => {
        this.processTokensResponse(tokensResponse);
      });
  }

  private setUpFormValueChanges() {
    this.form.valueChanges
      .pipe(
        filter(() => this.form.valid),
        tap(() => this.resourceAccessFormStateService.storeFormState(this.resource.id, this.form.value)),
        tap(() => this.authUrl = this.buildUrlForResourceAuthorization()),
        flatMap(() => {
          return this.appExploreService.getTokens(
            [this.resourceAuthorizationId],
            { minimum_ttl: this.minimumTokensTtl }
          );
        })
      )
      .subscribe((tokensResponse: TokensResponseModel) => {
        this.processTokensResponse(tokensResponse);
      });
  }

  private processTokensResponse(tokensResponse: TokensResponseModel) {
    if (tokensResponse.requiresAdditionalAuth) {
      this.authUrl = this.buildUrlForResourceAuthorization(tokensResponse.authorizationUrlBase);
    } else {
      const resourceAuthorizationId = `${this.selectedResourceInterface.authorizationId}`;
      this.resourceAccess = tokensResponse.access[resourceAuthorizationId];
    }
  }

  private buildUrlForResourceAuthorization(authorizationUrlBase?: string): string {
    if (!authorizationUrlBase) {
      const realm = this.route.root.firstChild.snapshot.params.realmId;
      authorizationUrlBase = `${environment.ddapApiUrl}/${realm}/resources/authorize`
        + `?resource=${this.resourceAuthorizationId}`;
    }
    return `${authorizationUrlBase}`
      + `&redirect_uri=${encodeURIComponent(this.router.url)}`
      + `&ttl=${this.timeToLiveDuration}`;
  }

}
