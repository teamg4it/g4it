import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { of } from 'rxjs';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { DigitalServicesApplyRecommendationsComponent } from './digital-services-apply-recommendations.component';

import { DigitalServiceStoreService } from 'src/app/core/store/digital-service.store';
import { DigitalServicesDataService } from 'src/app/core/service/data/digital-services-data.service';
import { InPhysicalEquipmentsService } from 'src/app/core/service/data/in-out/in-physical-equipments.service';
import { InVirtualEquipmentsService } from 'src/app/core/service/data/in-out/in-virtual-equipments.service';
import { DigitalServiceVersionDataService } from 'src/app/core/service/data/digital-service-version-data-service';
import { Router, ActivatedRoute } from '@angular/router';

describe('DigitalServicesApplyRecommendationsComponent', () => {
  let component: DigitalServicesApplyRecommendationsComponent;
  let fixture: ComponentFixture<DigitalServicesApplyRecommendationsComponent>;

  //   Mock Store principal
  const digitalServiceStoreMock = {
    digitalService: () => ({ uid: 'ds-uid' }),
    inPhysicalEquipments: () => [],
    inVirtualEquipments: () => [],
    inDatacenters: () => [],
    addDatacenter: jasmine.createSpy(),
    terminalDeviceTypes: () => [],
    networkTypes: () => [],
    serverTypes: () => [],
    countryMap: () => ({ FR: 'France' }),
    setServer: jasmine.createSpy(),
  };

  //  Mock API cloud
  const digitalDataServiceMock = {
    getBoaviztapiCloudProviders: () => of(['AWS']),
    getBoaviztapiInstanceTypes: () => of(['t2.micro']),
    launchEvaluating: () => of({}),
  };

  const physicalServiceMock = {
    get: () => of([]),
    delete: () => of({}),
    create: () => of({}),
  };

  const virtualServiceMock = {
    getByDigitalService: () => of([]),
    delete: () => of({}),
    create: () => of({}),
  };

  const versionServiceMock = {
    duplicateVersion: () => of({ uid: 'new-version' }),
  };

  const routerMock = {
    url: '/organizations/1/workspaces/1/test',
    navigate: jasmine.createSpy(),
  };

  const routeMock = {};

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DigitalServicesApplyRecommendationsComponent],
      imports: [ReactiveFormsModule, FormsModule],
      providers: [
        { provide: DigitalServiceStoreService, useValue: digitalServiceStoreMock },
        { provide: DigitalServicesDataService, useValue: digitalDataServiceMock },
        { provide: InPhysicalEquipmentsService, useValue: physicalServiceMock },
        { provide: InVirtualEquipmentsService, useValue: virtualServiceMock },
        { provide: DigitalServiceVersionDataService, useValue: versionServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: ActivatedRoute, useValue: routeMock },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA], // ignore composants enfants
    }).compileComponents();

    fixture = TestBed.createComponent(DigitalServicesApplyRecommendationsComponent);
    component = fixture.componentInstance;

    //   Inputs obligatoires
    component.selectedRecommendations = [
      {
        title: 'Reco 1',
        description: 'Desc',
        category: ['Réseaux'],
      },
    ];

    component.dsVersionUid = 'version-1';

    fixture.detectChanges(); // ngOnInit
  });

  // =========================
  //  BASICS
  // =========================

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form and importDetails', () => {
    expect(component.importDetails).toBeDefined();
    expect(component.importForm).toBeDefined();
    expect(component.importDetails.menu.length).toBe(1);
  });

  // =========================
  //   LOGIQUE
  // =========================

  it('should select tab correctly', () => {
    component.selectTab(0);

    expect(component.selectedMenuIndex).toBe(0);
    expect(component.importDetails.menu[0].active).toBeTrue();
  });

  it('should navigate to next tab', () => {
    component.importDetails.menu.push({ active: false });

    component.nextTab(0);

    expect(component.selectedMenuIndex).toBe(1);
  });

  it('should navigate to previous tab', () => {
    component.importDetails.menu.push({ active: false });

    component.selectTab(1);
    component.previousTab(1);

    expect(component.selectedMenuIndex).toBe(0);
  });

  // =========================
  //   SIGNALS
  // =========================

  it('should detect modifications', () => {
    component.simulationModified.set(new Map([['test', true]]));

    expect(component.hasAnyModified()).toBeTrue();
  });

  it('should return current recommendation', () => {
    expect(component.currentRecommendation.title).toBe('Reco 1');
  });

  it('should compute categories correctly', () => {
    expect(component.currentCategories).toEqual(['Réseaux']);
  });

  // =========================
  //   ACTIONS
  // =========================

  it('should emit close event', () => {
    spyOn(component.close, 'emit');

    component.closeSidebar();

    expect(component.close.emit).toHaveBeenCalled();
  });

  it('should update simulation on network delete', () => {
    component.simulationEquipments.set([
      { id: 1, type: 'Network', name: 'net1' } as any,
    ]);

    component.onNetworkDeletedFromChild({ id: 1, name: 'net1' } as any);

    expect(component.simulationEquipments().length).toBe(0);
  });

  it('should update simulation on cloud delete', () => {
    component.simulationVirtualEquipments.set([
      { id: 1, name: 'cloud1' } as any,
    ]);

    component.onCloudDeletedFromChild({ id: 1, name: 'cloud1' } as any);

    expect(component.simulationVirtualEquipments().length).toBe(0);
  });

  // =========================
  //   CLOUD
  // =========================

  it('should open and close cloud editor', () => {
    const cloud = { id: 1, name: 'cloud' } as any;

    component.openCloudEditor(cloud);
    expect(component.editingCloud).toBeTruthy();

    component.closeCloudEditor();
    expect(component.editingCloud).toBeNull();
  });

  // =========================
  //   SERVER
  // =========================

  it('should open server editor', () => {
    const server = { id: 1, name: 'server' } as any;

    component.openServerEditor(server);

    expect(component.editingServer).toBeTruthy();
    expect(digitalServiceStoreMock.setServer).toHaveBeenCalled();
  });

  it('should close server editor', () => {
    component.editingServer = {} as any;

    component.closeEditor();

    expect(component.editingServer).toBeNull();
  });

  // =========================
  //  NETWORK
  // =========================

  it('should open and close network editor', () => {
    const network = { id: 1, name: 'net' } as any;

    component.openNetworkEditor(network);
    expect(component.editingNetwork).toBeTruthy();

    component.closeNetworkEditor();
    expect(component.editingNetwork).toBeNull();
  });

  // =========================
  //  DATACENTER
  // =========================

  it('should add datacenter', () => {
    const dc = { name: 'DC1', location: 'FR', pue: 1 } as any;

    component.simulationDatacenters.set([]);

    component.addDatacenter(dc);

    expect(component.simulationDatacenters().length).toBe(1);
    expect(digitalServiceStoreMock.addDatacenter).toHaveBeenCalled();
  });

});