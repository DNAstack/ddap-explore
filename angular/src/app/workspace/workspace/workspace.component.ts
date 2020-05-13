import { Component, EventEmitter, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RealmStateService } from 'ddap-common-lib';
import _get from 'lodash.get';

import { SPIAppBeacon } from '../../shared/spi/app-beacon.model';
import { SPIAppSearchSimple } from '../../shared/spi/app-search-simple.model';
import { SPICollection } from '../../shared/spi/collection.model';
import { SPIAppService } from '../../shared/spi/spi-app.service';
import { SPIService } from '../../shared/spi/spi.service';

@Component({
  selector: 'ddap-workspace',
  templateUrl: './workspace.component.html',
  styleUrls: ['./workspace.component.scss'],
})
export class WorkspaceComponent implements OnInit {

  collection: SPICollection;
  beaconResources: SPIAppBeacon[];
  simpleSearchResources: SPIAppSearchSimple[];

  activeBeaconResource: SPIAppBeacon;
  activeSimpleSearchResource: SPIAppSearchSimple;
  private initializationEvent = new EventEmitter<{ dataType: string }>();

  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private realmStateService: RealmStateService,
              private spiService: SPIService,
              private spiAppService: SPIAppService) {
  }

  ngOnInit() {
    this.initialize();

    // We don't really need to use paramMap. We just need to make sure that the component needs
    // to be reinitialized when the parameters are changed.
    this.activatedRoute.paramMap.subscribe(paramMap => {
      this.initializeView(this.getResourceType(), this.getResourceId());
    });
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

  private getRealmId() {
    return _get(this.activatedRoute, 'root.firstChild.snapshot.params.realmId', this.realmStateService.getRealm());
  }

  private initialize() {
    const collectionId = this.getCollectionId();
    const activeResource = this.getActiveResource();

    const dataTypeLoadedMap = {
      'beacon': false,
      'simple-search': false,
    };

    if (activeResource) {
      // Find the default resource and then redirect to that resource.
      this.initializationEvent.subscribe(e => {
        const dataType = e.dataType;

        if (activeResource.type !== dataType) {
          return;
        }

        this.initializeView(dataType, activeResource.id);
      });
    } else {
      // Find the default resource and then redirect to that resource.
      this.initializationEvent.subscribe(e => {
        const dataType = e.dataType;
        dataTypeLoadedMap[dataType] = true;

        const numberOfLoadedDataTypes = Object.values(dataTypeLoadedMap)
          .reduce((pValue: number, currentDataTypeLoaded: boolean) => {
            return pValue + (currentDataTypeLoaded ? 1 : 0);
          }, 0);

        // When all data is loaded
        if (numberOfLoadedDataTypes === Object.keys(dataTypeLoadedMap).length) {
          if (this.beaconResources.length > 0) {
            const defaultResource = this.beaconResources[0];
            this.router.navigate([this.getBaseUrl(), collectionId, UIResourceType.Beacon, defaultResource.resource.id]);
          } else if (this.simpleSearchResources.length > 0) {
            const defaultResource = this.simpleSearchResources[0];
            this.router.navigate([this.getBaseUrl(), collectionId, UIResourceType.SimpleSearch, defaultResource.resource.id]);
          } else {
            throw new Error('Misconfiguration detected');
          }
        }
      });
    }

    if (!this.collection || this.collection.id !== collectionId) {
      // Get the active collection.
      this.spiService.getCollection(collectionId).subscribe(o => {
        this.collection = o;
      });

      // Get all beacons.
      this.spiAppService.getBeaconResources(collectionId).subscribe(o => {
        this.beaconResources = o.data || [];

        const dataType = 'beacon';
        this.initializationEvent.emit({dataType: dataType});
      });

      // Get all simple searches.
      this.spiAppService.getSimpleSearchResources(collectionId).subscribe(o => {
        this.simpleSearchResources = o.data || [];

        const dataType = 'simple-search';
        this.initializationEvent.emit({dataType: dataType});
      });
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
      const matchedResources = this.beaconResources.filter(beacon => beacon.resource.id === id);
      this.activeBeaconResource = matchedResources[0];
      this.activeSimpleSearchResource = null;
    } else if (type === UIResourceType.SimpleSearch) {
      if (!this.simpleSearchResources) {
        return;
      }
      const matchedResources = this.simpleSearchResources.filter(simpleSearch => simpleSearch.resource.id === id);
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
