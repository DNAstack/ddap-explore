import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Collection } from '../model/collection';
import { ServiceInfo } from '../model/service-info';

import { DataAppServicesConfigService } from './data-app-services-config.service';

@Injectable({
  providedIn: 'root',
})
export class DataCollectionsConfigService {
  private collections;
  private loaded: boolean;

  constructor(
    private http: HttpClient,
    private configService: DataAppServicesConfigService
    ) {

      this.collections = [
        {
          'id' : 'dnastack',
          'searchServiceId' : 'dnastack-search-service',
          'name' : 'DNAstack',
          'description': 'All collection of resources made searchable by DNAstack.',
          'imgUrl' : 'https://miro.medium.com/max/10272/1*VhbU2aGNDHg7M5Srk5evVA.jpeg',
          'organization' :  {
            'name' : 'DNAstack',
            'contact' : {
              'url' : 'https://www.dnastack.com',
            },
          },
        },
        {
          'id' : 'shgp',
          'searchServiceId' : 'shgp-search-service',
          'name' : 'DNAstack - SHGP',
          'description': 'All collection of resources made searchable by the Secure Health and Genomics Platform.',
          'imgUrl' : 'https://dnastack.com/assets/images/dnastack-logo-gotham.png',
          'organization' :  {
            'name' : 'DNAstack/SHGP',
            'contact' : {
              'url' : 'https://www.dnastack.com',
            },
          },
        },
        {
          'id' : 'human-cell-atlas',
          'searchServiceId' : 'dnastack-search-service',
          'name' : 'Human Cell Atlas',
          'description': 'A comprehensive reference maps of all human cells.',
          'tableFilters' : [
            'search_cloud.human_cell_atlas',
          ],
          'imgUrl' : 'https://www.oxfordmartin.ox.ac.uk/images/_810x540_crop_center-center_none/AdobeStock_Anusorn_StemCell.jpg',
          'organization' :  {
            'name' : 'Human Cell Atlas',
            'contact' : {
              'url' : 'https://www.humancellatlas.org/',
            },
          },
        },
        {
          'id' : 'clinvar',
          'searchServiceId' : 'dnastack-search-service',
          'name' : 'ClinVar',
          'description': 'A public archive of relationships among human variations and phenotypes.',
          'tableFilters' : [
            'search_cloud.clinvar',
          ],
          'imgUrl' : 'https://images.pexels.com/photos/733857/pexels-photo-733857.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260',
          'organization' :  {
            'name' : 'NCBI - NIH',
            'contact' : {
              'url' : 'https://www.ncbi.nlm.nih.gov/clinvar/intro/',
            },
          },
        },
        {
          'id' : 'refseq',
          'searchServiceId' : 'dnastack-search-service',
          'name' : 'RefSeq',
          'description': 'A comprehensive, well-annotated set of reference sequences including genomic, transcript, and protein.',
          'tableFilters' : [
            'search_postgres.public',
          ],
          'imgUrl' : 'https://images.pexels.com/'
            + 'photos/51342/books-education-school-literature-51342.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260',
          'organization' :  {
            'name' : 'NCBI',
            'contact' : {
              'url' : 'https://www.ncbi.nlm.nih.gov/',
            },
          },
        },
        {
          'id' : 'human-phenotype-ontology',
          'searchServiceId' : 'dnastack-search-service',
          'name' : 'Human Phenotype Ontology',
          'description': 'A standardized vocabulary of phenotypic abnormalities encountered in human disease.',
          'tableFilters' : [
            'search_cloud.hpo',
          ],
          'imgUrl' : 'https://images.pexels.com'
            + '/photos/40568/medical-appointment-doctor-healthcare-40568.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260',
          'organization' :  {
            'name' : 'Jackson Laboratory for Genomic Medicine',
            'contact' : {
              'url' : 'https://hpo.jax.org/app/',
            },
          },
        },
        {
          'id' : 'pgp-canada',
          'searchServiceId' : 'dnastack-search-service',
          'name' : 'Personal Genomes Project Canada',
          'description': 'Trait and Whole Genome Sequence data from the inaugural 56 Canadian participants.',
          'imgUrl' : 'https://images.pexels.com'
            + '/photos/1267369/pexels-photo-1267369.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260',
          'tableFilters' : [
            'search_drs.org_ga4gh_drs',
            'search_tables.datasets',
          ],
          'organization' :  {
            'name' : 'The Centre for Applied Genomics (TCAG)',
            'contact' : {
              'url' : 'http://tcag.ca/',
            },
          },
        },
        {
          'id' : 'brca-exchange',
          'searchServiceId' : 'dnastack-search-service',
          'name' : 'BRCA Exchange',
          'description': 'Catalogued of BRCA1 and BRCA2 genetic variants curated and classified by an international experts.',
          'imgUrl' : 'https://www.chatelaine.com'
            + '/wp-content/uploads/2017/10/breast-cancer-screening.jpg',
          'tableFilters' : [
            'search_cloud.brca_exchange',
          ],
          'organization' :  {
            'name' : 'Global Alliance for Genomics & Health (GA4GH)',
            'contact' : {
              'url' : 'http://ga4gh.org/',
            },
          },
        },


        {
          'id' : 'gel-panel-app',
          'searchServiceId' : 'dnastack-search-service',
          'imgUrl' : 'https://images.pexels.com'
            + '/photos/672532/pexels-photo-672532.jpeg?auto=compress&cs=tinysrgb&dpr=2&w=500',
          'name' : 'Panel App',
          'description': 'A crowdsourcing tool to allow gene panels to be shared, downloaded, '
          + 'viewed and evaluated by the Scientific Community.',
          'tableFilters' : [
            'search_cloud.genomics_england',
          ],
          'organization' :  {
            'name' : 'Genomics England',
            'contact' : {
              'url' : 'https://www.internationalgenome.org',
            },
          },
        },
        {
          'id' : 'dbgap',
          'searchServiceId' : 'dnastack-search-service',
          'imgUrl' : 'https://caswellmedical.org'
            + '/wp-content/uploads/2019/03/colorectal-cancer.jpg',
          'name' : 'Database of Genotypes and Phenotypes',
          'description': 'Archive for data and results from studies that have investigated the '
           + 'interaction of genotype and phenotype in Humans.',
          'tableFilters' : [
            'search_cloud.dbgap_phs001554',
            'search_cloud.scr_gecco_crc_susceptibility',
            'search_cloud.organoid_profiling_pc',
          ],
          'organization' :  {
            'name' : 'NCBI',
            'contact' : {
              'url' : 'https://www.ncbi.nlm.nih.gov/gap/',
            },
          },
        },
        {
          'id' : 'beacon-network',
          'searchServiceId' : 'dnastack-beacon-network',
          'imgUrl' : 'https://images.pexels.com'
            + '/photos/1532771/pexels-photo-1532771.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260',
          'name' : 'Beacon Network',
          'description': 'A realtime globally distributed search engine for genomic variants.',
          'tableFilters' : [],
          'organization' :  {
            'name' : 'DNAstack',
            'contact' : {
              'url' : 'https://www.dnastack.com',
            },
          },
        },
        {
          'id' : 'mssng',
          'searchServiceId' : 'dnastack-mssng-search-service',
          'name' : 'MSSNG',
          'description': 'Deep genotype and phenotype data from over 10,000 individuals '
           + 'affected by autism and their families.',
          'tableFilters' : [
          ],
          'imgUrl' : 'https://miro.medium.com'
            + '/max/3000/1*LXJK0X0rLP2ie0vL-4Nt2A.png',
          'organization' :  {
            'name' : 'Autism Speaks',
            'contact' : {
              'url' : 'http://autismspeaks.ca',
            },
          },
        },
      ];
  }

  getCollections(): Collection[] {
    return this.collections;
  }

  getCollectionById(id: string): Collection {
    for (let i = 0; i < this.collections.length; i++) {
      if (this.collections[i].id === id) {
        return this.collections[i];
      }
    }
  }

  getSearchServiceInfoForCollection(collection: Collection): ServiceInfo {
    return this.configService.getServiceById(collection.searchServiceId);
  }
}
