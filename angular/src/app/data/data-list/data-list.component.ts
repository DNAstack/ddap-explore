import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EntityModel } from 'ddap-common-lib';
import { Subscription } from 'rxjs';
import { Observable } from 'rxjs';
import { flatMap, map, mergeAll, tap } from 'rxjs/operators';

import { AppConfigModel } from '../../shared/app-config/app-config.model';
import { AppConfigService } from '../../shared/app-config/app-config.service';
import { DamInfoService } from '../../shared/dam/dam-info.service';
import { ImagePlaceholderRetriever } from '../../shared/image-placeholder.service';
import { DataService } from '../data.service';


@Component({
  selector: 'ddap-data-list',
  templateUrl: './data-list.component.html',
  styleUrls: ['./data-list.component.scss'],
  providers: [ImagePlaceholderRetriever],
})
export class DataListComponent implements OnInit, OnDestroy {

  qualifiedResources$: Observable<{ damId: string, entity: EntityModel }[]>;
  routeParamsSubscription: Subscription;

  constructor(
    private dataService: DataService,
    private route: ActivatedRoute,
    public randomImageRetriever: ImagePlaceholderRetriever,
    private appConfigService: AppConfigService,
    private router: Router,
    private damInfoService: DamInfoService
  ) {
  }

  ngOnInit() {
    // Ensure that the user can only access this component when it is enabled.
    this.appConfigService.get()
      .subscribe((data: AppConfigModel) => {
        if (data.featureExploreDataEnabled) {
          this.initialize();
        } else {
          this.router.navigate(['/']);
        }
      });
  }

  ngOnDestroy(): void {
    if (this.routeParamsSubscription) {
      this.routeParamsSubscription.unsubscribe();
    }
  }

  ellipseIfLongerThan(text: string, maxLength: number): string {
    if (text && text.length > maxLength) {
      return `${text.substring(0, maxLength)}...`;
    }
    return text;
  }

  private initialize() {
    this.routeParamsSubscription = this.route.parent.params.subscribe(() => {
      this.qualifiedResources$ = this.damInfoService.getDamUrls()
        .pipe(
          map((damApiUrls) => Array.from(damApiUrls.keys())),
          flatMap((damIds: string[]) => {
            return damIds.map(damId => this.dataService.get(damId)
              .pipe(
                map((ems: EntityModel[]) => ems.map(DataListComponent.qualifier(damId)))
              )
            );
          }),
          mergeAll()
        );
    });
  }

  private static qualifier(damId: string): (em: EntityModel) => { damId: string, entity: EntityModel } {
    return em => {
      return {
        damId: damId,
        entity: em,
      };
    };
  }

}
