import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { Subscription } from 'rxjs';
import { filter, flatMap, mergeAll } from 'rxjs/operators';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import IResourceTokens = dam.v1.IResourceTokens;
import { AppConfigService } from '../../shared/app-config/app-config.service';
import { BeaconResponse } from '../../shared/beacon-search/beacon-response.model';
import { BeaconSearchParams } from '../../shared/beacon-search/beacon-search-params.model';
import { ResourceBeaconService } from '../../shared/beacon-search/resource-beacon.service';
import { ImagePlaceholderRetriever } from '../../shared/image-placeholder.service';
import { dam } from '../../shared/proto/dam-service';
import { ResourceService } from '../../shared/resource/resource.service';
import { DataService } from '../data.service';

@Component({
  selector: 'ddap-resource-detail',
  templateUrl: './data-search.component.html',
  styleUrls: ['./data-search.component.scss'],
  providers: [ImagePlaceholderRetriever, ResourceBeaconService],
})
export class DataSearchComponent implements OnDestroy, OnInit {

  resource: string;
  resourceName$:  Observable<string>;
  views: any;
  results: BeaconResponse[] = [];
  resultsAction: Subscription;
  limitSearch = false;
  resourceAuthUrl: string;
  requireAuth = true;

  private queryParamsSubscription: Subscription;
  private searchParams: BeaconSearchParams;

  constructor(private route: ActivatedRoute,
              private appConfigService: AppConfigService,
              private router: Router,
              private activatedRoute: ActivatedRoute,
              private dataService: DataService,
              private beaconService: ResourceBeaconService,
              private resourceService: ResourceService) {
  }

  ngOnInit() {
    // Ensure that the user can only access this component when it is enabled.
    this.appConfigService.get().subscribe((data: AppConfigModel) => {
      if (data.featureExploreDataEnabled) {
        this.initialize();
      } else {
        this.router.navigate(['/']);
      }
    });
  }

  ngOnDestroy() {
    this.queryParamsSubscription.unsubscribe();
  }

  limitSearchChange($event) {
    const limitSearch = $event.checked;
    const searchParams: BeaconSearchParams = {
      ...this.searchParams,
       // Don't put a boolean into this map, so that we are always pulling out the limitSearch as a string
      limitSearch: limitSearch + '',
    };
    this.router.navigate(['.'], {
      relativeTo: this.activatedRoute, replaceUrl: true,
      queryParams: { ...searchParams },
    });
  }

  private initialize() {
    this.queryParamsSubscription = this.route.queryParams
      .subscribe(({ auth, ...searchState }: any) => {
        this.results = [];
        this.initializeComponentFields(searchState);
        const queryParams = { ...searchState };

        let allBeaconsResponse = [];
        let damIdResourcePathPairs: string[];
        this.resultsAction = this.beaconService.query(queryParams)
          .pipe(
            filter((beaconResponse) => beaconResponse && beaconResponse.length > 0),
            flatMap((beaconResponse: BeaconResponse[]) => {
              allBeaconsResponse = beaconResponse;
              damIdResourcePathPairs = beaconResponse.map((result) => {
                return `${result.beaconInfo.damId};${result.beaconInfo.resourcePath}`;
              });
              return this.resourceService.getAccessTokensForAuthorizedResources(damIdResourcePathPairs);
            }),
            flatMap((resourceTokens: IResourceTokens) => {
              this.requireAuth = false;
              return damIdResourcePathPairs
                .map((damIdResourcePathPair) => {
                  const damId = damIdResourcePathPair.split(';')[0];
                  const resourcePath = damIdResourcePathPair.split(';')[1];
                  const resourceId = resourcePath.split('/')[0];

                  queryParams.damId = damId;
                  queryParams.resource = resourceId;

                  const accessToken = this.resourceService.lookupResourceToken(resourceTokens, resourcePath)['access_token'];
                  return this.beaconService.query(queryParams, accessToken);
                });
            }),
            mergeAll()
          ).subscribe((beaconResponse: BeaconResponse[]) => {
              beaconResponse.forEach((response) => this.results.push(response));
          },
          (error) => {
            this.requireAuth = true;
            this.results = allBeaconsResponse;
            this.resourceAuthUrl = this.getUrlForObtainingAccessToken(this.getResourcePathsFrom(allBeaconsResponse));
          });
      });
  }

  private initializeComponentFields(searchParams: BeaconSearchParams) {
    this.resource = searchParams.resource;
    if (this.resource) {
      this.resourceName$ = this.dataService.getName(searchParams.damId, this.resource);
    }
    this.searchParams = searchParams;
    this.limitSearch = searchParams.limitSearch === 'true';
  }

  private getResourcePathsFrom(queryResponse: BeaconResponse[]): string[] {
    return queryResponse.map((beaconResponse) => {
      const { beaconInfo } = beaconResponse;
      return `${beaconInfo.damId};${beaconInfo.resourcePath}`;
    });
  }

  private getUrlForObtainingAccessToken(damIdResourcePathPairs: string[] = []): string {
    const redirectUri = this.getRedirectUrl();
    return this.resourceService.getUrlForObtainingAccessToken(damIdResourcePathPairs, encodeURIComponent(redirectUri));
  }

  private getRedirectUrl(): string {
    const currentUrl = this.router.url;
    return `${currentUrl}`;
  }

}
