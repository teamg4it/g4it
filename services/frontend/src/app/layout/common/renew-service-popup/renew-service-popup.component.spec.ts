import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule } from "@ngx-translate/core";
import { ClipboardService } from "ngx-clipboard";
import { of, throwError } from "rxjs";
import {
    RenewServiceResp,
    RenewServiceUpdateResp,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { InventoryDataService } from "src/app/core/service/data/inventory-data.service";
import { RenewServicePopupComponent } from "./renew-service-popup.component";

describe("RenewServicePopupComponent", () => {
    let component: RenewServicePopupComponent;
    let fixture: ComponentFixture<RenewServicePopupComponent>;
    let mockDigitalServicesDataService: jasmine.SpyObj<DigitalServicesDataService>;
    let mockInventoryDataService: jasmine.SpyObj<InventoryDataService>;
    const mockClipboard = {
        copy: jasmine.createSpy("copy"),
    };

    const mockRenewServiceResp: RenewServiceResp = {
        serviceId: "123",
        retentionDays: 30,
        expiryDate: "1",
        serviceName: "Test Service",
    };

    const mockRenewServiceUpdateResp: RenewServiceUpdateResp = {
        isRenewed: true,
        serviceId: "123",
        responseMessage: "Service renewed successfully",
    };

    beforeEach(async () => {
        mockDigitalServicesDataService = jasmine.createSpyObj(
            "DigitalServicesDataService",
            ["getServiceRenewalDetails", "renewService"],
        );
        mockInventoryDataService = jasmine.createSpyObj("InventoryDataService", [
            "getServiceRenewalDetails",
            "renewService",
        ]);

        await TestBed.configureTestingModule({
            imports: [
                RenewServicePopupComponent,
                TranslateModule.forRoot(),
                HttpClientTestingModule,
            ],
            providers: [
                { provide: ClipboardService, useValue: mockClipboard },
                {
                    provide: DigitalServicesDataService,
                    useValue: mockDigitalServicesDataService,
                },
                { provide: InventoryDataService, useValue: mockInventoryDataService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(RenewServicePopupComponent);
        component = fixture.componentInstance;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize with isRenewButtonDisabled as false", () => {
        expect(component.isRenewButtonDisabled).toBe(false);
    });

    it("should not call service when serviceId is empty on ngOnInit", () => {
        fixture.componentRef.setInput("serviceId", "");
        fixture.detectChanges();

        expect(
            mockDigitalServicesDataService.getServiceRenewalDetails,
        ).not.toHaveBeenCalled();
        expect(mockInventoryDataService.getServiceRenewalDetails).not.toHaveBeenCalled();
    });

    it("should call digitalServicesData service when isInventory is false on ngOnInit", () => {
        mockDigitalServicesDataService.getServiceRenewalDetails.and.returnValue(
            of(mockRenewServiceResp),
        );

        fixture.componentRef.setInput("serviceId", "123");
        fixture.componentRef.setInput("isInventory", false);
        fixture.detectChanges();

        expect(
            mockDigitalServicesDataService.getServiceRenewalDetails,
        ).toHaveBeenCalledWith("123");
        expect(component.renewServiceParams).toEqual(mockRenewServiceResp);
    });

    it("should call inventoryDataService when isInventory is true on ngOnInit", () => {
        mockInventoryDataService.getServiceRenewalDetails.and.returnValue(
            of(mockRenewServiceResp),
        );

        fixture.componentRef.setInput("serviceId", "456");
        fixture.componentRef.setInput("isInventory", true);
        fixture.detectChanges();

        expect(mockInventoryDataService.getServiceRenewalDetails).toHaveBeenCalledWith(
            "456",
        );
        expect(component.renewServiceParams).toEqual(mockRenewServiceResp);
    });

    it("should not call renewService when isRenewButtonDisabled is true", () => {
        component.isRenewButtonDisabled = true;
        component.renewService();

        expect(mockDigitalServicesDataService.renewService).not.toHaveBeenCalled();
        expect(mockInventoryDataService.renewService).not.toHaveBeenCalled();
    });

    it("should not call renewService when serviceId is empty", () => {
        component.isRenewButtonDisabled = false;
        fixture.componentRef.setInput("serviceId", "");
        component.renewServiceParams = mockRenewServiceResp;

        component.renewService();

        expect(mockDigitalServicesDataService.renewService).not.toHaveBeenCalled();
        expect(mockInventoryDataService.renewService).not.toHaveBeenCalled();
    });

    it("should not call renewService when renewServiceParams is null", () => {
        component.isRenewButtonDisabled = false;
        fixture.componentRef.setInput("serviceId", "123");
        component.renewServiceParams = null;

        component.renewService();

        expect(mockDigitalServicesDataService.renewService).not.toHaveBeenCalled();
        expect(mockInventoryDataService.renewService).not.toHaveBeenCalled();
    });

    it("should emit outClose when closePopup is called", () => {
        spyOn(component.outClose, "emit");

        component.closePopup();

        expect(component.isRenewButtonDisabled).toBe(false);
        expect(component.outClose.emit).toHaveBeenCalled();
    });

    it("should reset isRenewButtonDisabled to false on closePopup", () => {
        component.isRenewButtonDisabled = true;

        component.closePopup();

        expect(component.isRenewButtonDisabled).toBe(false);
    });

    it("should handle error when getServiceRenewalDetails fails for digital services", () => {
        mockDigitalServicesDataService.getServiceRenewalDetails.and.returnValue(
            throwError(() => new Error("Service error")),
        );

        fixture.componentRef.setInput("serviceId", "123");
        fixture.componentRef.setInput("isInventory", false);

        expect(() => fixture.detectChanges()).not.toThrow();
    });

    it("should handle error when getServiceRenewalDetails fails for inventory", () => {
        mockInventoryDataService.getServiceRenewalDetails.and.returnValue(
            throwError(() => new Error("Service error")),
        );

        fixture.componentRef.setInput("serviceId", "456");
        fixture.componentRef.setInput("isInventory", true);

        expect(() => fixture.detectChanges()).not.toThrow();
    });

    it("should disable button during renewService call", () => {
        mockDigitalServicesDataService.renewService.and.returnValue(
            of(mockRenewServiceUpdateResp),
        );

        component.isRenewButtonDisabled = false;
        fixture.componentRef.setInput("serviceId", "123");
        fixture.componentRef.setInput("isInventory", false);
        component.renewServiceParams = mockRenewServiceResp;

        component.renewService();

        expect(component.isRenewButtonDisabled).toBe(true);
    });
});
