import { ComponentFixture, TestBed } from "@angular/core/testing";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { of } from "rxjs";
import { DigitalServicesFootprintFooterComponent } from "./digital-services-footprint-footer.component";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesAiDataService } from "src/app/core/service/data/digital-services-ai-data.service";
import { AIFormsStore } from "src/app/core/store/ai-forms.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Router } from "@angular/router";

describe("DigitalServicesFootprintFooterComponent", () => {
    let component: DigitalServicesFootprintFooterComponent;
    let fixture: ComponentFixture<DigitalServicesFootprintFooterComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DigitalServicesFootprintFooterComponent],
            imports: [
                HttpClientTestingModule,
                RouterTestingModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                MessageService,
                {
                    provide: DigitalServicesDataService,
                    useValue: {
                        digitalService$: of({ uid: "test-uid" }),
                        launchEvaluating: () => of(true),
                        get: () => of({ uid: "test-uid" }),
                    },
                },
                {
                    provide: DigitalServiceBusinessService,
                    useValue: { launchCalcul$: of(true) },
                },
                {
                    provide: DigitalServiceStoreService,
                    useValue: {
                        setDigitalService: jasmine.createSpy(),
                        setEnableCalcul: jasmine.createSpy(),
                        enableCalcul: () => true,
                        digitalService: () => ({
                            lastCalculationDate: null,
                            lastUpdateDate: Date.now(),
                        }),
                        inPhysicalEquipments: () => [],
                        inVirtualEquipments: () => [],
                    },
                },
                { provide: UserService, useValue: {} },
                { provide: DigitalServicesAiDataService, useValue: {} },
                {
                    provide: AIFormsStore,
                    useValue: {
                        getInfrastructureFormData: () => ({}),
                        getParametersFormData: () => ({}),
                    },
                },
                {
                    provide: GlobalStoreService,
                    useValue: { setLoading: jasmine.createSpy() },
                },
                {
                    provide: TranslateService,
                    useValue: { instant: (key: string) => key },
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesFootprintFooterComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should subscribe to digitalService$ and set digitalService on ngOnInit", () => {
        component.ngOnInit();
        expect(component.digitalService.uid).toBe("test-uid");
    });

    it("should subscribe to launchCalcul$ and call launchCalcul on ngOnInit", () => {
        spyOn(component, "launchCalcul");
        component.ngOnInit();
        expect(component.launchCalcul).toHaveBeenCalled();
    });
});
