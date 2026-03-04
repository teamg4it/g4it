import { HttpClientTestingModule } from "@angular/common/http/testing";
import { EventEmitter } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ReactiveFormsModule } from "@angular/forms";
import { TranslateModule } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { UserService } from "src/app/core/service/business/user.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { DigitalServicesNetworksSidePanelComponent } from "./digital-services-networks-side-panel.component";

describe("DigitalServicesNetworksSidePanelComponent", () => {
    let component: DigitalServicesNetworksSidePanelComponent;
    let fixture: ComponentFixture<DigitalServicesNetworksSidePanelComponent>;

    beforeEach(async () => {
        const mockDigitalServiceStore = {
            networkTypes: jasmine.createSpy("networkTypes").and.returnValue([
                { code: "1", value: "Network Type 1" },
                { code: "2", value: "Network Type 2" },
            ]),
        };
        await TestBed.configureTestingModule({
            declarations: [DigitalServicesNetworksSidePanelComponent],
            imports: [
                SharedModule,
                ReactiveFormsModule,
                ButtonModule,
                DropdownModule,
                InputNumberModule,
                InputTextModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                {
                    provide: DigitalServiceStoreService,
                    useValue: mockDigitalServiceStore,
                },
                { provide: UserService, useValue: {} },
                MessageService,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesNetworksSidePanelComponent);
        component = fixture.componentInstance;
        component.network = { idFront: undefined, name: "" } as any;
        component.networkData = [];
        component.update = new EventEmitter();
        component.delete = new EventEmitter();
        component.outCancel = new EventEmitter();
        component.sidebarVisible = new EventEmitter();
        fixture.detectChanges();
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize the form on ngOnInit", () => {
        component.networkData = [{ name: "Network1" } as any];
        component.network = { idFront: undefined, name: "" } as any;
        component.ngOnInit();
        expect(component.networksForm).toBeDefined();
        expect(component.existingNames).toEqual(["Network1"]);
    });

    it("should emit delete event when deleteNetwork is called", () => {
        spyOn(component.delete, "emit");
        component.network = { idFront: 1, name: "Test Network" } as any;
        component.deleteNetwork();
        expect(component.delete.emit).toHaveBeenCalledWith(component.network);
    });

    it("should emit update event with updated network when submitFormData is called", () => {
        spyOn(component.update, "emit");
        component.networksForm.setValue({
            name: "Updated Network",
            type: {
                code: "1",
                value: "Type1",
                country: "FR",
                type: "Type",
                annualQuantityOfGo: 100,
            },
            yearlyQuantityOfGbExchanged: 500,
        });
        component.submitFormData();
        expect(component.update.emit).toHaveBeenCalledWith({
            ...component.network,
            type: {
                code: "1",
                value: "Type1",
                country: "FR",
                type: "Type",
                annualQuantityOfGo: 100,
            },
        });
    });

    it("should emit outCancel event when cancelNetwork is called", () => {
        spyOn(component.outCancel, "emit");
        component.cancelNetwork();
        expect(component.outCancel.emit).toHaveBeenCalledWith(component.network);
    });

    it("should emit outCancel and sidebarVisible events when close is called", () => {
        spyOn(component.outCancel, "emit");
        spyOn(component.sidebarVisible, "emit");
        component.close();
        expect(component.outCancel.emit).toHaveBeenCalledWith(component.network);
        expect(component.sidebarVisible.emit).toHaveBeenCalledWith(false);
    });
});
