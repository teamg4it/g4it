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
import { DigitalServiceAIServiceConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { DigitalServicesAIServicesSidePanelComponent } from "./digital-services-ai-services-side-panel.component";

describe("DigitalServicesAIServicesSidePanelComponent", () => {
    let component: DigitalServicesAIServicesSidePanelComponent;
    let fixture: ComponentFixture<DigitalServicesAIServicesSidePanelComponent>;
    let digitalServicesDataService: jasmine.SpyObj<DigitalServicesDataService>;
    let digitalServiceBusinessService: jasmine.SpyObj<DigitalServiceBusinessService>;
    let mockRouter: jasmine.SpyObj<Router>;
    let mockDigitalServiceStore: any;

    beforeEach(async () => {
        const digitalServicesDataServiceSpy = jasmine.createSpyObj(
            "DigitalServicesDataService",
            ["getBoaviztapiAIProviders", "getBoaviztapiAIModels"],
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

        digitalServicesDataServiceSpy.getBoaviztapiAIProviders.and.returnValue(
            of(["aws", "azure", "gcp"]),
        );
        digitalServicesDataServiceSpy.getBoaviztapiInstanceTypes.and.returnValue(
            of(["instance-1", "instance-2", "instance-3"]),
        );
        digitalServiceBusinessServiceSpy.getNextAvailableName.and.returnValue(
            "AI Service 1",
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
                DigitalServicesAIServicesSidePanelComponent,
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

        fixture = TestBed.createComponent(DigitalServicesAIServicesSidePanelComponent);
        component = fixture.componentInstance;
        digitalServicesDataService = TestBed.inject(
            DigitalServicesDataService,
        ) as jasmine.SpyObj<DigitalServicesDataService>;
        digitalServiceBusinessService = TestBed.inject(
            DigitalServiceBusinessService,
        ) as jasmine.SpyObj<DigitalServiceBusinessService>;
        mockRouter = TestBed.inject(Router) as jasmine.SpyObj<Router>;

        component.ai = {
            id: 1,
            idFront: 1,
            digitalServiceUid: "123",
            name: "Test AI Service",
            tokenQuantity: 2,
            aiProvider: "aws",
            aiModel: "instance-1",
            location: { code: "EEE", name: "Europe" },
            locationValue: "Europe",
        } as DigitalServiceAIServiceConfig;
        component.aiServices = [];
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

    it("should delete AI service", () => {
        spyOn(component.deleteAIServices, "emit");
        spyOn(component.sidebarVisibleChange, "emit");
        component.deleteServerAI();
        expect(component.deleteAIServices.emit).toHaveBeenCalledWith(component.ai);
        expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
    });

    it("should submit form data", () => {
        spyOn(component.updateAIServices, "emit");
        spyOn(component.sidebarVisibleChange, "emit");

        component.aiForm.controls["name"].setValue("Updated AI Service");
        component.aiForm.controls["aiProvider"].setValue("azure");
        component.aiForm.controls["instanceType"].setValue("instance-2");
        component.aiForm.controls["location"].setValue({
            code: "FR",
            name: "France",
        });
        component.aiForm.controls["quantity"].setValue("5");
        component.aiForm.controls["averageWorkload"].setValue("75");
        component.aiForm.controls["annualUsage"].setValue("7000");

        component.submitFormData();

        expect(component.updateAIServices.emit).toHaveBeenCalledWith(component.ai);
        expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
    });

    it("should initialize form with default values and validators", () => {
        component.aiServices = [
            { name: "AI Service 1" } as DigitalServiceAIServiceConfig,
            { name: "AI Service 2" } as DigitalServiceAIServiceConfig,
        ];
        component.isNew = true;
        component.initForm();

        expect(component.existingNames).toEqual(["AI Service 1", "AI Service 2"]);
        expect(component.aiForm.controls["name"].value).toBe("");
        expect(component.aiForm.controls["name"].validator).toBeTruthy();
        expect(component.aiForm.controls["aiProvider"].value).toBe("");
        expect(component.aiForm.controls["aiProvider"].validator).toBeTruthy();
        expect(component.aiForm.controls["aiModel"].value).toBe("");
        expect(component.aiForm.controls["aiModel"].validator).toBeTruthy();
        expect(component.aiForm.controls["location"].value).toBe("");
        expect(component.aiForm.controls["location"].validator).toBeTruthy();
        expect(component.aiForm.controls["tokenQuantity"].value).toBe("0");
        expect(component.aiForm.controls["tokenQuantity"].validator).toBeTruthy();
        expect(component.aiForm.get("name")?.dirty).toBeTrue();
    });

    it("should exclude current AI service name from existing names when not new", () => {
        component.aiServices = [
            { name: "AI Service 1" } as DigitalServiceAIServiceConfig,
            { name: "AI Service 2" } as DigitalServiceAIServiceConfig,
        ];
        component.isNew = false;
        component.ai = { name: "AI Service 1" } as DigitalServiceAIServiceConfig;
        component.initForm();

        expect(component.existingNames).toEqual(["AI Service 2"]);
    });

    it("should load Boavizta referentials on init", async () => {
        await component.getBoaviztaReferentials();

        expect(component.countries.length).toBeGreaterThan(0);
        expect(component.countries).toContain(
            jasmine.objectContaining({ code: "FR", name: "France" }),
        );
        expect(component.aiProviders).toEqual(["aws", "azure", "gcp"]);
        expect(digitalServicesDataService.getBoaviztapiAIProviders).toHaveBeenCalled();
        expect(digitalServicesDataService.getBoaviztapiAIModel).toHaveBeenCalledWith(
            "aws",
        );
        expect(digitalServicesDataService.getBoaviztapiAIModel).toHaveBeenCalledWith(
            "azure",
        );
        expect(digitalServicesDataService.getBoaviztapiAIModel).toHaveBeenCalledWith(
            "gcp",
        );
    });

    it("should set default ai provider when ai has no idFront", async () => {
        component.ai = { idFront: undefined } as any;
        await component.getBoaviztaReferentials();

        expect(component.ai.aiProvider).toBe("aws");
    });

    it("should populate aiModelsByProvider map", async () => {
        await component.getBoaviztaReferentials();

        expect(component.aiModelsByProvider.size).toBe(3);
        expect(component.aiModelsByProvider.get("aws")).toEqual([
            "instance-1",
            "instance-2",
            "instance-3",
        ]);
        expect(component.aiModelsByProvider.get("azure")).toEqual([
            "instance-1",
            "instance-2",
            "instance-3",
        ]);
        expect(component.aiModelsByProvider.get("gcp")).toEqual([
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

    it("should reset AI service with default values", async () => {
        component.existingNames = ["AI Service 1", "AI Service 2"];
        await component.resetAIServices();

        expect(component.ai.name).toBe("AI Service 1");
        expect(component.ai.tokenQuantity).toBe(1);
        expect(component.ai.aiProvider).toBe("");
        expect(component.ai.aiModel).toBe("");
        expect(component.ai.location).toEqual({ code: "EEE", name: "Europe" });
        expect(component.ai.locationValue).toBe("Europe");
        expect(component.ai.digitalServiceUid).toBe("ds1");
        expect(digitalServiceBusinessService.getNextAvailableName).toHaveBeenCalledWith(
            component.existingNames,
            "AI Service",
            false,
        );
    });

    it("should set isNew to true when AI has no idFront", () => {
        component.ai = { idFront: undefined } as any;
        component.ngOnInit();
        expect(component.isNew).toBeTrue();
    });

    it("should set isNew to false when AI has idFront", () => {
        component.ai = { idFront: 1 } as any;
        component.ngOnInit();
        expect(component.isNew).toBeFalse();
    });

    it("should call resetAIServices when AI has no idFront on init", async () => {
        component.ai = { idFront: undefined } as any;
        spyOn(component, "resetAIServices");
        await component.ngOnInit();
        expect(component.resetAIServices).toHaveBeenCalled();
    });

    it("should not call resetAIServices when AI has idFront on init", async () => {
        component.ai = { idFront: 1 } as any;
        spyOn(component, "resetAIServices");
        await component.ngOnInit();
        expect(component.resetAIServices).not.toHaveBeenCalled();
    });
});
