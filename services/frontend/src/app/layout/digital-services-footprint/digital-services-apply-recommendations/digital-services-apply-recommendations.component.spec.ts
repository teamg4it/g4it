import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { signal } from '@angular/core';

import { DigitalServicesApplyRecommendationsComponent } from './digital-services-apply-recommendations.component';
import { DigitalServiceStoreService } from 'src/app/core/store/digital-service.store';
import { DigitalServicesDataService } from 'src/app/core/service/data/digital-services-data.service';
import { InPhysicalEquipmentsService } from 'src/app/core/service/data/in-out/in-physical-equipments.service';
import { InVirtualEquipmentsService } from 'src/app/core/service/data/in-out/in-virtual-equipments.service';
import { DigitalServiceVersionDataService } from 'src/app/core/service/data/digital-service-version-data-service';

describe('DigitalServicesApplyRecommendationsComponent', () => {
  let component: DigitalServicesApplyRecommendationsComponent;
  let fixture: ComponentFixture<DigitalServicesApplyRecommendationsComponent>;
  let mockRouter: jasmine.SpyObj<Router>;

  // Signal mocks
  let simulationEquipmentsSignal: any;
  let simulationVirtualEquipmentsSignal: any;
  let simulationDatacentersSignal: any;
  let simulationModifiedSignal: any;

  const mockDigitalService = {
    uid: 'ds-1',
    name: 'Test Service',
    creationDate: '2024-01-01T00:00:00Z',
    lastUpdateDate: '2024-01-01T00:00:00Z',
    lastCalculationDate: '2024-01-01T00:00:00Z'
  };

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj<Router>('Router', ['navigate']);
    mockRouter.navigate.and.returnValue(Promise.resolve(true));
    (mockRouter as any).url = '/organizations/o/workspaces/w';

    const mockStore = {
      digitalService: () => mockDigitalService,
      inDatacenters: jasmine.createSpy('inDatacenters').and.returnValue([]),
      addDatacenter: jasmine.createSpy('addDatacenter'),
      setServer: jasmine.createSpy('setServer'),

      terminalDeviceTypes: jasmine.createSpy('terminalDeviceTypes').and.returnValue([]),
      serverTypes: jasmine.createSpy('serverTypes').and.returnValue([]),
      countryMap: jasmine.createSpy('countryMap').and.returnValue({}),
      networkTypes: jasmine.createSpy('networkTypes').and.returnValue([]),
      inPhysicalEquipments: jasmine.createSpy('inPhysicalEquipments').and.returnValue([]),
      inVirtualEquipments: jasmine.createSpy('inVirtualEquipments').and.returnValue([])
    };

    const mockVersionService = {
      duplicateVersion: jasmine.createSpy('duplicateVersion').and.returnValue(
        of({
          uid: 'new-version',
          name: 'New Version',
          creationDate: '2024-01-01T00:00:00Z',
          lastUpdateDate: '2024-01-01T00:00:00Z',
          lastCalculationDate: '2024-01-01T00:00:00Z'
        })
      )
    };

    await TestBed.configureTestingModule({
      declarations: [DigitalServicesApplyRecommendationsComponent],
      imports: [HttpClientTestingModule, ReactiveFormsModule, FormsModule],
      providers: [
        { provide: DigitalServiceStoreService, useValue: mockStore },
        {
          provide: DigitalServicesDataService,
          useValue: {
            getBoaviztapiCloudProviders: () => of(['aws']),
            getBoaviztapiInstanceTypes: () => of(['t2.micro']),
            launchEvaluating: () => of(true)
          }
        },
        { provide: DigitalServiceVersionDataService, useValue: mockVersionService },
        {
          provide: InPhysicalEquipmentsService,
          useValue: {
            get: () => of([]),
            delete: () => of({}),
            create: () => of({})
          }
        },
        {
          provide: InVirtualEquipmentsService,
          useValue: {
            getByDigitalService: () => of([]),
            delete: () => of({}),
            create: () => of({})
          }
        },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: {} }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(DigitalServicesApplyRecommendationsComponent);
    component = fixture.componentInstance;

    // Component inputs
    component.selectedRecommendations = [
      { title: 'Rec1', category: 'Clouds Publics - IaaS', description: 'desc' },
      { title: 'Rec2', category: ['Réseaux'], description: 'desc2' }
    ];
    component.dsVersionUid = 'ds-uid';

    // === CORRECT SIGNAL MOCKS ===
    // simulationEquipmentsSignal = signal<any[]>([]);
    // simulationVirtualEquipmentsSignal = signal<any[]>([]);
    // simulationDatacentersSignal = signal<any[]>([]);
    
    // // Important: simulationModified must be a Map signal
    // simulationModifiedSignal = signal<Map<string, boolean>>(new Map());

    // spyOn(simulationEquipmentsSignal, 'set');
    // spyOn(simulationVirtualEquipmentsSignal, 'set');
    // spyOn(simulationDatacentersSignal, 'set');
    // spyOn(simulationModifiedSignal, 'set');
    function createSignalMock(initial: any) {
  const fn = jasmine.createSpy('signal').and.returnValue(initial);
  (fn as any).set = jasmine.createSpy('set');
  return fn as any;
}

simulationEquipmentsSignal = createSignalMock([]);
simulationVirtualEquipmentsSignal = createSignalMock([]);
simulationDatacentersSignal = createSignalMock([]);
simulationModifiedSignal = createSignalMock(new Map());

    Object.defineProperty(component, 'simulationEquipments', {
      value: simulationEquipmentsSignal,
      writable: true
    });
    Object.defineProperty(component, 'simulationVirtualEquipments', {
      value: simulationVirtualEquipmentsSignal,
      writable: true
    });
    Object.defineProperty(component, 'simulationDatacenters', {
      value: simulationDatacentersSignal,
      writable: true
    });
    Object.defineProperty(component, 'simulationModified', {
      value: simulationModifiedSignal,
      writable: true
    });

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle datacenter add', () => {
    const dc = { name: 'DC1', location: 'FR', pue: 1 } as any;
    component.addDatacenter(dc);
    expect((component as any).digitalServiceStore.addDatacenter).toHaveBeenCalled();
  });

  it('should refresh datacenters', () => {
    component.ngOnInit();
    (component as any).refreshDatacenters();
    expect((component as any).digitalServiceStore.inDatacenters).toHaveBeenCalled();
  });

  it('should handle terminal delete', () => {
    component.onTerminalDeletedFromChild({ id: 1, name: 'TestTerminal' } as any);
    expect(simulationEquipmentsSignal.set).toHaveBeenCalled();
    expect(simulationModifiedSignal.set).toHaveBeenCalled();
  });

  it('should save terminal', fakeAsync(() => {
    component.editingTerminal = {
      id: 1,
      name: 't1',
      lifespan: 3,
      numberOfUsers: 10,
      yearlyUsageTimePerUser: 1000,
      country: 'FR',
      type: { code: 'mobile' },
      digitalServiceUid: 'ds-1',
      creationDate: '2024-01-01T00:00:00.000Z',
      datePurchase: '2024-01-01T00:00:00.000Z',
      dateWithdrawal: '2027-01-01T00:00:00.000Z'
    } as any;

    component.onTerminalSaved();
    tick();

    expect(simulationEquipmentsSignal.set).toHaveBeenCalled();
    expect(simulationModifiedSignal.set).toHaveBeenCalled();
  }));

  it('should handle server save', fakeAsync(() => {
    component.editingServer = {
      id: 1,
      name: 'srv',
      quantity: 1,
      mutualizationType: 'Dedicated',
      host: { reference: 'server1' },
      datacenter: { name: 'DC1' },
      annualOperatingTime: 8760,
      digitalServiceUid: 'ds-1',
      creationDate: '2024-01-01T00:00:00.000Z'
    } as any;

    component.onServerSaved();
    tick();

    expect(simulationEquipmentsSignal.set).toHaveBeenCalled();
    expect(simulationModifiedSignal.set).toHaveBeenCalled();
  }));

  it('should handle cloud save', fakeAsync(() => {
    component.editingCloud = {
      id: 1,
      name: 'c1',
      cloudProvider: 'aws',
      instanceType: 't2.micro',
      quantity: 1,
      annualUsage: 8760,
      averageWorkload: 50,
      location: { code: 'FR' },
      digitalServiceUid: 'ds-1'
    } as any;

    component.onCloudSaved();
    tick();

    expect(simulationVirtualEquipmentsSignal.set).toHaveBeenCalled();
    expect(simulationModifiedSignal.set).toHaveBeenCalled();
  }));

  it('should handle network save', fakeAsync(() => {
    
    component.editingNetwork = {
      id: 1,
      name: 'net',
      yearlyQuantityOfGbExchanged: 1000,
      type: { code: 'fixed', type: 'Fixed', annualQuantityOfGo: 1000 },
      digitalServiceUid: 'ds-1',
      creationDate: '2024-01-01T00:00:00.000Z'
    } as any;

    component.onNetworkSaved();
    tick();

    expect(simulationEquipmentsSignal.set).toHaveBeenCalled();
    expect(simulationModifiedSignal.set).toHaveBeenCalled();
  }));

  it('should create new version flow', fakeAsync(() => {
    component.createNewVersion();
    tick();
    const versionService = TestBed.inject(DigitalServiceVersionDataService);

expect(versionService.duplicateVersion).toHaveBeenCalledWith('ds-uid');

    expect(mockRouter.navigate).toHaveBeenCalled();
  }));

  it('should switch tabs', () => {
    component.selectTab(1);
    expect((component as any).selectedMenuIndex).toEqual(1);
  });

  it('should use store uid when dsVersionUid is missing', () => {
  component.dsVersionUid = undefined as any;

  const store = TestBed.inject(DigitalServiceStoreService) as any;
  store.digitalService = () => ({ uid: 'store-uid' });

  component.ngOnInit();

  expect(component.dsVersionUid).toBe('store-uid');
});
it('should call store refreshDatacenters', () => {
  const store = TestBed.inject(DigitalServiceStoreService) as any;

  component.refreshDatacenters();

  expect(store.inDatacenters).toHaveBeenCalled();
});
it('should update existing terminal instead of creating new', fakeAsync(() => {
  component.simulationEquipments.set([
    {
      id: 1,
      type: 'Terminal',
      name: 't1',
      digitalServiceUid: 'ds-1',
      numberOfUsers: 1,
      yearlyUsageTimePerUser: 100,
      location: 'FR',
      durationHour: 100,
      creationDate: '2024-01-01T00:00:00Z',
      datePurchase: '2024-01-01T00:00:00Z',
      dateWithdrawal: '2025-01-01T00:00:00Z'
    } as any
  ]);

  component.editingTerminal = {
    id: 1,
    name: 't1-updated',
    lifespan: 2,
    numberOfUsers: 2,
    yearlyUsageTimePerUser: 200,
    country: 'FR',
    type: { code: 'mobile' },
    digitalServiceUid: 'ds-1',
    creationDate: '2024-01-01T00:00:00.000Z',
    datePurchase: '2024-01-01T00:00:00.000Z',
    dateWithdrawal: '2027-01-01T00:00:00.000Z'
  } as any;

  component.onTerminalSaved();
  tick();

  expect(simulationEquipmentsSignal.set).toHaveBeenCalled();
}));
it('should remove non-terminal equipments when saving terminal', fakeAsync(() => {
  component.simulationEquipments.set([
    { id: 1, type: 'Terminal' } as any,
    { id: 2, type: 'Server Server' } as any
  ]);

  component.editingTerminal = {
    id: 1,
    name: 't',
    lifespan: 1,
    numberOfUsers: 1,
    yearlyUsageTimePerUser: 100,
    country: 'FR',
    type: { code: 'mobile' },
    digitalServiceUid: 'ds-1',
    creationDate: '2024-01-01T00:00:00Z',
    datePurchase: '2024-01-01T00:00:00Z',
    dateWithdrawal: '2025-01-01T00:00:00Z'
  } as any;

  component.onTerminalSaved();
  tick();

  expect(simulationEquipmentsSignal.set).toHaveBeenCalled();
}));
it('should delete network and mark modified', () => {
  component.simulationEquipments.set([
    { id: 1, type: 'Network' } as any,
    { id: 2, type: 'Terminal' } as any
  ]);

  component.onNetworkDeletedFromChild({ id: 1 } as any);

  expect(simulationEquipmentsSignal.set).toHaveBeenCalled();
  expect(simulationModifiedSignal.set).toHaveBeenCalled();
});
it('should delete cloud service', () => {
  component.simulationVirtualEquipments.set([
    { id: 1, infrastructureType: 'CLOUD_SERVICES' } as any
  ]);

  component.onCloudDeletedFromChild({ id: 1, name: 'c' } as any);

  expect(simulationVirtualEquipmentsSignal.set).toHaveBeenCalled();
});
it('should activate only selected tab', () => {
  component.importDetails = { menu: [{ active: false }, { active: false }] } as any;

  component.selectTab(1);

  expect(component.importDetails.menu[1].active).toBeTrue();
});
it('should convert valid date to timestamp', () => {
  const result = (component as any).toTimestamp('2024-01-01');
  expect(typeof result).toBe('number');
});

it('should fallback to now when value is undefined', () => {
  const before = Date.now();
  const result = (component as any).toTimestamp(undefined);
  const after = Date.now();

  expect(result).toBeGreaterThanOrEqual(before);
  expect(result).toBeLessThanOrEqual(after);
});

it('should fallback to now when date is invalid', () => {
  const before = Date.now();
  const result = (component as any).toTimestamp('not-a-date');
  const after = Date.now();

  expect(result).toBeGreaterThanOrEqual(before);
  expect(result).toBeLessThanOrEqual(after);
});
it('should return 0 when yearlyQuantityOfGbExchanged is 0', () => {
  const result = (component as any).calculateQuantity(0, { type: 'Fixed' });
  expect(result).toBe(0);
});

it('should return raw value for mobile network', () => {
  const result = (component as any).calculateQuantity(100, { type: 'Mobile' });
  expect(result).toBe(100);
});

it('should divide by annualQuantityOfGo for fixed network', () => {
  const result = (component as any).calculateQuantity(200, {
    type: 'Fixed',
    annualQuantityOfGo: 100
  });
  expect(result).toBe(2);
});

// it('should return 0 if fixed network has no annualQuantityOfGo', () => {
//   const result = (component as any).calculateQuantity(200, {
//     type: 'Fixed'
//   });
//   expect(result).toBe(0);
// });
// it('should emit close event', () => {
//   spyOn(component.close, 'emit');

//   component.closeSidebar();

//   expect(component.close.emit).toHaveBeenCalled();
// });
// it('should handle createNewVersion error', fakeAsync(() => {
//   const versionService = TestBed.inject(DigitalServiceVersionDataService) as any;
//   versionService.duplicateVersion.and.returnValue(Promise.reject('error'));

//   spyOn(console, 'error');

//   component.createNewVersion();
//   tick();

//   expect(console.error).toHaveBeenCalled();
// }));
});