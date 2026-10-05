import { ComponentFixture, fakeAsync, TestBed, tick } from "@angular/core/testing";
import { ActivatedRoute, Router } from "@angular/router";
import {
    DigitalService,
    DigitalServiceServerConfig,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { PanelListVmComponent } from "./list-vm.component";

describe("PanelListVmComponent", () => {
    let component: PanelListVmComponent;
    let fixture: ComponentFixture<PanelListVmComponent>;

    let serverConfig: DigitalServiceServerConfig;
    let digitalService: DigitalService;
    let digitalServiceStore: jasmine.SpyObj<DigitalServiceStoreService>;
    let digitalServiceBusiness: jasmine.SpyObj<DigitalServiceBusinessService>;
    let router: jasmine.SpyObj<Router>;

    const createServer = (): DigitalServiceServerConfig =>
        ({
            type: "Compute",
            name: "Compute Server",
            mutualizationType: "Shared",
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
        }) as DigitalServiceServerConfig;

    beforeEach(async () => {
        serverConfig = createServer();
        digitalService = { uid: "ds-uid" } as DigitalService;

        digitalServiceStore = jasmine.createSpyObj("DigitalServiceStoreService", [
            "server",
            "digitalService",
            "setServer",
        ]);
        digitalServiceStore.server.and.returnValue(serverConfig);
        digitalServiceStore.digitalService.and.returnValue(digitalService);

        digitalServiceBusiness = jasmine.createSpyObj("DigitalServiceBusinessService", [
            "submitServerForm",
            "closePanel",
            "openPanel",
        ]);

        router = jasmine.createSpyObj("Router", ["navigate"]);

        await TestBed.configureTestingModule({
            imports: [PanelListVmComponent],
            providers: [
                { provide: DigitalServiceStoreService, useValue: digitalServiceStore },
                {
                    provide: DigitalServiceBusinessService,
                    useValue: digitalServiceBusiness,
                },
                { provide: Router, useValue: router },
                { provide: ActivatedRoute, useValue: {} as ActivatedRoute },
                { provide: UserService, useValue: {} as UserService },
            ],
        })
            .overrideComponent(PanelListVmComponent, { set: { template: "" } })
            .compileComponents();

        fixture = TestBed.createComponent(PanelListVmComponent);
        component = fixture.componentInstance;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should return Compute header fields", () => {
        serverConfig.type = "Compute";
        expect(component.headerFields()).toEqual([
            "name",
            "quantity",
            "vCpu",
            "annualOperatingTime",
            "electricityConsumption",
        ]);
    });

    it("should return AI header fields", () => {
        serverConfig.type = "AI";
        expect(component.headerFields()).toEqual([
            "name",
            "quantity",
            "vRam",
            "annualOperatingTime",
            "electricityConsumption",
        ]);
    });

    it("should return Storage (default) header fields", () => {
        serverConfig.type = "Storage";
        expect(component.headerFields()).toEqual([
            "name",
            "quantity",
            "disk",
            "annualOperatingTime",
            "electricityConsumption",
        ]);
    });

    it("should return a copy of the server VMs", () => {
        const vmData = component.vmData();
        expect(vmData).toEqual(serverConfig.vm);
        expect(vmData).not.toBe(serverConfig.vm);
    });

    it("should return the server from the store", () => {
        expect(component.server()).toBe(serverConfig);
    });

    it("should reset the index and open the panel", () => {
        component.index = 3;
        component.resetIndex();
        expect(component.index).toBeUndefined();
        expect(component.addVMPanelVisible).toBeTrue();
    });

    it("should set the index and open the panel", () => {
        component.setIndex(2);
        expect(component.index).toBe(2);
        expect(component.addVMPanelVisible).toBeTrue();
    });

    it("should delete a VM and update the store", () => {
        component.deleteVm(0);
        expect(serverConfig.vm).toEqual([]);
        expect(digitalServiceStore.setServer).toHaveBeenCalledWith(serverConfig);
    });

    it("should not delete anything when the server has no VMs", () => {
        serverConfig.vm = undefined as any;
        component.deleteVm(0);
        expect(digitalServiceStore.setServer).not.toHaveBeenCalled();
    });

    it("should navigate to the previous step", () => {
        component.previousStep();
        expect(router.navigate).toHaveBeenCalledWith(["../panel-parameters"], {
            relativeTo: (component as any).route,
        });
    });

    it("should submit the server form and close the panel", async () => {
        const close = spyOn(component, "close");

        await component.submitServer();

        expect(digitalServiceBusiness.submitServerForm).toHaveBeenCalledWith(
            serverConfig,
            digitalService,
        );
        expect(close).toHaveBeenCalled();
    });

    it("should close the panel via the business service", () => {
        component.close();
        expect(digitalServiceBusiness.closePanel).toHaveBeenCalled();
    });

    it("should open the side panel via the business service", () => {
        component.openSidePanel();
        expect(digitalServiceBusiness.openPanel).toHaveBeenCalled();
    });

    it("should focus the add-vm button for a given index after the delay", fakeAsync(() => {
        const button = document.createElement("button");
        const container = document.createElement("div");
        container.id = "add-vm2";
        container.appendChild(button);
        document.body.appendChild(container);
        const focusSpy = spyOn(button, "focus");

        component.index = 1;
        component.focusVmButton();
        tick(400);

        expect(focusSpy).toHaveBeenCalled();
        document.body.removeChild(container);
    }));

    it("should focus the default add-vm button when no index is set", fakeAsync(() => {
        const button = document.createElement("button");
        const container = document.createElement("div");
        container.id = "add-vm";
        container.appendChild(button);
        document.body.appendChild(container);
        const focusSpy = spyOn(button, "focus");

        component.index = undefined;
        component.focusVmButton();
        tick(400);

        expect(focusSpy).toHaveBeenCalled();
        document.body.removeChild(container);
    }));
});
