import { Component, inject, Input, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { UserService } from 'src/app/core/service/business/user.service';
import { of } from 'rxjs';
import { DigitalServiceCloudServiceConfig, DigitalServiceNetworkConfig, DigitalServiceServerConfig } from 'src/app/core/interfaces/digital-service.interfaces';
import { DigitalServiceStoreService } from 'src/app/core/store/digital-service.store';


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

  selectedMenuIndex: number | null = 0;
  



  importForm!: FormGroup;

  importDetails: any;

  ngOnInit(): void {
    this.buildImportDetails();
  }
  

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
private digitalServiceStore = inject(DigitalServiceStoreService);
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

openCloudEditor(cloud: DigitalServiceCloudServiceConfig) {
  this.editingCloud = structuredClone(cloud);
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

}