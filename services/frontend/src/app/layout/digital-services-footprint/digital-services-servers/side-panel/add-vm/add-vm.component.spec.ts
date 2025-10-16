import { HttpClientTestingModule } from "@angular/common/http/testing";
import { signal } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ReactiveFormsModule } from "@angular/forms";
import { TranslateModule } from "@ngx-translate/core";
import { DigitalServiceServerConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { PanelAddVmComponent } from "./add-vm.component";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";

describe("PanelAddVmComponent", () => {
    let component: PanelAddVmComponent;
    let fixture: ComponentFixture<PanelAddVmComponent>;

    const serverConfig: DigitalServiceServerConfig = {
        type: "Compute",
        totalVCpu: 8,
        totalDisk: 500,
        annualElectricConsumption: 200,
        name: "Compute Server",
        mutualizationType: "Dedicated",
        quantity: 1,
        vm: [
            {
                uid: "VM1",
                name: "Base VM",
                vCpu: 2,
                disk: 50,
                quantity: 1,
                annualOperatingTime: 8760,
                electricityConsumption: 30,
            },
        ],
    };

    const mockDigitalServiceStore = {
        server: () => serverConfig,
        setServer: jasmine.createSpy("setServer"),
    };

    const mockUserService = {
        currentOrganization$: { subscribe: () => {} },
    } as any as UserService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ReactiveFormsModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
            ],
            declarations: [PanelAddVmComponent],
            providers: [
                { provide: UserService, useValue: mockUserService },
                { provide: DigitalServiceStoreService, useValue: mockDigitalServiceStore },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(PanelAddVmComponent);
        component = fixture.componentInstance;

        // Assign server signal AFTER component is created
        (component as any).server = signal(serverConfig);

        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("ngOnInit (new VM) should set next VM name (VM 2)", () => {
        component.index = undefined;
        component.ngOnInit();
        expect(component.vm.name).toBe("VM 2");
    });

    it("ngOnInit (edit existing) should clone selected vm", () => {
        component.index = 0;
        component.ngOnInit();
        expect(component.vm.name).toBe("Base VM");
        component.vm.name = "Modified";
        // original remains unchanged
        expect(serverConfig.vm[0].name).toBe("Base VM");
    });

    it("verifyElectricityValue should set and clear isElecValueTooHigh", () => {
        component.index = undefined;
        component.ngOnInit();
        component.addVmForm.patchValue({ electricityConsumption: 180 });
        component.verifyElectricityValue();
        expect(component.electricityConsumptionControl.errors?.["isElecValueTooHigh"]).toBeTrue();

        component.addVmForm.patchValue({ electricityConsumption: 150 });
        component.verifyElectricityValue();
        expect(component.electricityConsumptionControl.errors?.["isElecValueTooHigh"]).toBeUndefined();
    });

    it("close should emit false", () => {
        const emitSpy = spyOn(component.addVMPanelVisibleChange, "emit");
        component.close();
        expect(emitSpy).toHaveBeenCalledWith(false);
    });
});
