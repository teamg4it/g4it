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
    // =============================
// EXISTING NAMES (filter branch)
// =============================
it("should filter existing names when editing (not new)", () => {
    component.networkData = [
        { name: "A" },
        { name: "B" }
    ] as any;

    component.network = { idFront: 1, name: "A" } as any;

    component.ngOnInit();

    // "A" doit être exclu car on édite A
    expect(component.existingNames).toEqual(["B"]);
});

// =============================
// PATCH VALUE (edit mode)
// =============================
it("should patch form when editing existing network", () => {
    component.network = {
        idFront: 1,
        name: "Net1",
        type: { code: "1" },
        yearlyQuantityOfGbExchanged: 123
    } as any;

    component.ngOnInit();

    expect(component.networksForm.value.name).toBe("Net1");
    expect(component.networksForm.value.yearlyQuantityOfGbExchanged).toBe(123);
});

// =============================
// ONLY QUANTITY EDITABLE
// =============================
it("should disable name and type when onlyQuantityEditable is true", () => {
    component.onlyQuantityEditable = true;

    component.ngOnInit();

    expect(component.networksForm.get('name')?.disabled).toBeTrue();
    expect(component.networksForm.get('type')?.disabled).toBeTrue();
});

// =============================
// EDITABLE FIELDS (partial enable)
// =============================
it("should disable name and type when onlyQuantityEditable is true", () => {
    component.onlyQuantityEditable = true;

    component.ngOnInit();

    expect(component.networksForm.get('name')?.disabled).toBeTrue();
    expect(component.networksForm.get('type')?.disabled).toBeTrue();
});

// =============================
// ngOnChanges (with existing form)
// =============================
it("should patch form on ngOnChanges", () => {
    component.ngOnInit();

    component.network = {
        name: "Changed",
        type: { code: "X" },
        yearlyQuantityOfGbExchanged: 999
    } as any;

    component.ngOnChanges({
        network: {
            currentValue: component.network,
            previousValue: null,
            firstChange: false,
            isFirstChange: () => false
        }
    });

    expect(component.networksForm.value.name).toBe("Changed");
    expect(component.networksForm.value.yearlyQuantityOfGbExchanged).toBe(999);
});

// =============================
// ngOnChanges (without form → initForm)
// =============================
it("should init form if not existing in ngOnChanges", () => {
    component.networksForm = undefined as any;

    component.network = {
        name: "Init",
        type: { code: "Y" },
        yearlyQuantityOfGbExchanged: 50
    } as any;

    component.ngOnChanges({
        network: {
            currentValue: component.network,
            previousValue: null,
            firstChange: false,
            isFirstChange: () => false
        }
    });

    expect(component.networksForm).toBeDefined();
    expect(component.networksForm.value.name).toBe("Init");
});

// =============================
// INIT FORM DIRECT
// =============================
it("should initialize form manually", () => {
    component.networkData = [{ name: "A" }] as any;

    component.initForm();

    expect(component.networksForm).toBeDefined();
    expect(component.networksForm.get('name')).toBeTruthy();
});
});
