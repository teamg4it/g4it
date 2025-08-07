import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DigitalServicesFootprintComponent } from './digital-services-footprint.component';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';
import { DigitalServicesDataService } from 'src/app/core/service/data/digital-services-data.service';
import { DigitalServiceStoreService } from 'src/app/core/store/digital-service.store';
import { DigitalServiceBusinessService } from 'src/app/core/service/business/digital-services.service';
import { GlobalStoreService } from 'src/app/core/store/global.store';
import { InDatacentersService } from 'src/app/core/service/data/in-out/in-datacenters.service';

describe('DigitalServicesFootprintComponent', () => {
  let component: DigitalServicesFootprintComponent;
  let fixture: ComponentFixture<DigitalServicesFootprintComponent>;

  const mockRoute = {
    snapshot: {
      paramMap: {
        get: (key: string) => 'test-uid',
      },
    },
  };

  const mockDigitalService = {
    uid: 'test-uid',
    isAi: false,
    lastCalculationDate: new Date(),
  };

  const mockDigitalServicesDataService = {
    get: () => of(mockDigitalService),
    getNetworkReferential: () => of([]),
    getDeviceReferential: () => of([]),
    getHostServerReferential: () => of([]),
    update: () => of(mockDigitalService),
  };

  const mockDigitalServiceStoreService = {
    setDigitalService: jasmine.createSpy(),
    initInPhysicalEquipments: jasmine.createSpy(),
    initInVirtualEquipments: jasmine.createSpy(),
    setInDatacenters: jasmine.createSpy(),
    setNetworkTypes: jasmine.createSpy(),
    setTerminalDeviceTypes: jasmine.createSpy(),
    setServerTypes: jasmine.createSpy(),
  };

  const mockDigitalServiceBusinessService = {
    initCountryMap: jasmine.createSpy(),
  };

  const mockGlobal = {
    setLoading: jasmine.createSpy(),
  };

  const mockInDatacentersService = {
    get: () => of([]),
    create: () => of({}),
  };

  const mockTranslate = {
    instant: (key: string) => key,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DigitalServicesFootprintComponent],
      providers: [
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: DigitalServicesDataService, useValue: mockDigitalServicesDataService },
        { provide: DigitalServiceStoreService, useValue: mockDigitalServiceStoreService },
        { provide: DigitalServiceBusinessService, useValue: mockDigitalServiceBusinessService },
        { provide: GlobalStoreService, useValue: mockGlobal },
        { provide: InDatacentersService, useValue: mockInDatacentersService },
        { provide: TranslateService, useValue: mockTranslate },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DigitalServicesFootprintComponent);
    component = fixture.componentInstance;
  });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    // it('should load digital service and initialize data on init', async () => {
    //     await component.ngOnInit();
    //     expect(component.digitalService.uid).toBe('test-uid');
    //     expect(mockDigitalServiceStoreService.setDigitalService).toHaveBeenCalled();
    //     expect(mockDigitalServiceStoreService.initInPhysicalEquipments).toHaveBeenCalledWith('test-uid');
    //     expect(mockDigitalServiceStoreService.initInVirtualEquipments).toHaveBeenCalledWith('test-uid');
    //     expect(mockDigitalServiceBusinessService.initCountryMap).toHaveBeenCalled();
    //     expect(mockGlobal.setLoading).toHaveBeenCalledTimes(2); // true & false
    // });



  });


