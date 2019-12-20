import { Component, Input, OnDestroy, OnInit } from "@angular/core";
import { FormControl, Validators } from '@angular/forms';
import { EntityModel } from 'ddap-common-lib';
import _get from 'lodash.get';
import { Subscription } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { dam } from '../../proto/dam-service';
import { ResourceService } from '../resource.service';
import ResourceToken = dam.v1.ResourceTokens.ResourceToken;
import { ActivatedRoute, Router } from "@angular/router";
import ResourceTokens = dam.v1.ResourceTokens;
import IResourceToken = dam.v1.ResourceTokens.IResourceToken;

@Component({
  selector: 'ddap-resource-view-item',
  templateUrl: './resource-view-item.component.html',
  styleUrls: ['./resource-view-item.component.scss'],
})
export class ResourceViewItemComponent implements OnInit, OnDestroy {

  @Input()
  resource: EntityModel;
  @Input()
  view: EntityModel;
  @Input()
  damId: string;

  paramsSubscription: Subscription;
  accessSubscription: Subscription;
  resourceTokens: ResourceTokens;
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
          .subscribe((access) => this.resourceTokens = access);
      });
  }

  ngOnDestroy(): void {
    this.paramsSubscription.unsubscribe();
  }

  getUrlForObtainingAccessToken(): string {
    const redirectUri = `${this.router.url}?checkout=true`;
    return this.resourceService.getUrlForObtainingAccessToken(this.damId, this.resource.name, this.view.name, 'discovery', redirectUri);
  }

  getUrlIfApplicable(viewName: string, token: string): string {
    const view = this.resource.dto.views[viewName];
    const interfaces = view.interfaces;
    const httpInterfaces = Object.keys(interfaces)
      .filter((viewInterface) => viewInterface.startsWith('http'));

    if (!httpInterfaces.length) {
      return;
    }

    const viewAccessUrl = _get(interfaces, `[${httpInterfaces[0]}].uri[0]`);
    return `${viewAccessUrl}/o?access_token=${token}`;
  }

  getAccessTokensForAuthorizedResources() {
    const resource = this.resourceService.getDamResourcePath(this.damId, this.resource.name, this.view.name, "discovery");
    return this.resourceService.getAccessTokensForAuthorizedResources(resource);
  }

  lookupResourceToken(): IResourceToken {
    if (!this.resourceTokens) {
      return;
    }
    const resource = this.lookupResourceTokenDescriptor();
    return this.resourceTokens.access[resource.access];
  }

  private lookupResourceTokenDescriptor() {
    if (!this.resourceTokens) {
      return;
    }
    const resourcePath = `${this.resource.name}/views/${this.view.name}/roles/discovery`;
    const resourceKey: any = Object.keys(this.resourceTokens.resources)
      .find((key) => {
        return key.includes(resourcePath);
      });
    return this.resourceTokens.resources[resourceKey];
  }

}
