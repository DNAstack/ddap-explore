import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EntityModel } from 'ddap-common-lib';
import _get from 'lodash.get';
import { Subscription } from 'rxjs';
import IResourceAccess = dam.v1.ResourceResults.IResourceAccess;
import { shareReplay } from 'rxjs/operators';

import { environment } from '../../../../environments/environment';
import { dam } from '../../proto/dam-service';
import { ResourceService } from '../resource.service';

@Component({
  selector: 'ddap-resource-view-item',
  templateUrl: './resource-view-item.component.html',
  styleUrls: ['./resource-view-item.component.scss'],
})
export class ResourceViewItemComponent implements OnInit, OnDestroy {

  get defaultRole(): string {
    const roles = Object.keys(_get(this.resource, `dto.views.${this.view.name}.roles`, {}));
    return roles.length > 0 ? roles[0] : '';
  }

  @Input()
  resource: EntityModel;
  @Input()
  view: EntityModel;
  @Input()
  damId: string;

  paramsSubscription: Subscription;
  accessSubscription: Subscription;
  resourceAccess: IResourceAccess;
  url?: string;

  roles: string[];
  interfaces: string[];

  ttlForm = new FormControl(1, Validators.compose([Validators.required, Validators.min(1)]));
  selectedTimeUnit = 'h';
  downloadCliUrl = `${environment.ddapApiUrl}/cli/download`;
  role: string;
  interfaceId: string;

  constructor(public resourceService: ResourceService,
              private route: ActivatedRoute,
              private router: Router) {

  }

  ngOnInit(): void {
    const {dto} = this.view;
    this.roles = Object.keys(dto.roles);
    this.interfaces = Object.keys(dto.interfaces);
    this.setDefaults();

    this.paramsSubscription = this.route.queryParams
      .subscribe(params => {
        if (!params.checkout) {
          return;
        }
        this.accessSubscription = this.getAccessTokensForAuthorizedResources()
          .subscribe((access) => {
            const resourcePath = `${this.resource.name}/views/${this.view.name}/roles/${this.defaultRole}`;
            this.resourceAccess = this.resourceService.lookupResourceToken(access, resourcePath);
            this.url = this.getUrlIfApplicable();
          });
      });
  }

  ngOnDestroy(): void {
    this.paramsSubscription.unsubscribe();
  }

  getUrlForObtainingAccessToken(): string {
    const redirectUri = `${this.router.url}?checkout=true`;
    const resource = this.resourceService.getDamResourcePath(
      this.damId, this.resource.name, this.view.name, this.role, this.interfaceId
    );
    return this.resourceService.getUrlForObtainingAccessToken([resource], redirectUri);
  }

  getUrlIfApplicable(): string {
    const view = this.resource.dto.views[this.view.name];
    const interfaces = view.interfaces;
    const httpInterfaces = Object.keys(interfaces)
      .filter((viewInterface) => viewInterface.startsWith('http'));

    if (!httpInterfaces.length) {
      return;
    }

    const viewAccessUrl = _get(interfaces, `[${httpInterfaces[0]}].uri[0]`);

    return `${viewAccessUrl}/o?access_token=${this.resourceAccess.credentials['access_token']}`;
  }

  getAccessTokensForAuthorizedResources() {
    const resource = this.resourceService.getDamResourcePath(
      this.damId, this.resource.name, this.view.name, this.role, this.interfaceId
    );
    return this.resourceService
      .getAccessTokensForAuthorizedResources([resource])
      .pipe(
        /*
         * The template will end up making this request twice on a page load,
         * which causes timing issues in e2e tests
         */
        shareReplay(1)
      );
  }

  private setDefaults() {
    this.role = this.roles[0];
    this.interfaceId = this.interfaces[0];
  }
}
