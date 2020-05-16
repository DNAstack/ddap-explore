import { Component, EventEmitter, HostListener, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RealmStateService } from 'ddap-common-lib';
import _get from 'lodash.get';
import { Subscription } from 'rxjs';

import { AppBeacon } from '../../shared/apps/app-discovery/app-discovery.model';
import { AppDiscoveryService } from '../../shared/apps/app-discovery/app-discovery.service';
import { AppSimpleSearchService } from '../../shared/apps/app-simple-search/app-simple-search.service';
import { SPIAppSearchSimple } from '../../shared/apps/app-simple-search/models/app-search-simple.model';
import { CollectionModel } from '../../shared/apps/collection.model';
import { ResourceModel } from '../../shared/apps/resource.model';
import { ResourceService } from '../../shared/apps/resource.service';
import { KeyValuePair } from '../../shared/key-value-pair.model';

@Component({
  selector: 'ddap-workspace',
  templateUrl: './workspace.component.html',
  styleUrls: ['./workspace.component.scss'],
})
export class WorkspaceComponent implements OnInit, OnDestroy {
  inStandaloneMode: boolean;

  collection: CollectionModel;
  beaconResources: ResourceModel[];
  simpleSearchResources: ResourceModel[];

  activeBeaconResource: ResourceModel;
  activeSimpleSearchResource: ResourceModel;

  private initializationEvent = new EventEmitter<void>();
  private initializationEventSubscription: Subscription;
  private routingUpdateSubscription: Subscription;

  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private realmStateService: RealmStateService,
              private resourceService: ResourceService,
              private appDiscoveryService: AppDiscoveryService,
              private appSimpleSearchService: AppSimpleSearchService) {
  }

  ngOnInit() {
    this.initialize();
  }

  ngOnDestroy() {
    this.initializationEventSubscription.unsubscribe();
    this.routingUpdateSubscription.unsubscribe();
  }

  @HostListener('window:resize', ['$event'])
  onBrowserWindowResize(event) {
    const windowObj: Window = event.currentTarget;
    // console.log('innerWidth:', windowObj.innerWidth);
  }

  getBaseUrl(): string {
    return `/${this.getRealmId()}/workspace`;
  }

  getCollectionId(): string {
    return this.activatedRoute.snapshot.paramMap.get('collectionId');
  }

  getResourceType(): string {
    return this.activatedRoute.snapshot.paramMap.get('resourceType');
  }

  getResourceId(): string {
    return this.activatedRoute.snapshot.paramMap.get('resourceId');
  }

  getActiveResource(): { id: string, type: string } {
    const resourceType = this.getResourceType();
    const resourceId = this.getResourceId();
    return resourceId && resourceType ? {id: resourceId, type: resourceType} : null;
  }

  isInitialized(resourceList: object[]) {
    return !this.isNotInitialized(resourceList);
  }

  isNotInitialized(resourceList: object[]) {
    return resourceList === undefined;
  }

  private getRealmId() {
    return _get(this.activatedRoute, 'root.firstChild.snapshot.params.realmId', this.realmStateService.getRealm());
  }

  private initialize() {
    this.resourceService.getCollections().subscribe(response => {
      this.inStandaloneMode = response.data.length === 1;
    });

    // Observe changes in the routing parameters
    // NOTE We don't really need to use paramMap. We just need to make sure that the component needs
    //      to be reinitialized when the parameters are changed.
    this.routingUpdateSubscription = this.activatedRoute.paramMap.subscribe(paramMap => {
      this.initializeView(this.getResourceType(), this.getResourceId());
    });

    this.initializeLayout();
    this.initializeDefaultViewIfRequired();
  }

  private initializeDefaultViewIfRequired() {
    const collectionId = this.getCollectionId();
    const activeResource = this.getActiveResource();

    // Observe the initialization
    this.initializationEventSubscription = this.initializationEvent.subscribe(e => {
      if (activeResource) {
        this.initializeView(activeResource.type, activeResource.id);
      } else {
        // When all data is loaded
        if (this.isInitialized(this.beaconResources) && this.isInitialized(this.simpleSearchResources)) {
          if ((this.beaconResources || []).length > 0) {
            const defaultResource = this.beaconResources[0];
            this.router.navigate([this.getBaseUrl(), collectionId, UIResourceType.Beacon, defaultResource.id]);
          } else if ((this.simpleSearchResources || []).length > 0) {
            const defaultResource = this.simpleSearchResources[0];
            this.router.navigate([this.getBaseUrl(), collectionId, UIResourceType.SimpleSearch, defaultResource.id]);
          } else {
            throw new Error('Misconfiguration detected');
          }
        }
      }
    });
  }

  private initializeLayout() {
    const collectionId = this.getCollectionId();

    if (!this.collection || this.collection.id !== collectionId) {
      // Get the active collection.
      this.resourceService.getCollection(collectionId).subscribe(o => {
        this.collection = o;
      });

      if (this.isNotInitialized(this.beaconResources) && this.isNotInitialized(this.simpleSearchResources)) {
        this.resourceService.getResources({collection: collectionId}).subscribe(o => {
          this.beaconResources = o.data.filter(r => r.interfaces[0].type.match(/^http:beacon/)) || [];
          this.simpleSearchResources = o.data.filter(r => r.interfaces[0].type === 'http:search:table') || [];

          this.initializationEvent.emit();
        });
      }
    }
  }

  private initializeView(type: string, id: string) {
    if (type === null) {
      return;
    }

    if (type === UIResourceType.Beacon) {
      if (!this.beaconResources) {
        return;
      }
      const matchedResources = this.beaconResources.filter(beacon => beacon.id === id);
      this.activeBeaconResource = matchedResources[0];
      this.activeSimpleSearchResource = null;
    } else if (type === UIResourceType.SimpleSearch) {
      if (!this.simpleSearchResources) {
        return;
      }
      const matchedResources = this.simpleSearchResources.filter(simpleSearch => simpleSearch.id === id);
      this.activeBeaconResource = null;
      this.activeSimpleSearchResource = matchedResources[0];
    } else {
      throw new Error(`Unknown resource of type ${type}`);
    }
  }
}

enum UIResourceType {
  Beacon = 'beacon',
  SimpleSearch = 'simple-search',
}
