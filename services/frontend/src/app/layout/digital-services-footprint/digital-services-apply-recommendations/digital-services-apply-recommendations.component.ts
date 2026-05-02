import { Component, computed, EventEmitter, Host, inject, input, Input, OnInit, Output, Signal, signal, ViewChild } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { UserService } from 'src/app/core/service/business/user.service';
import { firstValueFrom, lastValueFrom, of } from 'rxjs';
import { DigitalServiceCloudServiceConfig, DigitalServiceNetworkConfig, DigitalServiceServerConfig, DigitalServiceTerminalConfig, NetworkType, ServerDC } from 'src/app/core/interfaces/digital-service.interfaces';
import { DigitalServiceStoreService } from 'src/app/core/store/digital-service.store';
import { DigitalServicesDataService } from 'src/app/core/service/data/digital-services-data.service';
import PanelDatacenterComponent from '../digital-services-servers/side-panel/add-datacenter/datacenter.component';
import { InDatacenterRest, InPhysicalEquipmentRest, InVirtualEquipmentRest } from 'src/app/core/interfaces/input.interface';
import { InVirtualEquipmentsService } from 'src/app/core/service/data/in-out/in-virtual-equipments.service';
import { InPhysicalEquipmentsService } from 'src/app/core/service/data/in-out/in-physical-equipments.service';
import { DigitalServiceVersionDataService } from 'src/app/core/service/data/digital-service-version-data-service';
import { ActivatedRoute, Router } from '@angular/router';
import { addDays, differenceInDays } from 'date-fns';


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
private toTimestamp(value: string | number | Date | undefined): number {
  if (!value) return Date.now();

  const d = new Date(value);
  return isNaN(d.getTime()) ? Date.now() : d.getTime();
}
  @Input() selectedRecommendations: any[] = [];
  @Input() dsVersionUid!: string;
  addSidebarVisible: boolean = false;
  selectedMenuIndex: number | null = 0;
  selectedCategoryIndex: number = 0;
  digitalServiceStore = inject(DigitalServiceStoreService);
  datacenterOptions!: Signal<ServerDC[]>;
  @Output() close = new EventEmitter<void>();
  @Input() editableFields: string[] = [];

  simulationEquipments = signal<InPhysicalEquipmentRest[]>([]);
 simulationVirtualEquipments = signal<any[]>([]);

simulationModified = signal<Map<string, boolean>>(new Map());

modifiedRecommendationEquipments = signal<InPhysicalEquipmentRest[]>([]); 


  closeSidebar() {
    this.close.emit();
  }



          current = {
              host: {} as Host,
              datacenter: {} as ServerDC,
          };
          addDatacenterVisible = false;
  
  private readonly digitalDataService = inject(DigitalServicesDataService);
  
  refreshDatacenters() {
    this.digitalServiceStore.inDatacenters(); 
  }



simulationDatacenters = signal<InDatacenterRest[]>([]);

