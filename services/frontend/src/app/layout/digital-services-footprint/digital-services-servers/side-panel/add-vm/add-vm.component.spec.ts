import { signal } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import {
    DigitalServiceServerConfig,
    ServerVM,
} from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { PanelAddVmComponent } from "./add-vm.component";

describe("PanelAddVmComponent", () => {
    let component: PanelAddVmComponent;
    let fixture: ComponentFixture<PanelAddVmComponent>;

    let serverConfig: DigitalServiceServerConfig;
    let digitalServiceStore: jasmine.SpyObj<DigitalServiceStoreService>;

    const createServer = (): DigitalServiceServerConfig => ({
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
    });

    beforeEach(async () => {
        serverConfig = createServer();
        digitalServiceStore = jasmine.createSpyObj("DigitalServiceStoreService", [
            "server",
            "setServer",
        ]);
        digitalServiceStore.server.and.returnValue(serverConfig);

        await TestBed.configureTestingModule({
            imports: [PanelAddVmComponent],
            providers: [
                { provide: UserService, useValue: {} as UserService },
                { provide: DigitalServiceStoreService, useValue: digitalServiceStore },
            ],
        })
            .overrideComponent(PanelAddVmComponent, { set: { template: "" } })
            .compileComponents();

        fixture = TestBed.createComponent(PanelAddVmComponent);
        component = fixture.componentInstance;

        (component as any).server = signal(serverConfig);
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize a new VM with the next available name", () => {
        component.index = undefined;
        component.ngOnInit();

        expect(component.vm.name).toBe("VM 2");
        expect(component.vm).toEqual({
            uid: "",
            name: "VM 2",
            vCpu: 1,
            disk: 1,
            quantity: 1,
            annualOperatingTime: 8760,
            electricityConsumption: undefined as any,
        });
        expect(component.vm.electricityConsumption).toBeUndefined();
    });

    it("should initialize the first VM name when the server has no VMs", () => {
        serverConfig.vm = [];
        component.index = undefined;

        component.ngOnInit();

        expect(component.vm.name).toBe("VM 1");
    });

    it("should clone the VM selected for editing", () => {
        component.index = 0;
        component.ngOnInit();

        expect(component.vm.name).toBe("Base VM");
        component.vm.name = "Modified";
        expect(serverConfig.vm[0].name).toBe("Base VM");
    });

    it("should validate Compute vCPUs and quantity", () => {
        component.addVmForm.patchValue({ vcpu: 7, quantity: 1 });

        component.verifyValue();

        expect(component.vcpuControl.errors?.["isValueTooHigh"]).toBeTrue();
        expect(component.quantityControl.errors?.["isQuantityTooLow"]).toBeUndefined();

        component.addVmForm.patchValue({ vcpu: 6, quantity: 0 });
        component.verifyValue();

        expect(component.vcpuControl.errors?.["isValueTooHigh"]).toBeUndefined();
        expect(component.quantityControl.errors?.["isQuantityTooLow"]).toBeTrue();
    });

    it("should validate Storage disk capacity", () => {
        serverConfig.type = "Storage";
        serverConfig.totalDisk = 100;
        component.addVmForm.patchValue({ disk: 51, quantity: 1 });

        component.verifyValue();

        expect(component.diskControl.errors?.["isValueTooHigh"]).toBeTrue();

        component.addVmForm.patchValue({ disk: 50, quantity: 1 });
        component.verifyValue();

        expect(component.diskControl.errors?.["isValueTooHigh"]).toBeUndefined();
    });

    it("should calculate capacity sums for Compute and Storage while excluding the edited VM", () => {
        component.vm = serverConfig.vm[0];

        expect(component.sum()).toBe(0);

        component.vm = {} as ServerVM;
        expect(component.sum()).toBe(2);

        serverConfig.type = "Storage";
        expect(component.sum()).toBe(50);
    });

    it("should validate electricity consumption against remaining server capacity", () => {
        component.vm = {} as ServerVM;
        component.addVmForm.patchValue({ electricityConsumption: 180 });
        component.verifyElectricityValue();

        expect(
            component.electricityConsumptionControl.errors?.["isElecValueTooHigh"],
        ).toBeTrue();

        component.addVmForm.patchValue({ electricityConsumption: 150 });
        component.verifyElectricityValue();

        expect(
            component.electricityConsumptionControl.errors?.["isElecValueTooHigh"],
        ).toBeUndefined();
    });

    it("should add a new VM and close the panel on submission", () => {
        const close = spyOn(component, "close");
        component.index = undefined;
        component.vm = { name: "New VM" } as ServerVM;

        component.submitFormData();

        expect(serverConfig.vm).toContain(component.vm);
        expect(digitalServiceStore.setServer).toHaveBeenCalledWith(serverConfig);
        expect(close).toHaveBeenCalled();
    });

    it("should update the selected VM and close the panel on submission", () => {
        const close = spyOn(component, "close");
        const updatedVm = { name: "Updated VM" } as ServerVM;
        component.index = 0;
        component.vm = updatedVm;

        component.submitFormData();

        expect(serverConfig.vm[0]).toBe(updatedVm);
        expect(digitalServiceStore.setServer).toHaveBeenCalledWith(serverConfig);
        expect(close).toHaveBeenCalled();
    });

    it("close should emit false", () => {
        const emitSpy = spyOn(component.addVMPanelVisibleChange, "emit");
        component.close();
        expect(emitSpy).toHaveBeenCalledWith(false);
    });
});
