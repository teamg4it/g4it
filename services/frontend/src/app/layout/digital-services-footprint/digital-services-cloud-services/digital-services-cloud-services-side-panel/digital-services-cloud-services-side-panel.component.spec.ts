/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ReactiveFormsModule } from "@angular/forms";
import { Router } from "@angular/router";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { SelectModule } from "primeng/select";
import { of } from "rxjs";
import { DigitalServiceCloudServiceConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { DigitalServicesCloudServicesSidePanelComponent } from "./digital-services-cloud-services-side-panel.component";

describe("DigitalServicesCloudServicesSidePanelComponent", () => {
    let component: DigitalServicesCloudServicesSidePanelComponent;
    let fixture: ComponentFixture<DigitalServicesCloudServicesSidePanelComponent>;
    let digitalServicesDataService: jasmine.SpyObj<DigitalServicesDataService>;
    let digitalServiceBusinessService: jasmine.SpyObj<DigitalServiceBusinessService>;
    let mockRouter: jasmine.SpyObj<Router>;
    let mockDigitalServiceStore: any;

    beforeEach(async () => {
        const digitalServicesDataServiceSpy = jasmine.createSpyObj(
            "DigitalServicesDataService",
            ["getBoaviztapiCloudProviders", "getBoaviztapiInstanceTypes"],
        );
        const digitalServiceBusinessServiceSpy = jasmine.createSpyObj(
            "DigitalServiceBusinessService",
            ["getNextAvailableName"],
        );
        const routerSpy = jasmine.createSpyObj("Router", [], {
            url: "/organizations/org1/workspaces/ws1/digital-services/ds1/footprint/123",
        });

        mockDigitalServiceStore = {
            countryMap: jasmine.createSpy("countryMap").and.returnValue({
                FR: "France",
                EEE: "Europe",
                USA: "United States",
            }),
        };

        digitalServicesDataServiceSpy.getBoaviztapiCloudProviders.and.returnValue(
            of(["aws", "azure", "gcp"]),
        );
        digitalServicesDataServiceSpy.getBoaviztapiInstanceTypes.and.returnValue(
            of(["instance-1", "instance-2", "instance-3"]),
        );
        digitalServiceBusinessServiceSpy.getNextAvailableName.and.returnValue(
            "Cloud Service 1",
        );

        await TestBed.configureTestingModule({
            imports: [
                ReactiveFormsModule,
                ButtonModule,
                SelectModule,
                InputNumberModule,
                InputTextModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
                DigitalServicesCloudServicesSidePanelComponent,
            ],
            providers: [
                {
                    provide: DigitalServicesDataService,
                    useValue: digitalServicesDataServiceSpy,
                },
                {
                    provide: DigitalServiceBusinessService,
                    useValue: digitalServiceBusinessServiceSpy,
                },
                {
                    provide: Router,
                    useValue: routerSpy,
                },
                {
                    provide: DigitalServiceStoreService,
                    useValue: mockDigitalServiceStore,
                },
                TranslatePipe,
                TranslateService,
                MessageService,
                UserService,
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesCloudServicesSidePanelComponent);
        component = fixture.componentInstance;
        digitalServicesDataService = TestBed.inject(
            DigitalServicesDataService,
        ) as jasmine.SpyObj<DigitalServicesDataService>;
        digitalServiceBusinessService = TestBed.inject(
            DigitalServiceBusinessService,
        ) as jasmine.SpyObj<DigitalServiceBusinessService>;
        mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;

        component.cloud = {
            id: 1,
            idFront: 1,
            digitalServiceUid: "123",
            name: "Test Cloud Service",
            quantity: 2,
            cloudProvider: "aws",
            instanceType: "instance-1",
            location: { code: "EEE", name: "Europe" },
            locationValue: "Europe",
            annualUsage: 8760,
            averageWorkload: 50,
        } as DigitalServiceCloudServiceConfig;
        component.cloudServices = [];
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should close sidebar", () => {
        spyOn(component.sidebarVisibleChange, "emit");
        component.close();
        expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
    });

    it("should delete cloud service", () => {
        spyOn(component.deleteCloudServices, "emit");
        spyOn(component.sidebarVisibleChange, "emit");
        component.deleteServerCloud();
        expect(component.deleteCloudServices.emit).toHaveBeenCalledWith(component.cloud);
        expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
    });

    it("should submit form data", () => {
        spyOn(component.updateCloudServices, "emit");
        spyOn(component.sidebarVisibleChange, "emit");

        component.cloudForm.controls["name"].setValue("Updated Cloud Service");
        component.cloudForm.controls["cloudProvider"].setValue("azure");
        component.cloudForm.controls["instanceType"].setValue("instance-2");
        component.cloudForm.controls["location"].setValue({
            code: "FR",
            name: "France",
        });
        component.cloudForm.controls["quantity"].setValue("5");
        component.cloudForm.controls["averageWorkload"].setValue("75");
        component.cloudForm.controls["annualUsage"].setValue("7000");

        component.submitFormData();

        expect(component.updateCloudServices.emit).toHaveBeenCalledWith(component.cloud);
        expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
    });

    it("should initialize form with default values and validators", () => {
        component.cloudServices = [
            { name: "Cloud Service 1" } as DigitalServiceCloudServiceConfig,
            { name: "Cloud Service 2" } as DigitalServiceCloudServiceConfig,
        ];
        component.isNew = true;
        component.initForm();

        expect(component.existingNames).toEqual(["Cloud Service 1", "Cloud Service 2"]);
        expect(component.cloudForm.controls["name"].value).toBe("");
        expect(component.cloudForm.controls["name"].validator).toBeTruthy();
        expect(component.cloudForm.controls["cloudProvider"].value).toBe("");
        expect(component.cloudForm.controls["cloudProvider"].validator).toBeTruthy();
        expect(component.cloudForm.controls["instanceType"].value).toBe("");
        expect(component.cloudForm.controls["instanceType"].validator).toBeTruthy();
        expect(component.cloudForm.controls["location"].value).toBe("");
        expect(component.cloudForm.controls["location"].validator).toBeTruthy();
        expect(component.cloudForm.controls["quantity"].value).toBe("0");
        expect(component.cloudForm.controls["quantity"].validator).toBeTruthy();
        expect(component.cloudForm.controls["averageWorkload"].value).toBe("0");
        expect(component.cloudForm.controls["averageWorkload"].validator).toBeTruthy();
        expect(component.cloudForm.controls["annualUsage"].value).toBe("0");
        expect(component.cloudForm.controls["annualUsage"].validator).toBeTruthy();
        expect(component.cloudForm.get("name")?.dirty).toBeTrue();
    });

    it("should exclude current cloud service name from existing names when not new", () => {
        component.cloudServices = [
            { name: "Cloud Service 1" } as DigitalServiceCloudServiceConfig,
            { name: "Cloud Service 2" } as DigitalServiceCloudServiceConfig,
        ];
        component.isNew = false;
        component.cloud = { name: "Cloud Service 1" } as DigitalServiceCloudServiceConfig;
        component.initForm();

        expect(component.existingNames).toEqual(["Cloud Service 2"]);
    });

    it("should load Boavizta referentials on init", async () => {
        await component.getBoaviztaReferentials();

        expect(component.countries.length).toBeGreaterThan(0);
        expect(component.countries).toContain(
            jasmine.objectContaining({ code: "FR", name: "France" }),
        );
        expect(component.cloudProviders).toEqual(["aws", "azure", "gcp"]);
        expect(digitalServicesDataService.getBoaviztapiCloudProviders).toHaveBeenCalled();
        expect(
            digitalServicesDataService.getBoaviztapiInstanceTypes,
        ).toHaveBeenCalledWith("aws");
        expect(
            digitalServicesDataService.getBoaviztapiInstanceTypes,
        ).toHaveBeenCalledWith("azure");
        expect(
            digitalServicesDataService.getBoaviztapiInstanceTypes,
        ).toHaveBeenCalledWith("gcp");
    });

    it("should set default cloud provider when cloud has no idFront", async () => {
        component.cloud = { idFront: undefined } as any;
        await component.getBoaviztaReferentials();

        expect(component.cloud.cloudProvider).toBe("aws");
    });

    it("should populate instanceTypesByProvider map", async () => {
        await component.getBoaviztaReferentials();

        expect(component.instanceTypesByProvider.size).toBe(3);
        expect(component.instanceTypesByProvider.get("aws")).toEqual([
            "instance-1",
            "instance-2",
            "instance-3",
        ]);
        expect(component.instanceTypesByProvider.get("azure")).toEqual([
            "instance-1",
            "instance-2",
            "instance-3",
        ]);
        expect(component.instanceTypesByProvider.get("gcp")).toEqual([
            "instance-1",
            "instance-2",
            "instance-3",
        ]);
    });

    it("should sort countries alphabetically by name", async () => {
        await component.getBoaviztaReferentials();

        expect(component.countries[0].name).toBe("Europe");
        expect(component.countries[1].name).toBe("France");
        expect(component.countries[2].name).toBe("United States");
    });

    it("should reset cloud service with default values", async () => {
        component.existingNames = ["Cloud Service 1", "Cloud Service 2"];
        await component.resetCloudServices();

        expect(component.cloud.name).toBe("Cloud Service 1");
        expect(component.cloud.quantity).toBe(1);
        expect(component.cloud.cloudProvider).toBe("");
        expect(component.cloud.instanceType).toBe("");
        expect(component.cloud.location).toEqual({ code: "EEE", name: "Europe" });
        expect(component.cloud.locationValue).toBe("Europe");
        expect(component.cloud.annualUsage).toBe(8760);
        expect(component.cloud.averageWorkload).toBe(50);
        expect(component.cloud.digitalServiceUid).toBe("ds1");
        expect(digitalServiceBusinessService.getNextAvailableName).toHaveBeenCalledWith(
            component.existingNames,
            "Cloud Service",
            false,
        );
    });

    it("should set isNew to true when cloud has no idFront", () => {
        component.cloud = { idFront: undefined } as any;
        component.ngOnInit();
        expect(component.isNew).toBeTrue();
    });

    it("should set isNew to false when cloud has idFront", () => {
        component.cloud = { idFront: 1 } as any;
        component.ngOnInit();
        expect(component.isNew).toBeFalse();
    });

    it("should call resetCloudServices when cloud has no idFront on init", async () => {
        component.cloud = { idFront: undefined } as any;
        spyOn(component, "resetCloudServices");
        await component.ngOnInit();
        expect(component.resetCloudServices).toHaveBeenCalled();
    });

    it("should not call resetCloudServices when cloud has idFront on init", async () => {
        component.cloud = { idFront: 1 } as any;
        spyOn(component, "resetCloudServices");
        await component.ngOnInit();
        expect(component.resetCloudServices).not.toHaveBeenCalled();
    });
});