addDatacenter(newDc: ServerDC) {
  const currentSimulation = this.simulationDatacenters();

  const datacenterName = `${newDc.name}|${crypto.randomUUID()}`;
  const newDatacenter: InDatacenterRest = {
    ...newDc,
    digitalServiceUid: this.digitalServiceStore.digitalService().uid,
    name: datacenterName,
    displayLabel: `${newDc.name} (${newDc.location} - PUE = ${newDc.pue})`,
  };

  this.simulationDatacenters.set([...currentSimulation, newDatacenter]);
    this.digitalServiceStore.addDatacenter(newDatacenter);

  this.current.datacenter = newDatacenter;

  if (this.editingServer) {
    this.editingServer.datacenter = this.current.datacenter;
  }

  this.addDatacenterVisible = false;
}

  importForm!: FormGroup;

  importDetails: any;

  ngOnInit(): void {
  this.simulationEquipments.set(
    structuredClone(this.digitalServiceStore.inPhysicalEquipments())
  );

  this.simulationVirtualEquipments.set(
    structuredClone(this.digitalServiceStore.inVirtualEquipments())
  );

  this.buildImportDetails();
  this.loadCloudInstanceTypes();
  this.datacenterOptions = computed(() => {
    const simulation = this.simulationDatacenters();
    const live = this.digitalServiceStore.inDatacenters();

    // Fusion des deux listes et dédoublonnage par `name`
    const combined = [...simulation, ...live];
    const unique = Array.from(
      new Map(combined.map(dc => [dc.name, dc])).values()
    );

    return unique.map((datacenter) => ({
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
         subTitle: Array.isArray(r.category) ? r.category.join(' | ') : r.category,
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

  openTerminalEditor(terminal: DigitalServiceTerminalConfig) {
  this.editingTerminal = structuredClone(terminal);
}

closeTerminalEditor() {
  this.editingTerminal = null;
}
onTerminalSavedFromChild(terminal: DigitalServiceTerminalConfig) {
  this.editingTerminal = terminal;
  this.onTerminalSaved();
}
onTerminalDeletedFromChild(terminal: DigitalServiceTerminalConfig) {
  const newSimulation = this.simulationEquipments()
    .filter(e => e.type !== "Terminal" || e.id !== terminal.id);

  this.simulationEquipments.set(newSimulation);

  const modified = this.simulationModified();
  modified.set(terminal.name, true);
  this.simulationModified.set(new Map(modified));
}

async onTerminalSaved() {
  if (!this.editingTerminal) {
    return;
  }

  const datePurchase = new Date("2020-01-01");
  const dateWithdrawal = addDays(
    datePurchase,
    this.editingTerminal.lifespan * 365
  );

  const updated: InPhysicalEquipmentRest = {
    id: this.editingTerminal.id,
    digitalServiceUid: this.editingTerminal.digitalServiceUid!,
    name: this.editingTerminal.name,
    type: "Terminal",
    model: this.editingTerminal.type?.code,
    location: this.editingTerminal.country,
    numberOfUsers: this.editingTerminal.numberOfUsers,
    quantity:
      (this.editingTerminal.numberOfUsers *
        this.editingTerminal.yearlyUsageTimePerUser) /
      (365 * 24),
    durationHour: this.editingTerminal.yearlyUsageTimePerUser,
    datePurchase: datePurchase.toISOString(),
    dateWithdrawal: dateWithdrawal.toISOString(),
    creationDate: this.editingTerminal.creationDate
      ? (new Date(this.editingTerminal.creationDate)).toISOString()
      : new Date().toISOString(),
  };

    const currentSimulation = this.simulationEquipments();

  const otherEquipments = currentSimulation.filter(
    e => e.type !== "Terminal"
  );
  const terminalEquipments = currentSimulation.filter(
    e => e.type === "Terminal"
  );

  const newTerminals = updated.id
    ? terminalEquipments.map(t =>
        t.id === updated.id
          ? { ...updated, creationDate: updated.creationDate }
          : t
      )
    : [
        ...terminalEquipments,
        { ...updated, creationDate: updated.creationDate },
      ];

this.simulationEquipments.set(
  [...otherEquipments, ...newTerminals].map(e => ({ ...e }))
);

  const modified = this.simulationModified();
  modified.set(updated.name, true);
  this.simulationModified.set(new Map(modified));

  this.closeTerminalEditor();
}
simulatedTerminals = computed(() => {
    const data = this.simulationEquipments();
  const deviceTypes = this.digitalServiceStore.terminalDeviceTypes();

  return this.simulationEquipments()
    .filter(e => e.type === "Terminal")
    .map(e => {
      const deviceType = deviceTypes.find(t => t.code === e.model);

      return {
        id: e.id,
        name: e.name,
        creationDate: e.creationDate,
        typeCode: deviceType?.value,
        type: deviceType,
        lifespan:
          e.datePurchase && e.dateWithdrawal
            ? differenceInDays(
                new Date(e.dateWithdrawal),
                new Date(e.datePurchase)
              ) / 365
            : 0,
        country: e.location,
        numberOfUsers: e.numberOfUsers ?? 0,
        yearlyUsageTimePerUser: e.durationHour ?? 0 ,
        digitalServiceUid: e.digitalServiceUid,
      } as DigitalServiceTerminalConfig;
    });
});
terminalDeviceTypes = this.digitalServiceStore.terminalDeviceTypes();
countries = computed(() => {
  const map = this.digitalServiceStore.countryMap();

  return Object.entries(map).map(([code, label]) => ({
    code,
    label
  }));
});

onNetworkSavedFromChild(network: DigitalServiceNetworkConfig) {
  this.editingNetwork = network;
  this.onNetworkSaved(); 
}


onNetworkDeletedFromChild(network: DigitalServiceNetworkConfig) {
  const newSimulation = this.simulationEquipments()
    .filter(e => e.type !== "Network" || e.id !== network.id);

  this.simulationEquipments.set(newSimulation);

  const modified = this.simulationModified();
  modified.set(network.name, true);
  this.simulationModified.set(new Map(modified));
}

  selectTab(index: number) {
    this.selectedMenuIndex = index;
     this.selectedCategoryIndex = 0; 
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
    
  this.instanceTypesByProvider.set(map);
  this.instanceTypesLoaded.set(true);

  
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

  get currentRecommendation(): any{ 
  return this.selectedRecommendations[this.selectedMenuIndex ?? 0];
}

  get currentCategories(): string[] {
    const cat = (this.currentRecommendation as any)?.category;
    return Array.isArray(cat) ? cat : [cat];
  }

  get currentModifiedCategory(): string{
    return this.currentCategories[this.selectedCategoryIndex];
  }

  get recommendationBlocks(): { category: string; index: number }[] {
  return this.currentCategories.map((cat, i) => ({
    category: cat,
    index: i,
  }));
}

editingServer: DigitalServiceServerConfig | null = null;
openServerEditor(server: DigitalServiceServerConfig) {
  this.editingServer = structuredClone(server);
  this.digitalServiceStore.setServer(this.editingServer); 
}

closeEditor() {
  this.editingServer = null;
}



simulatedServers = computed(() => {
  const serverTypes = this.digitalServiceStore.serverTypes();
  const datacenters =
    this.simulationDatacenters().length
      ? this.simulationDatacenters()
      : this.digitalServiceStore.inDatacenters();

  const virtuals = this.simulationVirtualEquipments();

  const groupedVMs = virtuals.reduce(
    (acc: { [key: string]: any[] }, vm: any) => {
      const key = vm.physicalEquipmentName;
      if (!acc[key]) acc[key] = [];
      acc[key].push(vm);
      return acc;
    },
    {}
  );

  return this.simulationEquipments()
    .filter((e) => e.type && e.type.endsWith(" Server"))
    .map((e) => {
      console.log("SERVER RAW QUANTITY", e.name, e.quantity);
      const serverType =
        serverTypes.find((s) => s.reference === e.model) ||
        serverTypes.find((s) => s.value === e.description);

     const normalize = (name?: string) => name?.split("|")[0];

const datacenter =
  datacenters.find(
    (dc) => normalize(dc.name) === normalize(e.datacenterName)
  ) ?? {
    name: e.datacenterName ?? "Default DC",
    location: e.location ?? "UNKNOWN",
    pue: 1,
  };
  console.log("datacenetr ::",datacenter)

      const vms = groupedVMs[e.name] ?? [];

      const typeAsString = e.type.replace(" Server", "");
      const validMutualization = ["Dedicated", "Virtualized", "Shared"].includes(typeAsString)
        ? typeAsString
        : "Dedicated";

      const lifespan = e.datePurchase && e.dateWithdrawal
        ? differenceInDays(e.dateWithdrawal, e.datePurchase) / 365
        : undefined;

        const sumOfVmQuantity = vms.reduce(
          (sum, vm) => sum + (vm.quantity ?? 0),
          0
        );

        const quantity =
    e.type === "Dedicated Server"
        ? e.quantity * (e.durationHour! / 8760)
        : e.quantity;

      const res =  {
        id: e.id,
        uid: e.id?.toString(),
        name: e.name,
        mutualizationType: validMutualization,
        type: serverType?.type || "Compute",
        quantity: Math.max(0.01, Number(quantity.toFixed(2))),
        quantityVms: `${quantity} (${sumOfVmQuantity})`, 
        host: serverType ?? undefined,
        hostValue: serverType?.value ?? undefined,
        datacenter,
        datacenterName: e.datacenterName?.split("|")[0] ?? "",
        totalVCpu: e.cpuCoreNumber ?? 0,
        totalDisk: e.sizeDiskGb ?? 0,
        annualOperatingTime: e.durationHour ?? 0,
        annualElectricConsumption: e.electricityConsumption ?? 0,
        lifespan,
        vm: vms.map((vm) => ({
          name: vm.name,
          annualOperatingTime: vm.durationHour,
          disk: vm.sizeDiskGb,
          quantity: vm.quantity,
          uid: vm.id?.toString(),
          vCpu: vm.vcpuCoreNumber,
          electricityConsumption: vm.electricityConsumption,
          digitalServiceUid: e.digitalServiceUid,
        })),
        digitalServiceUid: e.digitalServiceUid,
        digitalServiceVersionUid: e.digitalServiceVersionUid,
        creationDate: this.toTimestamp(e.creationDate), 
      } as DigitalServiceServerConfig;
       return res ; 
    });
});





onServerSavedFromChild(server: DigitalServiceServerConfig) {
  this.editingServer = server;
  this.onServerSaved();
}

onServerDeletedFromChild(server: DigitalServiceServerConfig) {
  const newSimulation = this.simulationEquipments()
    .filter(e => e.id !== server.id);

  this.simulationEquipments.set(newSimulation);
}

private readonly inPhysicalEquipmentsService = inject(InPhysicalEquipmentsService);

async onServerSaved() {
  if (!this.editingServer) {
    return;
  }

  const updatedServer: InPhysicalEquipmentRest = {
    id: this.editingServer.id,
    digitalServiceUid: this.editingServer.digitalServiceUid!,
    name: this.editingServer.name,
    datacenterName: this.editingServer.datacenter?.name?.split("|")[0] || "Default DC",
    location: this.editingServer.datacenter?.location ?? "UNKNOWN",
    quantity: this.editingServer.quantity,
    type: this.editingServer.mutualizationType + " Server",
    model: this.editingServer.host?.reference,
    description: this.editingServer.host?.value,
    durationHour: this.editingServer.annualOperatingTime,
    cpuCoreNumber: this.editingServer.totalVCpu,
    sizeDiskGb: this.editingServer.totalDisk,
    electricityConsumption: this.editingServer.annualElectricConsumption,
    creationDate: this.editingServer.creationDate
      ? (new Date(this.editingServer.creationDate)).toISOString()
      : new Date().toISOString(),
  };

  const currentSimulation = this.simulationEquipments();
  const newSimulation = currentSimulation.map((s) =>
    s.id === updatedServer.id
      ? {
          ...updatedServer,
          datacenterName: updatedServer.datacenterName,
          creationDate: updatedServer.creationDate,
        }
      : s
  );

  this.simulationEquipments.set(newSimulation);

  const modified = this.simulationModified();
  modified.set(updatedServer.name, true);
  this.simulationModified.set(new Map(modified));
    this.closeEditor();
}


editingNetwork: DigitalServiceNetworkConfig | null = null;
editingTerminal: DigitalServiceTerminalConfig | null = null;

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
    if (type.type === "Mobile") return yearlyQuantity;
    if (type.annualQuantityOfGo && type.annualQuantityOfGo > 0) {
      return yearlyQuantity / type.annualQuantityOfGo;
    }
    return 0;
  };

  const updatedNetwork: InPhysicalEquipmentRest = {
    id: this.editingNetwork.id,
    digitalServiceUid: this.editingNetwork.digitalServiceUid!,
    name: this.editingNetwork.name,
    type: "Network",
    model: this.editingNetwork.type?.code,
    quantity: calculateQuantity(
      this.editingNetwork.yearlyQuantityOfGbExchanged,
      this.editingNetwork.type!
    ),
    location: this.editingNetwork.type?.country ?? "UNKNOWN",
    creationDate: new Date(
      this.editingNetwork.creationDate ?? Date.now()
    ).toISOString(),
  };

  const currentSimulation = this.simulationEquipments();

  const otherEquipments = currentSimulation.filter((e) => e.type !== "Network");
  const networkEquipments = currentSimulation.filter((e) => e.type === "Network");

  const newNetworks = updatedNetwork.id
    ? networkEquipments.map((n) =>
        n.id === updatedNetwork.id
          ? { ...updatedNetwork, creationDate: updatedNetwork.creationDate }
          : n
      )
    : [...networkEquipments, { ...updatedNetwork, creationDate: updatedNetwork.creationDate }];

  this.simulationEquipments.set([...otherEquipments, ...newNetworks]);

  const modified = this.simulationModified();
  modified.set(updatedNetwork.name, true);
  this.simulationModified.set(new Map(modified));

  this.closeNetworkEditor();
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
onCloudLocationChange(code: string) {
  const found = this.countries().find(c => c.code === code);
  if (found && this.editingCloud) {
    this.editingCloud.location = {
      code: found.code,
      name: found.label
    };
  }
}

closeCloudEditor() {
  this.editingCloud = null;
}
private readonly inVirtualEquipmentsService = inject(InVirtualEquipmentsService);

async onCloudSaved() {
  if (!this.editingCloud) {
    return;
  }

  const updated: InVirtualEquipmentRest = {
    id: this.editingCloud.id,
    digitalServiceUid: this.editingCloud.digitalServiceUid,
    name: this.editingCloud.name,
    infrastructureType: "CLOUD_SERVICES",
    quantity: this.editingCloud.quantity,
    provider: this.editingCloud.cloudProvider,
    instanceType: this.editingCloud.instanceType,
    location: this.editingCloud.location.code,
    durationHour: this.editingCloud.annualUsage,
    workload: this.editingCloud.averageWorkload / 100,
    creationDate: this.editingCloud.creationDate?.toString() ?? new Date().toISOString(),
  };

  const currentSimulation = this.simulationVirtualEquipments();
  const newSimulation = currentSimulation.map((s) =>
    s.id === updated.id
      ? {
          ...updated,
          creationDate: updated.creationDate,
        }
      : s
  );
  this.simulationVirtualEquipments.set(newSimulation);

  const modified = this.simulationModified();
  modified.set(updated.name, true);
  this.simulationModified.set(new Map(modified));

  this.closeCloudEditor();
}

simulatedNetworks = computed(() => {
  const networkTypes = this.digitalServiceStore.networkTypes();

  return this.simulationEquipments()
    .filter(e => e.type === "Network")
    .map(e => {
      
      const type = networkTypes.find(t => t.code === e.model) 
                   ?? networkTypes[0]; 

      let yearlyQuantityOfGbExchanged = e.quantity;
      if (type?.type === "Fixed") {
        yearlyQuantityOfGbExchanged = type.annualQuantityOfGo * e.quantity;
      }

      const creationDate: Date | undefined = e.creationDate
        ? new Date(e.creationDate)
        : undefined;

      return {
        creationDate,
        id: e.id,
        typeCode: type?.value ?? undefined,
        type,          
        yearlyQuantityOfGbExchanged,
        name: e.name,
        digitalServiceUid: e.digitalServiceUid,
      } as DigitalServiceNetworkConfig;
    });
});

simulatedCloudServices = computed(() => {
  const countryMap = this.digitalServiceStore.countryMap(); 
  return this.simulationVirtualEquipments()
    .filter(e => e.infrastructureType === "CLOUD_SERVICES")
    .map(e => ({
      id: e.id,
      digitalServiceUid: e.digitalServiceUid,
      name: e.name,
      cloudProvider: e.provider,
      instanceType: e.instanceType,
      quantity: e.quantity,
        location: {
        code: e.location,
        name: countryMap[e.location] || e.location,
      },
      locationValue: countryMap[e.location] || e.location,
      annualUsage: e.durationHour,
      averageWorkload: e.workload! * 100,
    } as DigitalServiceCloudServiceConfig));
});
onCloudSavedFromChild(cloud: DigitalServiceCloudServiceConfig) {
  this.editingCloud = cloud;
  this.onCloudSaved();
}

onCloudDeletedFromChild(cloud: DigitalServiceCloudServiceConfig) {
  const newSimulation = this.simulationVirtualEquipments()
    .filter(e => e.id !== cloud.id);

  this.simulationVirtualEquipments.set(newSimulation);

  const modified = this.simulationModified();
  modified.set(cloud.name, true);
  this.simulationModified.set(new Map(modified));
}



get liveNetworks(): DigitalServiceNetworkConfig[] {
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


get liveCloudServices(): DigitalServiceCloudServiceConfig[] {
  const data = this.digitalServiceStore.inVirtualEquipments()
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
  return data;
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


private readonly versionService = inject(DigitalServiceVersionDataService);
private readonly router = inject(Router);
private readonly route = inject(ActivatedRoute);

private readonly digitalServicesData = inject(DigitalServicesDataService);
hasAnyModified = computed(() => {
  const modified = this.simulationModified();
  return modified.size > 0 && Array.from(modified.values()).some(Boolean);
});
async createNewVersion() {
  if (!this.dsVersionUid) return;

  try {
    const newVersion = await firstValueFrom(
      this.versionService.duplicateVersion(this.dsVersionUid)
    );

    const newUid = newVersion.uid;

    await this.clearVersionData(newUid);

    await this.applySimulationToVersion(newUid);

    await firstValueFrom(
      this.digitalServicesData.launchEvaluating(newUid)
    );

    const urlSegments = this.router.url.split("/").slice(1);
    const organization = urlSegments[1];
    const workspace = urlSegments[3];

    await this.router.navigate([
      "/organizations",
      organization,
      "workspaces",
      workspace,
      "digital-service-version",
      newUid,
      "footprint",
      "dashboard",
    ]);

  } catch (e) {
    console.error(" createNewVersion failed:", e);
  }
}
private async clearVersionData(versionUid: string) {
  const physicals = await firstValueFrom(
    this.inPhysicalEquipmentsService.get(versionUid)
  );

  await Promise.all(
    physicals.map(equip =>
      firstValueFrom(this.inPhysicalEquipmentsService.delete(equip))
    )
  );

  const virtuals = await firstValueFrom(
    this.inVirtualEquipmentsService.getByDigitalService(versionUid)
  );

  await Promise.all(
    virtuals.map(vequip =>
      firstValueFrom(
        this.inVirtualEquipmentsService.delete(vequip.id!, versionUid)
      )
    )
  );
}
private async applySimulationToVersion(versionUid: string) {
  const physicalSimulation = this.simulationEquipments();
  const virtualSimulation = this.simulationVirtualEquipments();

  // --- PHYSICAL EQUIPMENTS ---
for (const equip of physicalSimulation) {
  const payload: InPhysicalEquipmentRest = {
    ...equip,
    id: undefined,
    inventoryId: equip.inventoryId,
    digitalServiceUid: equip.digitalServiceUid,
    digitalServiceVersionUid: versionUid,

    name: equip.name,
    type: equip.type,

    datacenterName: (equip.datacenterName ?? "").split("|")[0] || "Default DC",
    location: equip.location ?? "UNKNOWN",
      quantity: equip.quantity,//Math.max(1, Math.round(Number(equip.quantity))),

      // Convertis timestamp → ISO string juste pour le payload
    creationDate: equip.creationDate
        ? (new Date(equip.creationDate)).toISOString()
      : new Date().toISOString(),
  };

  console.log("CLEAN PAYLOAD", payload);

  try {
    await firstValueFrom(
      this.inPhysicalEquipmentsService.create(payload)
    );
  } catch (e) {
    console.error(" Error creating physical equipment:", payload, e);
  }
}

  // --- VIRTUAL EQUIPMENTS ---
  for (const vequip of virtualSimulation) {
    const payload: InVirtualEquipmentRest = {
      ...vequip,
      id: undefined,
      digitalServiceVersionUid: versionUid,
      quantity: Number(vequip.quantity) || 1,
      durationHour: Number(vequip.durationHour) || 0,
      workload: Number(vequip.workload) || 0,
    };

    try {
      await firstValueFrom(
        this.inVirtualEquipmentsService.create(payload)
      );
    } catch (e) {
      console.error("Error creating virtual equipment:", payload, e);
    }
  }
}

}
