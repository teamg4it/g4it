import { Component, computed, Host, inject, Input, OnInit, signal, ViewChild } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { UserService } from 'src/app/core/service/business/user.service';
import { lastValueFrom, of } from 'rxjs';
import { DigitalServiceCloudServiceConfig, DigitalServiceNetworkConfig, DigitalServiceServerConfig, ServerDC } from 'src/app/core/interfaces/digital-service.interfaces';
import { DigitalServiceStoreService } from 'src/app/core/store/digital-service.store';
import { DigitalServicesDataService } from 'src/app/core/service/data/digital-services-data.service';
import PanelDatacenterComponent from '../digital-services-servers/side-panel/add-datacenter/datacenter.component';
import { InDatacenterRest } from 'src/app/core/interfaces/input.interface';


@Component({
  selector: 'app-digital-services-apply-recommendations',
  templateUrl: './digital-services-apply-recommendations.component.html',
  styleUrl: "./digital-services-apply-recommendations.component.scss",
  providers: [
    {
      provide: UserService,
      useValue: {
        isAllowedDigitalServiceWrite$: of(false),
      },
    },
  ],
  
})
export class DigitalServicesApplyRecommendationsComponent implements OnInit {

  @Input() selectedRecommendations: any[] = [];
  @Input() dsVersionUid!: string;
  addSidebarVisible: boolean = false;
  selectedMenuIndex: number | null = 0;
  digitalServiceStore = inject(DigitalServiceStoreService);
      datacenterOptions = computed(() => {
          return this.digitalServiceStore.inDatacenters().map((datacenter) => {
              return {
                  location: datacenter.location,
                  name: datacenter.name,
                  pue: datacenter.pue,
                  displayLabel: datacenter.displayLabel,
                  uid: "",
              } as ServerDC;
          });
      });
          current = {
              host: {} as Host,
              datacenter: {} as ServerDC,
          };
          addDatacenterVisible = false;
  
  private readonly digitalDataService = inject(DigitalServicesDataService);
  
  refreshDatacenters() {
    this.digitalServiceStore.inDatacenters(); 
  }

  addDatacenter(newDc: ServerDC) {
   
    const digitalServiceUid = this.digitalServiceStore.digitalService().uid;
    const datacenterName = `${newDc.name}|${crypto.randomUUID()}`;
  const currentList = this.digitalServiceStore.inDatacenters();

  const newDatacenter: InDatacenterRest = {
    ...newDc,
    digitalServiceUid: this.digitalServiceStore.digitalService().uid,
    displayLabel: `${newDc.name.split("|")[0]} (${newDc.location} - PUE = ${newDc.pue})`,
  };

  this.digitalServiceStore.setInDatacenters([...currentList, newDatacenter]);

  this.addDatacenterVisible = false;


  }


  importForm!: FormGroup;

  importDetails: any;

  ngOnInit(): void {
    this.buildImportDetails();
    this.loadCloudInstanceTypes();
  }
  instanceTypesLoaded = signal(false);
  

  buildImportDetails() {
    this.importDetails = {
      menu: this.selectedRecommendations.map((r) => ({
        title: r.title,
        subTitle: r.category,
        descriptionText: r.description,
        active: false,
        optional: true,
      })),
      form: this.selectedRecommendations.map((_, i) => ({
        name: `rec-${i}`,
      })),
    };

    this.importForm = new FormGroup(
      this.selectedRecommendations.reduce((acc, _, i) => {
        acc[`rec-${i}`] = new FormControl(undefined);
        return acc;
      }, {} as any)
    );

    this.selectTab(0);
  }

  selectTab(index: number) {
    this.selectedMenuIndex = index;
    this.importDetails.menu.forEach((m: any, i: number) => {
      m.active = i === index;
    });
  }

  private async loadCloudInstanceTypes() {
    const providers = await lastValueFrom(
      this.digitalDataService.getBoaviztapiCloudProviders()
    );

    const map = new Map<string, string[]>();
    for (const provider of providers) {
      const types = await lastValueFrom(
        this.digitalDataService.getBoaviztapiInstanceTypes(provider)
      );
      map.set(provider, types);
    }
    map.forEach((v, k) =>
    console.log(`Providerelrjhvbezkrhvbjfbkhbe;j: ${v} → ${v.length} types`)
  );
  this.instanceTypesByProvider.set(map);
  this.instanceTypesLoaded.set(true);
    console.log('[FINAL] Signal mis à jour — providers chargés :', map.size);

  
}

