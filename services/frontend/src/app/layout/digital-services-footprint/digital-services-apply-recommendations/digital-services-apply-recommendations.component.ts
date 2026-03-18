import { Component, computed, EventEmitter, Host, inject, Input, OnInit, Output, Signal, signal, ViewChild } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { UserService } from 'src/app/core/service/business/user.service';
import { firstValueFrom, lastValueFrom, of } from 'rxjs';
import { DigitalServiceCloudServiceConfig, DigitalServiceNetworkConfig, DigitalServiceServerConfig, NetworkType, ServerDC } from 'src/app/core/interfaces/digital-service.interfaces';
import { DigitalServiceStoreService } from 'src/app/core/store/digital-service.store';
import { DigitalServicesDataService } from 'src/app/core/service/data/digital-services-data.service';
import PanelDatacenterComponent from '../digital-services-servers/side-panel/add-datacenter/datacenter.component';
import { InDatacenterRest, InPhysicalEquipmentRest } from 'src/app/core/interfaces/input.interface';
import { InVirtualEquipmentsService } from 'src/app/core/service/data/in-out/in-virtual-equipments.service';
import { InPhysicalEquipmentsService } from 'src/app/core/service/data/in-out/in-physical-equipments.service';


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
      datacenterOptions!: Signal<ServerDC[]>;


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
    this.datacenterOptions = computed(() => {
    return this.digitalServiceStore.inDatacenters().map((datacenter) => ({
      location: datacenter.location,
      name: datacenter.name,
      pue: datacenter.pue,
      displayLabel: datacenter.displayLabel,
      uid: "",
    }));
  });

  if (!this.dsVersionUid) {
    this.dsVersionUid = this.digitalServiceStore.digitalService()?.uid;
  }
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




