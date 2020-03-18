import { Component, Input, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EntityModel } from 'ddap-common-lib';
import _get from 'lodash.get';
import { Observable, of, Subscription } from 'rxjs';
import { filter, flatMap, map, share, shareReplay } from 'rxjs/operators';

import { environment } from '../../../../environments/environment';
import { dam } from '../../proto/dam-service';
import { ResourceService } from '../resource.service';
import IResourceAccess = dam.v1.ResourceResults.IResourceAccess;

@Component({
  selector: 'ddap-resource-view-item',
  templateUrl: './resource-view-item.component.html',
  styleUrls: ['./resource-view-item.component.scss'],
})
export class ResourceViewItemComponent implements OnInit {

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

  pending = false;
  resourceAccess: Observable<IResourceAccess>;
  url: Observable<string | null>;

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

    const resourceResults = this.route
      .queryParams
      .pipe(
        filter(params => params.checkout),
        flatMap(_ => this.getAccessTokensForAuthorizedResources()),
        /*
         * The template will end up making this request twice on a page load,
         * which causes timing issues in e2e tests
         */
        shareReplay()
      );
    resourceResults.subscribe(
      _ => this.pending = false,
      _ => this.pending = false
    );
    this.resourceAccess = resourceResults.pipe(map(results => {
      const resourcePath = `${this.resource.name}/views/${this.view.name}/roles/${this.defaultRole}`;
      return this.resourceService.lookupResourceToken(results, resourcePath);
    }));
    this.url = this.getUrlIfApplicable();
  }

  getUrlForObtainingAccessToken(): string {
    const redirectUri = `${this.router.url}?checkout=true`;
    const resource = this.resourceService.getDamResourcePath(
      this.damId, this.resource.name, this.view.name, this.role, this.interfaceId
    );
    const ttl = `${this.ttlForm.value}${this.selectedTimeUnit}`;
    return this.resourceService.getUrlForObtainingAccessToken([resource], redirectUri, ttl);
  }

  getAccessTokensForAuthorizedResources() {
    const resource = this.resourceService.getDamResourcePath(
      this.damId, this.resource.name, this.view.name, this.role, this.interfaceId
    );
    return this.resourceService
      .getAccessTokensForAuthorizedResources([resource]);
  }

  private getUrlIfApplicable(): Observable<string | null> {
    const view = this.resource.dto.views[this.view.name];
    const interfaces = view.interfaces;
    const httpInterfaces = Object.keys(interfaces)
      .filter((viewInterface) => viewInterface.startsWith('http'));

    if (!httpInterfaces.length) {
      return of(null);
    }

    const viewAccessUrl = _get(interfaces, `[${httpInterfaces[0]}].uri[0]`);

    return this.resourceAccess.pipe(
      map(access => access.credentials['access_token']),
      map(token => `${viewAccessUrl}/o?access_token=${token}`)
    );
  }

  private setDefaults() {
    this.role = this.roles[0];
    this.interfaceId = this.interfaces[0];
  }
}
