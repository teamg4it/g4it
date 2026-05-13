import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { DigitalServicesCloudServicesComponent } from './digital-services-cloud-services.component';
import { Router } from '@angular/router';
import { of } from 'rxjs';

import { InVirtualEquipmentsService } from 'src/app/core/service/data/in-out/in-virtual-equipments.service';
import { DigitalServiceStoreService } from 'src/app/core/store/digital-service.store';
import { UserService } from 'src/app/core/service/business/user.service';

describe('DigitalServicesCloudServicesComponent', () => {
  let component: DigitalServicesCloudServicesComponent;
  let fixture: ComponentFixture<DigitalServicesCloudServicesComponent>;

  let mockRouter: any;
  let mockStore: any;
  let mockService: any;

  beforeEach(async () => {
    mockRouter = {
        url: '/a/b/c/d/e/ds-uid'
        };

    mockStore = {
      initInVirtualEquipments: jasmine.createSpy(),
      setEnableCalcul: jasmine.createSpy(),
      inVirtualEquipments: jasmine.createSpy().and.returnValue([]),
      countryMap: jasmine.createSpy().and.returnValue({ FR: 'France' })
    };

    mockService = {
      delete: jasmine.createSpy().and.returnValue(of({})),
      update: jasmine.createSpy().and.returnValue(of({})),
      create: jasmine.createSpy().and.returnValue(of({}))
    };

    await TestBed.configureTestingModule({
      declarations: [DigitalServicesCloudServicesComponent],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: DigitalServiceStoreService, useValue: mockStore },
        { provide: InVirtualEquipmentsService, useValue: mockService },
        { provide: UserService, useValue: {} }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DigitalServicesCloudServicesComponent);
    component = fixture.componentInstance;

    (component as any).dsVersionUid = () => 'version-1';
    (component as any).embedded = () => false;
    (component as any).cloudDataInput = () => undefined;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // =============================
  // INIT
  // =============================
  it('should init and load services', () => {
    component.ngOnInit();

    expect(component.digitalServiceUid).toBe('ds-uid');
    expect(mockStore.initInVirtualEquipments).toHaveBeenCalledWith('ds-uid');
  });

  // =============================
  // COMPUTED
  // =============================
  it('should use cloudDataInput (array)', () => {
    (component as any).cloudDataInput = () => [{ name: 'c1' }];

    const result = component.cloudServices();

    expect(result.length).toBe(1);
  });

  it('should use cloudDataInput (signal)', () => {
    (component as any).cloudDataInput = () => (() => [{ name: 'c2' }]);

    const result = component.cloudServices();

    expect(result[0].name).toBe('c2');
  });

  it('should fallback to store', () => {
    mockStore.inVirtualEquipments.and.returnValue([
      {
        id: 1,
        infrastructureType: 'CLOUD_SERVICES',
        location: 'FR',
        workload: 0.5,
        durationHour: 10
      }
    ]);

    (component as any).cloudDataInput = () => undefined;

    const result = component.cloudServices();

    expect(result.length).toBe(1);
  });

  // =============================
  // UI
  // =============================
  it('should change sidebar', () => {
    component.changeSidebar(true);
    expect(component.sidebarVisible).toBeTrue();
  });

  it('should set item', () => {
    spyOn(component.editEmbedded, 'emit');

    component.setItem({ index: 2, name: 'cloud' });

    expect(component.cloud.idFront).toBe(2);
    expect(component.editEmbedded.emit).toHaveBeenCalled();
  });

  it('should set item in embedded', () => {
    (component as any).embedded = () => true;
    spyOn(component.editEmbedded, 'emit');

    component.setItem({ index: 1, name: 'cloud' });

    expect(component.editEmbedded.emit).toHaveBeenCalledTimes(2);
  });

  // =============================
  // DELETE ITEM
  // =============================
  it('should delete item embedded', fakeAsync(() => {
    (component as any).embedded = () => true;
    spyOn(component.deleteEmbedded, 'emit');

    component.deleteItem({ id: 1 } as any);
    tick();

    expect(component.deleteEmbedded.emit).toHaveBeenCalled();
  }));

  it('should delete item service', fakeAsync(() => {
    (component as any).embedded = () => false;

    component.deleteItem({ id: 1 } as any);
    tick();

    expect(mockService.delete).toHaveBeenCalled();
    expect(mockStore.setEnableCalcul).toHaveBeenCalled();
  }));

  // =============================
  // UPDATE
  // =============================
  it('should update embedded', fakeAsync(() => {
    (component as any).embedded = () => true;
    spyOn(component.updateEmbedded, 'emit');

    component.updateCloudServices({ name: 'c' } as any);
    tick();

    expect(component.updateEmbedded.emit).toHaveBeenCalled();
  }));

  it('should update existing', fakeAsync(() => {
  component.updateCloudServices({
    id: 1,
    digitalServiceUid: 'ds',
    name: 'c',
    quantity: 1,
    cloudProvider: 'aws',
    instanceType: 't2',
    location: { code: 'FR' },
    annualUsage: 10,
    averageWorkload: 50
  } as any);

  tick();

  expect(mockService.update).toHaveBeenCalled();
}));

it('should create new', fakeAsync(() => {
  component.updateCloudServices({
    digitalServiceUid: 'ds',
    name: 'c',
    quantity: 1,
    cloudProvider: 'aws',
    instanceType: 't2',
    location: { code: 'FR' },
    annualUsage: 10,
    averageWorkload: 50
  } as any);

  tick();

  expect(mockService.create).toHaveBeenCalled();
}));


  // =============================
  // DELETE CLOUD
  // =============================
  it('should delete cloud', fakeAsync(() => {
    component.digitalServiceUid = 'ds';

    component.deleteCloudServices({ id: 1 } as any);
    tick();

    expect(mockService.delete).toHaveBeenCalled();
  }));

  // =============================
  // SETTERS
  // =============================
  it('should set server cloud', () => {
    component.setServerCloud({ name: 'c' } as any, 3);

    expect(component.cloud.idFront).toBe(3);
  });

  it('should reset cloud', () => {
    component.resetCloudServices();

    expect(component.cloud).toEqual({} as any);
  });

  // =============================
  // MAPPERS
  // =============================
  it('should map to InVirtualEquipmentRest', () => {
    const result = component.toInVirtualEquipmentRest({
      id: 1,
      digitalServiceUid: 'ds',
      name: 'c',
      quantity: 1,
      cloudProvider: 'aws',
      instanceType: 't2',
      location: { code: 'FR' },
      annualUsage: 10,
      averageWorkload: 50
    } as any);

    expect(result.provider).toBe('aws');
    expect(result.workload).toBe(0.5);
  });

  it('should map to config', () => {
    const result = component.toDigitalServiceCloudServiceConfig(
      {
        id: 1,
        digitalServiceUid: 'ds',
        name: 'c',
        quantity: 1,
        provider: 'aws',
        instanceType: 't2',
        location: 'FR',
        durationHour: 10,
        workload: 0.5
      } as any,
      { FR: 'France' }
    );

    expect(result.locationValue).toBe('France');
    expect(result.averageWorkload).toBe(50);
  });
});