private readonly inPhysicalEquipmentsService = inject(InPhysicalEquipmentsService);
async onServerSaved() {
  if (!this.editingServer) {
    return;
  }

  const payload: InPhysicalEquipmentRest = {
    id: this.editingServer.id,
    digitalServiceUid: this.editingServer.digitalServiceUid!,
    digitalServiceVersionUid: this.digitalServiceStore.digitalService().uid,
    name: this.editingServer.name,
    datacenterName: this.editingServer.datacenter?.name || "Default DC",
    location: this.editingServer.datacenter?.location ?? "UNKNOWN",
    quantity: this.editingServer.quantity,
    type: this.editingServer.mutualizationType + " Server",
    model: this.editingServer.host?.reference,
    description: this.editingServer.host?.value,
    durationHour: this.editingServer.annualOperatingTime,
    cpuCoreNumber: this.editingServer.totalVCpu,
    sizeDiskGb: this.editingServer.totalDisk,
    electricityConsumption: this.editingServer.annualElectricConsumption,
    creationDate: this.editingServer.creationDate?.toString(),
    
  };


  let updatedServer: InPhysicalEquipmentRest;
  if (this.editingServer.id) {
    updatedServer = await firstValueFrom(
      this.inPhysicalEquipmentsService.update(payload)
    );
  } else {
    return;
  }

  const currentServers = this.digitalServiceStore.inPhysicalEquipments();
  const newServers = currentServers.map((s) =>
    s.id === updatedServer.id
      ? {
          ...updatedServer,
          datacenterName:
            updatedServer.datacenterName || this.editingServer?.datacenter?.name,
          creationDate: updatedServer.creationDate?.toString(),
        }
      : s
  );
  this.digitalServiceStore.setInPhysicalEquipments(newServers);

  this.closeEditor();
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

private calculateQuantity(
  yearlyQuantityOfGbExchanged: number,
  type: NetworkType,
): number {
  if (!yearlyQuantityOfGbExchanged) return 0;
  if (type.type === "Mobile") return yearlyQuantityOfGbExchanged;

  if (type.annualQuantityOfGo && type.annualQuantityOfGo > 0) {
    return yearlyQuantityOfGbExchanged / type.annualQuantityOfGo;
  }
  return 0;
}

async onNetworkSaved() {
  if (!this.editingNetwork) {
    return;
  }
  const calculateQuantity = (yearlyQuantity: number, type: NetworkType) => {
    if (!yearlyQuantity) return 0;
    if (type.type === 'Mobile') return yearlyQuantity;
    if (type.annualQuantityOfGo && type.annualQuantityOfGo > 0) {
      return yearlyQuantity / type.annualQuantityOfGo;
    }
    return 0;
  };

  if (!this.dsVersionUid) {
  return;
}

  // Préparer le payload API
  const payload: InPhysicalEquipmentRest = {
    id: this.editingNetwork.id,
    digitalServiceUid: this.editingNetwork.digitalServiceUid!,
    digitalServiceVersionUid: this.dsVersionUid,
    name: this.editingNetwork.name,
    type: 'Network',
    model: this.editingNetwork.type?.code,
    quantity: calculateQuantity(
      this.editingNetwork.yearlyQuantityOfGbExchanged,
      this.editingNetwork.type!,
    ),
    location: this.editingNetwork.type?.country ?? 'UNKNOWN',
    creationDate: new Date(this.editingNetwork.creationDate ?? Date.now()).toISOString()
  };

  // Appel API
  let updatedNetwork: InPhysicalEquipmentRest;
  if (this.editingNetwork.id) {
    updatedNetwork = await firstValueFrom(
      this.inPhysicalEquipmentsService.update(payload)
    );
  } else {
    updatedNetwork = await firstValueFrom(
      this.inPhysicalEquipmentsService.create(payload)
    );
  }

  // Mise à jour locale dans le store
  const currentEquipments = this.digitalServiceStore.inPhysicalEquipments();
  const otherEquipments = currentEquipments.filter((e) => e.type !== 'Network');
  const networkEquipments = currentEquipments.filter((e) => e.type === 'Network');

  const newNetworks = updatedNetwork.id
    ? networkEquipments.map((n) =>
        n.id === updatedNetwork.id
          ? { ...updatedNetwork, creationDate: updatedNetwork.creationDate }
          : n
      )
    : [...networkEquipments, { ...updatedNetwork, creationDate: updatedNetwork.creationDate }];

  this.digitalServiceStore.setInPhysicalEquipments([...otherEquipments, ...newNetworks]);

  // Fermer l'éditeur
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
private readonly inVirtualEquipmentsService = inject(InVirtualEquipmentsService);

async onCloudSaved() {
    if (!this.editingCloud){ 
      return;}

  const payload = {
    id: this.editingCloud.id,
    digitalServiceUid: this.editingCloud.digitalServiceUid,
    digitalServiceVersionUid: this.digitalServiceStore.digitalService().uid,
    name: this.editingCloud.name,
    infrastructureType: "CLOUD_SERVICES",
    quantity: this.editingCloud.quantity,
    provider: this.editingCloud.cloudProvider,
    instanceType: this.editingCloud.instanceType,
    location: this.editingCloud.location.code,
    durationHour: this.editingCloud.annualUsage,
    workload: this.editingCloud.averageWorkload / 100,
  };

  if (this.editingCloud.id) {
    const res = await firstValueFrom(
      this.inVirtualEquipmentsService.update(payload)
    );
  }

  await this.digitalServiceStore.initInVirtualEquipments(this.dsVersionUid);
   this.closeCloudEditor();
}

get cloudServices(): DigitalServiceCloudServiceConfig[] {
  const data =  this.digitalServiceStore.inVirtualEquipments()
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
     return data ; 
}

onDatacenterChange(dc: ServerDC) {

  this.current.datacenter = dc;

  if (this.editingServer) {
    this.editingServer.datacenter = dc;

  }
}

 recommendationParameters: Record<string, string[]> = {
  'Clouds Publics - IaaS': ['quantity', 'instanceType', 'annualUsage', 'averageWorkload'],
  'Réseaux': ['yearlyQuantityOfGbExchanged', 'type'],
  'Infrastructure Privée': ['datacenter'],
};

@ViewChild(PanelDatacenterComponent)
datacenterPanel!: PanelDatacenterComponent;

}