  previousTab(index: number) {
    if (index > 0) {
      this.selectTab(index - 1);
    }
  }

  nextTab(index: number) {
    if (index < this.importDetails.menu.length - 1) {
      this.selectTab(index + 1);
    }
  }

  get currentRecommendation() {
  return this.selectedRecommendations[this.selectedMenuIndex ?? 0];
}

editingServer: DigitalServiceServerConfig | null = null;
openServerEditor(server: DigitalServiceServerConfig) {
  console.log('[PARENT] editEmbedded reçu', server);
  this.editingServer = structuredClone(server);
  console.log('[PARENT] editingServer set', this.editingServer);
  this.digitalServiceStore.setServer(this.editingServer); 
}

closeEditor() {
  this.editingServer = null;
}

onServerSaved() {
    if (this.editingServer) {
    this.digitalServiceStore.setServer(this.editingServer);
  }
  this.closeEditor();
  
  this.digitalServiceStore.setRefresh(Date.now());
}

editingNetwork: DigitalServiceNetworkConfig | null = null;

openNetworkEditor(network: DigitalServiceNetworkConfig) {
    this.editingNetwork = structuredClone(network);
  if (!this.editingNetwork.idFront && this.editingNetwork.id) {
    this.editingNetwork.idFront = this.editingNetwork.id;
  }
}

closeNetworkEditor() {
  this.editingNetwork = null;
}

onNetworkSaved() {
  this.closeNetworkEditor();
}

get allNetworks(): DigitalServiceNetworkConfig[] {
  const networkTypes = this.digitalServiceStore.networkTypes();
  return this.digitalServiceStore.inPhysicalEquipments()
    .filter(e => e.type === "Network")
    .map(e => {
      const type = networkTypes.find(t => t.code === e.model);
      let yearlyQuantityOfGbExchanged = e.quantity;
      if (type?.type === "Fixed") {
        yearlyQuantityOfGbExchanged = type.annualQuantityOfGo * e.quantity;
      }
      return {
        creationDate: e.creationDate,
        id: e.id,
        typeCode: type?.value,
        type,
        yearlyQuantityOfGbExchanged,
        name: e.name,
        digitalServiceUid: e.digitalServiceUid,
      } as DigitalServiceNetworkConfig;
    });
}

editingCloud: DigitalServiceCloudServiceConfig | null = null;
instanceTypesByProvider = signal<Map<string, string[]>>(new Map());
  get editingCloudInstanceTypes() {
    return this.editingCloud 
      ? this.instanceTypesByProvider().get(this.editingCloud.cloudProvider) ?? []
      : [];
  }

openCloudEditor(cloud: DigitalServiceCloudServiceConfig) {
  this.editingCloud = structuredClone(cloud);
    const current = this.instanceTypesByProvider();
}

closeCloudEditor() {
  this.editingCloud = null;
}

onCloudSaved() {
  this.closeCloudEditor();
}

get cloudServices(): DigitalServiceCloudServiceConfig[] {
  return this.digitalServiceStore.inVirtualEquipments()
    .filter(e => e.infrastructureType === "CLOUD_SERVICES")
    .map(e => ({
      id: e.id,
      digitalServiceUid: e.digitalServiceUid,
      name: e.name,
      cloudProvider: e.provider,
      instanceType: e.instanceType,
      quantity: e.quantity,
      location: { code: e.location, name: e.location },
      locationValue: e.location,
      annualUsage: e.durationHour,
      averageWorkload: e.workload! * 100,
    } as DigitalServiceCloudServiceConfig));
}

 recommendationParameters: Record<string, string[]> = {
  'Clouds Publics - IaaS': ['quantity', 'instanceType', 'annualUsage', 'averageWorkload'],
  'Réseaux': ['yearlyQuantityOfGbExchanged', 'type'],
  'Infrastructure Privée': ['datacenter'],
};

@ViewChild(PanelDatacenterComponent)
datacenterPanel!: PanelDatacenterComponent;

}
