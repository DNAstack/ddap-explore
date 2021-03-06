import { Component, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { filter, flatMap, tap } from 'rxjs/operators';

import { environment } from '../../../../../environments/environment';
import { AccessModel, TokensResponseModel } from '../../../../shared/apps/app-explore/app-explore.model';
import { AppExploreService } from '../../../../shared/apps/app-explore/app-explore.service';
import { InterfaceModel, ResourceModel } from '../../../../shared/apps/resource.model';

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
  @Input()
  isPublicAccess = false;

  resourceAccess: AccessModel;
  form: FormGroup;
  authUrl: string;

  readonly downloadCliUrl = `${environment.ddapAlphaApiUrl}/cli/download`;
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

  get resourceInterfaceId(): string {
    return this.selectedResourceInterface.id;
  }

  get timeToLiveDuration(): string {
    const { numericValue, timeUnit } = this.form.get('ttl').value;
    return `${numericValue}${timeUnit}`;
  }

  ngOnInit(): void {
    if (this.isPublicAccess) {
      // Do not continue setting up access form if resource is public
      return;
    }

    const formState = this.resourceAccessFormStateService.getFormState(this.resource.id);
    this.form = this.resourceAccessFormBuilder.buildForm(formState);

    if (formState && Object.values(formState).length > 0) {
      this.getTokensForSelectedResourceInterface();
    }
    this.setUpFormValueChanges();
  }

  isViewableUri(uri: string) {
    return uri.startsWith('http://') || uri.startsWith('https://');
  }

  resetFormStateToDefaults(): void {
    this.resourceAccessFormStateService.removeFormState(this.resource.id);
    this.form.reset(defaultState);
  }

  private getTokensForSelectedResourceInterface() {
    this.appExploreService.getTokens(
      [this.resourceInterfaceId],
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
            [this.resourceInterfaceId],
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
      const resourceInterfaceId = `${this.selectedResourceInterface.id}`;
      this.resourceAccess = tokensResponse.access[resourceInterfaceId];
    }
  }

  private buildUrlForResourceAuthorization(authorizationUrlBase?: string): string {
    if (!authorizationUrlBase) {
      const realm = this.route.root.firstChild.snapshot.params.realmId;
      authorizationUrlBase = `${environment.ddapApiUrl}/${realm}/resources/authorize`
        + `?resource=${this.resourceInterfaceId}`;
    }
    return `${authorizationUrlBase}`
      + `&redirect_uri=${encodeURIComponent(this.router.url)}`
      + `&ttl=${this.timeToLiveDuration}`;
  }

}
