import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { filter, flatMap, mergeAll, pluck } from 'rxjs/operators';

import { ImagePlaceholderRetriever } from '../../../shared/image-placeholder.service';
import { dam } from '../../../shared/proto/dam-service';
import { ResourceService } from '../../../shared/resource/resource.service';
import { DataService } from '../../data.service';

import { BeaconSearchRequestModel, BeaconSearchResponseModel } from './beacon-search.model';
import { BeaconSearchService } from './beacon-search.service';
import IResourceResults = dam.v1.IResourceResults;

@Component({
  selector: 'ddap-collection-beacon-search',
  templateUrl: './collection-beacon-search.component.html',
  styleUrls: ['./collection-beacon-search.component.scss'],
  providers: [ImagePlaceholderRetriever],
})
export class CollectionBeaconSearchComponent implements OnInit {

  collectionName$: Observable<string>;
  queryResultsSubscription: Subscription;

  views: any;
  results: BeaconSearchResponseModel[] = [];
  limitSearch = false;
  resourceAuthUrl: string;
  requireAuth = true;

  private searchParams: BeaconSearchRequestModel;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private dataService: DataService,
    private beaconService: BeaconSearchService,
    private resourceService: ResourceService
  ) {
  }

  ngOnInit() {
    this.initialize();
  }

  limitSearchChange($event) {
    const limitSearch = $event.checked;
    const searchParams: BeaconSearchRequestModel = {
      ...this.searchParams,
      // Don't put a boolean into this map, so that we are always pulling out the limitSearch as a string
      limitSearch: `${limitSearch}`,
    };
    this.router.navigate(['.'], {
      relativeTo: this.activatedRoute,
      replaceUrl: true,
      queryParams: { ...searchParams },
    });
  }

  private initialize() {
    this.route.queryParams
      .subscribe(({ auth, ...searchState }: any) => {
        this.results = [];
        this.initializeComponentFields(searchState);
        const queryParams = { ...searchState };

        let allBeaconsResponse = [];
        let damIdResourcePathPairs: string[];
        this.queryResultsSubscription = this.beaconService.query(queryParams)
          .pipe(
            filter((beaconResponse) => beaconResponse && beaconResponse.length > 0),
            flatMap((beaconResponse: BeaconSearchResponseModel[]) => {
              allBeaconsResponse = beaconResponse;
              damIdResourcePathPairs = beaconResponse.map((result) => {
                return `${result.beaconInfo.damId};${result.beaconInfo.resourcePath}`;
              });
              return this.resourceService.getAccessTokensForAuthorizedResources(damIdResourcePathPairs);
            }),
            flatMap((resourceTokens: IResourceResults) => {
              this.requireAuth = false;
              return damIdResourcePathPairs
                .map((damIdResourcePathPair) => {
                  const damId = damIdResourcePathPair.split(';')[0];
                  const resourcePath = damIdResourcePathPair.split(';')[1];
                  const resourceId = resourcePath.split('/')[0];

                  queryParams.damId = damId;
                  queryParams.resource = resourceId;

                  const accessToken = this.resourceService.lookupResourceToken(
                    resourceTokens,
                    resourcePath
                  ).credentials['access_token'];
                  return this.beaconService.query(queryParams, accessToken);
                });
            }),
            mergeAll()
          ).subscribe(
            (beaconResponse: BeaconSearchResponseModel[]) => {
              beaconResponse.forEach((response) => this.results.push(response));
            },
            (error) => {
              this.requireAuth = true;
              this.results = allBeaconsResponse;
              this.resourceAuthUrl = this.getUrlForObtainingAccessToken(this.getResourcePathsFrom(allBeaconsResponse));
            }
          );
      });
  }

  private initializeComponentFields(searchParams: BeaconSearchRequestModel) {
    const collectionId = searchParams.collection;
    if (collectionId) {
      this.collectionName$ = this.dataService.getCollection(collectionId)
        .pipe(pluck('name'));
    }
    this.searchParams = searchParams;
    this.limitSearch = searchParams.limitSearch === 'true';
  }

  private getResourcePathsFrom(queryResponse: BeaconSearchResponseModel[]): string[] {
    return queryResponse.map((beaconResponse) => {
      const { beaconInfo } = beaconResponse;
      return `${beaconInfo.damId};${beaconInfo.resourcePath}`;
    });
  }

  private getUrlForObtainingAccessToken(damIdResourcePathPairs: string[] = []): string {
    const redirectUri = this.getRedirectUrl();
    return this.resourceService.getUrlForObtainingAccessToken(damIdResourcePathPairs, redirectUri);
  }

  private getRedirectUrl(): string {
    const currentUrl = this.router.url;
    return `${currentUrl}`;
  }

}
