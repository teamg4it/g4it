/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute, convertToParamMap, Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { InplaceModule } from "primeng/inplace";
import { InputTextModule } from "primeng/inputtext";
import { of } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { DigitalServicesFootprintHeaderComponent } from "./digital-services-footprint-header.component";

fdescribe("DigitalServicesFootprintHeaderComponent", () => {
    let component: DigitalServicesFootprintHeaderComponent;
    let fixture: ComponentFixture<DigitalServicesFootprintHeaderComponent>;
    let router: Router;

    //mock data service
    const digitalServiceDataMock = {
        digitalService$: of({
            name: "Test Digital Service",
            uid: "test-uid",
            creationDate: Date.now(),
            lastUpdateDate: Date.now(),
            lastCalculationDate: null,
            terminals: [],
            servers: [],
            enableDataInconsistency: false,
            networks: [],
            activeDsvUid: "1",
        } as DigitalService),
        copyUrl: () => of({ url: "test", expiryDate: new Date() }),
        get: () => of({} as DigitalService),
        delete: () => of({}),
        getDuplicateDigitalServiceAndVersionName: () =>
            of({ dsNames: [], versionNames: [] }),
    };

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [DigitalServicesFootprintHeaderComponent],
            imports: [
                HttpClientTestingModule,
                RouterTestingModule,
                InplaceModule,
                FormsModule,
                InputTextModule,
                SharedModule,
                ConfirmPopupModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                {
                    provide: DigitalServicesDataService,
                    useValue: digitalServiceDataMock,
                },
                TranslatePipe,
                TranslateService,
                UserService,
                MessageService,
                ConfirmationService,
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            paramMap: convertToParamMap({
                                digitalServiceVersionId: "12345",
                            }),
                        },
                        paramMap: of(
                            convertToParamMap({
                                digitalServiceVersionId: "12345",
                            }),
                        ),
                    },
                },
            ],
        });
        fixture = TestBed.createComponent(DigitalServicesFootprintHeaderComponent);
        component = fixture.componentInstance;

        router = TestBed.inject(Router);

        // Mock router.url
        Object.defineProperty(router, "url", {
            writable: true,
            value: "/organizations/test-org/workspaces/test-workspace/digital-service-version/12345/footprint/resources",
        });

        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should emit event on digital service name change", () => {
        const newDigitalServiceName = "New name";
        spyOn(component.digitalServiceChange, "emit");
        component.onNameUpdate(newDigitalServiceName, true);
        fixture.detectChanges();
        const emittedObject = { name: newDigitalServiceName };
        expect(component.digitalServiceChange.emit).toHaveBeenCalledWith(
            jasmine.objectContaining(emittedObject),
        );
    });

    it("ngOnInit should subscribe to digitalService$ and set digitalService and call setDigitalService", () => {
        const digitalServiceStoreSpy = spyOn(
            component.digitalServiceStore,
            "setDigitalService",
        );
        const testDigitalService = {
            name: "Test Digital Service",
            uid: "test-uid",
            creationDate: Date.now(),
            lastUpdateDate: Date.now(),
            lastCalculationDate: null,
            terminals: [],
            servers: [],
            networks: [],
            enableDataInconsistency: false,
            activeDsvUid: "1",
        } as DigitalService;

        // Patch the observable to emit a new value
        (component as any).digitalServicesData.digitalService$ = of(testDigitalService);

        component.ngOnInit();

        expect(component.digitalService).toEqual(testDigitalService);
        expect(digitalServiceStoreSpy).toHaveBeenCalledWith(testDigitalService);
    });

    it("ngOnInit should subscribe to currentOrganization$ and set selectedOrganizationName, organization, and isEcoMindEnabledForCurrentOrganization", () => {
        const testOrganization = { name: "SubName", ecomindai: true };
        (component as any).userService.currentOrganization$ = of(testOrganization);

        component.ngOnInit();

        expect(component.selectedOrganizationName).toBe("SubName");
        expect(component.isEcoMindEnabledForCurrentOrganization).toBeTrue();
    });

    it("ngOnInit should subscribe to currentWorkspace$ and set selectedOrganizationName", () => {
        const testOrganization = { name: "OrgName" };
        (component as any).userService.currentWorkspace$ = of(testOrganization);

        component.ngOnInit();

        expect(component.selectedWorkspaceName).toBe("OrgName");
    });

    it("should toggle displayLinkCreatePopup (open)", () => {
        expect(component.displayLinkCreatePopup).toBeFalse();
        component.shareDs();
        expect(component.displayLinkCreatePopup).toBeTrue();
    });

    it("should set digitalServiceVersionUid from route paramMap", () => {
        component.ngOnInit();
        expect(component.digitalServiceVersionUid).toBe("12345");
    });

    it("should navigate to digital-services page when changePageToDigitalServices is called", () => {
        Object.defineProperty(router, "url", {
            writable: true,
            value: "/organizations/test-org/workspaces/test-workspace/digital-service-version/12345/footprint/resources",
        });

        const result = component.changePageToDigitalServices();
        expect(result).toBe(
            "/organizations/test-org/workspaces/test-workspace/digital-services",
        );
    });

    it("should navigate to eco-mind-ai page when changePageToDigitalServices is called for AI service", () => {
        Object.defineProperty(router, "url", {
            writable: true,
            value: "/organizations/test-org/workspaces/test-workspace/eco-mind-ai/12345/footprint/resources",
        });

        const result = component.changePageToDigitalServices();
        expect(result).toBe(
            "/organizations/test-org/workspaces/test-workspace/eco-mind-ai",
        );
    });

    describe("validateDataDS", () => {
        it("should set disableDs to true when value is in duplicateDsNames excluding saved dsName", () => {
            component.duplicateDsNames = ["ds1", "ds2"];
            component.savedDigitalServiceAndVersion = { dsName: "ds1", version: "v1" };
            component.validateDataDS("ds2");
            expect(component.disableDs).toBeTrue();
        });

        it("should set disableDs to false when value matches saved dsName", () => {
            component.duplicateDsNames = ["ds1", "ds2"];
            component.savedDigitalServiceAndVersion = { dsName: "ds1", version: "v1" };
            component.validateDataDS("ds1");
            expect(component.disableDs).toBeFalse();
        });

        it("should set disableDs to false when value is not in duplicateDsNames", () => {
            component.duplicateDsNames = ["ds1", "ds2"];
            component.savedDigitalServiceAndVersion = { dsName: "ds1", version: "v1" };
            component.validateDataDS("ds3");
            expect(component.disableDs).toBeFalse();
        });

        it("should handle trimming correctly", () => {
            component.duplicateDsNames = [" ds1 ", "ds2"];
            component.savedDigitalServiceAndVersion = { dsName: "ds1", version: "v1" };
            component.validateDataDS(" ds2 ");
            expect(component.disableDs).toBeTrue();
        });

        it("should handle empty duplicateDsNames", () => {
            component.duplicateDsNames = [];
            component.savedDigitalServiceAndVersion = { dsName: "ds1", version: "v1" };
            component.validateDataDS("ds2");
            expect(component.disableDs).toBeFalse();
        });
    });

    describe("validateDataVersion", () => {
        it("should set disableVersion to true when value is in duplicateVersionNames excluding saved version", () => {
            component.duplicateVersionNames = ["v1", "v2"];
            component.savedDigitalServiceAndVersion = { dsName: "ds1", version: "v1" };
            component.validateDataVersion("v2");
            expect(component.disableVersion).toBeTrue();
        });

        it("should set disableVersion to false when value matches saved version", () => {
            component.duplicateVersionNames = ["v1", "v2"];
            component.savedDigitalServiceAndVersion = { dsName: "ds1", version: "v1" };
            component.validateDataVersion("v1");
            expect(component.disableVersion).toBeFalse();
        });

        it("should set disableVersion to false when value is not in duplicateVersionNames", () => {
            component.duplicateVersionNames = ["v1", "v2"];
            component.savedDigitalServiceAndVersion = { dsName: "ds1", version: "v1" };
            component.validateDataVersion("v3");
            expect(component.disableVersion).toBeFalse();
        });

        it("should handle trimming correctly", () => {
            component.duplicateVersionNames = [" v1 ", "v2"];
            component.savedDigitalServiceAndVersion = { dsName: "ds1", version: "v1" };
            component.validateDataVersion(" v2 ");
            expect(component.disableVersion).toBeTrue();
        });

        it("should handle empty duplicateVersionNames", () => {
            component.duplicateVersionNames = [];
            component.savedDigitalServiceAndVersion = { dsName: "ds1", version: "v1" };
            component.validateDataVersion("v2");
            expect(component.disableVersion).toBeFalse();
        });
    });

    describe("validateDs", () => {
        it("should call callDSVersion with value and true when firstDsVersionCall is true", () => {
            component.firstDsVersionCall = true;
            spyOn(component, "callDSVersion");
            component.validateDs("testValue");
            expect(component.callDSVersion).toHaveBeenCalledWith("testValue", true);
        });

        it("should call validateDataDS with value when firstDsVersionCall is false", () => {
            component.firstDsVersionCall = false;
            spyOn(component, "validateDataDS");
            component.validateDs("testValue");
            expect(component.validateDataDS).toHaveBeenCalledWith("testValue");
        });
    });

    describe("validateVersion", () => {
        it("should call callDSVersion with value and false when firstDsVersionCall is true", () => {
            component.firstDsVersionCall = true;
            spyOn(component, "callDSVersion");
            component.validateVersion("testValue");
            expect(component.callDSVersion).toHaveBeenCalledWith("testValue", false);
        });

        it("should call validateDataVersion with value when firstDsVersionCall is false", () => {
            component.firstDsVersionCall = false;
            spyOn(component, "validateDataVersion");
            component.validateVersion("testValue");
            expect(component.validateDataVersion).toHaveBeenCalledWith("testValue");
        });
    });
});
