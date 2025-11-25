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

describe("DigitalServicesFootprintHeaderComponent", () => {
    let component: DigitalServicesFootprintHeaderComponent;
    let fixture: ComponentFixture<DigitalServicesFootprintHeaderComponent>;

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
            ],
        });
        fixture = TestBed.createComponent(DigitalServicesFootprintHeaderComponent);
        component = fixture.componentInstance;
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
});
