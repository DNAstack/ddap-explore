import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EntityModel } from 'ddap-common-lib';
import _get from 'lodash.get';
import { Subscription } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { dam } from '../../proto/dam-service';
import { ResourceService } from '../resource.service';
import ResourceTokens = dam.v1.ResourceTokens;
import IResourceToken = dam.v1.ResourceTokens.IResourceToken;
import IResourceTokens = dam.v1.IResourceTokens;



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
  resourceToken: IResourceToken;
  url?: string;

  ttlForm = new FormControl(1, Validators.compose([Validators.required, Validators.min(1)]));
  selectedTimeUnit = 'h';
  // Downloads the same zip file regardless of realm
  downloadCliUrl = `${environment.ddapApiUrl}/master/cli/download`;

  constructor(public resourceService: ResourceService,
              private route: ActivatedRoute,
              private router: Router) {

  }

  ngOnInit(): void {
    this.paramsSubscription = this.route.queryParams
      .subscribe(params => {
        if (!params.checkout) {
          return;
        }
        this.accessSubscription = this.getAccessTokensForAuthorizedResources()
          .subscribe((access) => {
            const resourcePath = `${this.resource.name}/views/${this.view.name}/roles/${this.defaultRole}`;
            this.resourceToken = this.resourceService.lookupResourceToken(access, resourcePath);
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
      this.damId, this.resource.name, this.view.name, this.defaultRole
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

    return `${viewAccessUrl}/o?access_token=${this.resourceToken['access_token']}`;
  }

  getAccessTokensForAuthorizedResources() {
    const resource = this.resourceService.getDamResourcePath(
      this.damId, this.resource.name, this.view.name, this.defaultRole
    );
    return this.resourceService.getAccessTokensForAuthorizedResources([resource]);
  }

}
