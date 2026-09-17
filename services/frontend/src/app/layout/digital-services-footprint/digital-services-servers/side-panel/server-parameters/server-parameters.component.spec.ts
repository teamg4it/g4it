import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateModule } from "@ngx-translate/core";
import { of } from "rxjs";
import {
    DigitalServiceServerConfig,
    Host,
    ServerDC,
    ServerVM,
} from "src/app/core/interfaces/digital-service.interfaces";
import { InDatacenterRest } from "src/app/core/interfaces/input.interface";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { InDatacentersService } from "src/app/core/service/data/in-out/in-datacenters.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { PanelServerParametersComponent } from "./server-parameters.component";

describe("PanelServerParametersComponent", () => {
    let component: PanelServerParametersComponent;
    let digitalServiceStore: jasmine.SpyObj<DigitalServiceStoreService>;
    let digitalServiceBusiness: jasmine.SpyObj<DigitalServiceBusinessService>;
    let inDatacentersService: jasmine.SpyObj<InDatacentersService>;
    let router: jasmine.SpyObj<Router>;

    const baseServer = (
        overrides: Partial<DigitalServiceServerConfig> = {},
    ): DigitalServiceServerConfig =>
        ({
            name: "Server",
            mutualizationType: "Dedicated",
            type: "Compute",
            quantity: 1,
            vm: [],
            datacenter: {} as ServerDC,
            ...overrides,
        }) as DigitalServiceServerConfig;

    async function setup(config: {
        server?: DigitalServiceServerConfig;
        serverTypes?: Host[];
        inDatacenters?: InDatacenterRest[];
        digitalService?: any;
    }): Promise<{
        component: PanelServerParametersComponent;
        fixture: ComponentFixture<PanelServerParametersComponent>;
        digitalServiceStore: jasmine.SpyObj<DigitalServiceStoreService>;
        digitalServiceBusiness: jasmine.SpyObj<DigitalServiceBusinessService>;
        inDatacentersService: jasmine.SpyObj<InDatacentersService>;
        router: jasmine.SpyObj<Router>;
    }> {
        const store = jasmine.createSpyObj("DigitalServiceStoreService", [
            "server",
            "serverTypes",
            "inDatacenters",
            "digitalService",
            "setServer",
            "setInDatacenters",
        ]);
        store.server.and.returnValue(config.server ?? baseServer());
        store.serverTypes.and.returnValue(config.serverTypes ?? []);
        store.inDatacenters.and.returnValue(config.inDatacenters ?? []);
        store.digitalService.and.returnValue(config.digitalService ?? { uid: "ds-uid" });

        const business = jasmine.createSpyObj("DigitalServiceBusinessService", [
            "submitServerForm",
            "closePanel",
        ]);

        const inDatacenters = jasmine.createSpyObj("InDatacentersService", [
            "create",
            "get",
        ]);
        inDatacenters.create.and.returnValue(of({}));
        inDatacenters.get.and.returnValue(of([]));

        const routerSpy = jasmine.createSpyObj("Router", ["navigate"]);

        TestBed.resetTestingModule();
        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot(), PanelServerParametersComponent],
            providers: [
                { provide: DigitalServiceStoreService, useValue: store },
                { provide: DigitalServiceBusinessService, useValue: business },
                { provide: InDatacentersService, useValue: inDatacenters },
                { provide: Router, useValue: routerSpy },
                { provide: ActivatedRoute, useValue: {} as ActivatedRoute },
                { provide: UserService, useValue: {} as UserService },
            ],
        })
            .overrideComponent(PanelServerParametersComponent, { set: { template: "" } })
            .compileComponents();

        const fixture = TestBed.createComponent(PanelServerParametersComponent);
        return {
            component: fixture.componentInstance,
            fixture,
            digitalServiceStore: store,
            digitalServiceBusiness: business,
            inDatacentersService: inDatacenters,
            router: routerSpy,
        };
    }

    beforeEach(async () => {
        const ctx = await setup({});
        component = ctx.component;
        digitalServiceStore = ctx.digitalServiceStore;
        digitalServiceBusiness = ctx.digitalServiceBusiness;
        inDatacentersService = ctx.inDatacentersService;
        router = ctx.router;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    describe("defaultHostForType", () => {
        it("should pick the Compute default host", () => {
            const hosts: Host[] = [
                { code: 1, value: "Server Compute M", characteristic: [] },
            ];
            expect((component as any).defaultHostForType("Compute", hosts)).toBe(
                hosts[0],
            );
        });

        it("should pick the Storage default host", () => {
            const hosts: Host[] = [
                { code: 2, value: "Server Storage M", characteristic: [] },
            ];
            expect((component as any).defaultHostForType("Storage", hosts)).toBe(
                hosts[0],
            );
        });

        it("should pick the AI default host for any other type", () => {
            const hosts: Host[] = [
                { code: 3, value: "Medium AI Server", characteristic: [] },
            ];
            expect((component as any).defaultHostForType("AI", hosts)).toBe(hosts[0]);
        });
    });

    describe("applyDefaultCharacteristics", () => {
        it("should assign missing characteristics from the host", () => {
            const srv = baseServer({
                type: "Compute",
                host: {
                    code: 1,
                    value: "Host",
                    characteristic: [
                        { code: "vCPU", value: 8 },
                        { code: "annualElectricityConsumption", value: 500 },
                        { code: "lifespan", value: 5 },
                    ],
                },
            });

            (component as any).applyDefaultCharacteristics(srv);

            expect(srv.totalVCpu).toBe(8);
            expect(srv.annualElectricConsumption).toBe(500);
            expect(srv.lifespan).toBe(5);
            expect(srv.totalVram).toBeUndefined();
            expect(srv.totalDisk).toBeUndefined();
        });

        it("should assign the Storage disk characteristic when missing", () => {
            const srv = baseServer({
                type: "Storage",
                host: {
                    code: 2,
                    value: "Host",
                    characteristic: [{ code: "disk", value: 300 }],
                },
                annualElectricConsumption: 50,
                lifespan: 10,
            });

            (component as any).applyDefaultCharacteristics(srv);

            expect(srv.totalDisk).toBe(300);
            expect(srv.annualElectricConsumption).toBe(50);
            expect(srv.lifespan).toBe(10);
        });

        it("should assign the AI vRAM characteristic when missing", () => {
            const srv = baseServer({
                type: "AI",
                host: {
                    code: 3,
                    value: "Host",
                    characteristic: [{ code: "vCPU", value: 16 }],
                },
            });

            (component as any).applyDefaultCharacteristics(srv);

            expect(srv.totalVram).toBe(16);
        });

        it("should keep existing totalVCpu and totalDisk values", () => {
            const srvCompute = baseServer({ type: "Compute", totalVCpu: 5 });
            (component as any).applyDefaultCharacteristics(srvCompute);
            expect(srvCompute.totalVCpu).toBe(5);

            const srvStorage = baseServer({ type: "Storage", totalDisk: 50 });
            (component as any).applyDefaultCharacteristics(srvStorage);
            expect(srvStorage.totalDisk).toBe(50);

            const srvAi = baseServer({ type: "AI", totalVram: 99 });
            (component as any).applyDefaultCharacteristics(srvAi);
            expect(srvAi.totalVram).toBe(99);
        });
    });

    describe("resolveDatacenter", () => {
        it("should use the current datacenter name when set", () => {
            (component as any).current.datacenter = { name: "Existing DC" } as ServerDC;
            const srv = baseServer({
                datacenter: { name: "Existing DC", location: "France", pue: 1 },
            });
            const datacenters: ServerDC[] = [
                { name: "Existing DC", location: "France", pue: 1 },
            ];

            const result = (component as any).resolveDatacenter(srv, datacenters);

            expect(result).toBe(datacenters[0]);
        });

        it("should fall back to Default DC when no current datacenter name is set", () => {
            (component as any).current.datacenter = {} as ServerDC;
            const srv = baseServer({ datacenter: undefined });
            const datacenters: ServerDC[] = [
                { name: "Default DC", location: "France", pue: 1.5 },
            ];

            const result = (component as any).resolveDatacenter(srv, datacenters);

            expect(result).toBe(datacenters[0]);
        });
    });

    describe("getServerTotal", () => {
        it("should return totalVCpu for Compute", () => {
            expect(
                (component as any).getServerTotal(
                    baseServer({ type: "Compute", totalVCpu: 8 }),
                ),
            ).toBe(8);
        });

        it("should return totalDisk for Storage", () => {
            expect(
                (component as any).getServerTotal(
                    baseServer({ type: "Storage", totalDisk: 100 }),
                ),
            ).toBe(100);
        });

        it("should return totalVram for any other type", () => {
            expect(
                (component as any).getServerTotal(
                    baseServer({ type: "AI", totalVram: 20 }),
                ),
            ).toBe(20);
        });
    });

    describe("getVmValue", () => {
        const vm: ServerVM = {
            uid: "1",
            name: "VM",
            vCpu: 2,
            disk: 50,
            vRam: 5,
            quantity: 1,
            annualOperatingTime: 8760,
            electricityConsumption: 10,
        };

        it("should return vCpu for Compute", () => {
            expect(
                (component as any).getVmValue(baseServer({ type: "Compute" }), vm),
            ).toBe(2);
        });

        it("should return disk for Storage", () => {
            expect(
                (component as any).getVmValue(baseServer({ type: "Storage" }), vm),
            ).toBe(50);
        });

        it("should return vRam for any other type", () => {
            expect((component as any).getVmValue(baseServer({ type: "AI" }), vm)).toBe(5);
        });
    });

    describe("setControlError", () => {
        it("should merge a new error while keeping other existing errors", () => {
            const control = component.serverForm.get("quantity")!;
            control.setErrors({ otherError: true });

            (component as any).setControlError(control, "isValueTooHigh", true);

            expect(control.errors).toEqual({ otherError: true, isValueTooHigh: true });
        });

        it("should remove only the given error while keeping other existing errors", () => {
            const control = component.serverForm.get("quantity")!;
            control.setErrors({ otherError: true, isValueTooHigh: true });

            (component as any).setControlError(control, "isValueTooHigh", false);

            expect(control.errors).toEqual({ otherError: true });
        });

        it("should clear all errors when none remain", () => {
            const control = component.serverForm.get("quantity")!;
            control.setErrors({ isValueTooHigh: true });

            (component as any).setControlError(control, "isValueTooHigh", false);

            expect(control.errors).toBeNull();
        });
    });

    describe("setDefaultForm", () => {
        it("should do nothing when the current host has no code", () => {
            (component as any).current.host = {} as Host;

            component.setDefaultForm("Compute");

            expect(component.serverForm.controls["vcpu"].value).toBe(0);
        });

        it("should set electricity, lifespan and vcpu for Compute", () => {
            (component as any).current.host = {
                code: 1,
                value: "Host",
                characteristic: [
                    { code: "annualElectricityConsumption", value: 400 },
                    { code: "lifespan", value: 4 },
                    { code: "vCPU", value: 12 },
                ],
            } as Host;

            component.setDefaultForm("Compute");

            expect(component.serverForm.controls["electricityConsumption"].value).toBe(
                400,
            );
            expect(component.serverForm.controls["lifespan"].value).toBe(4);
            expect(component.serverForm.controls["vcpu"].value).toBe(12);
        });

        it("should set vcpu for AI as well", () => {
            (component as any).current.host = {
                code: 1,
                value: "Host",
                characteristic: [{ code: "vCPU", value: 20 }],
            } as Host;

            component.setDefaultForm("AI");

            expect(component.serverForm.controls["vcpu"].value).toBe(20);
        });

        it("should set disk for Storage", () => {
            (component as any).current.host = {
                code: 1,
                value: "Host",
                characteristic: [{ code: "disk", value: 250 }],
            } as Host;

            component.setDefaultForm("Storage");

            expect(component.serverForm.controls["disk"].value).toBe(250);
        });

        it("should not set vcpu nor disk for an unknown type", () => {
            (component as any).current.host = {
                code: 1,
                value: "Host",
                characteristic: [
                    { code: "vCPU", value: 20 },
                    { code: "disk", value: 250 },
                ],
            } as Host;

            component.setDefaultForm("Other");

            expect(component.serverForm.controls["vcpu"].value).toBe(0);
            expect(component.serverForm.controls["disk"].value).toBe(0);
        });
    });

    describe("verifyValue", () => {
        it("should return early when the matching control is missing", () => {
            spyOn(component.serverForm, "get").and.returnValue(null);
            const setControlErrorSpy = spyOn(component as any, "setControlError");

            component.verifyValue(baseServer());

            expect(setControlErrorSpy).not.toHaveBeenCalled();
        });

        it("should not flag an error when there are no VMs", () => {
            component.verifyValue(baseServer({ type: "Compute", vm: [] }));

            expect(component.totalVmvCpu).toBe(0);
            expect(
                component.serverForm.controls["vcpu"].errors?.["isValueTooHigh"],
            ).toBeFalsy();
        });

        it("should mark the control dirty when the VM total exceeds capacity", () => {
            const srv = baseServer({
                type: "Compute",
                totalVCpu: 2,
                vm: [
                    {
                        uid: "1",
                        name: "VM1",
                        vCpu: 5,
                        disk: 0,
                        quantity: 1,
                        annualOperatingTime: 8760,
                        electricityConsumption: 0,
                    },
                ],
            });
            const control = component.serverForm.get("vcpu")!;
            const markAsDirtySpy = spyOn(control, "markAsDirty").and.callThrough();

            component.verifyValue(srv);

            expect(control.errors?.["isValueTooHigh"]).toBeTrue();
            expect(markAsDirtySpy).toHaveBeenCalled();
        });

        it("should not mark the control dirty when the VM total is within capacity", () => {
            const srv = baseServer({
                type: "Compute",
                totalVCpu: 20,
                vm: [
                    {
                        uid: "1",
                        name: "VM1",
                        vCpu: 5,
                        disk: 0,
                        quantity: 1,
                        annualOperatingTime: 8760,
                        electricityConsumption: 0,
                    },
                ],
            });
            const control = component.serverForm.get("vcpu")!;
            const markAsDirtySpy = spyOn(control, "markAsDirty").and.callThrough();

            component.verifyValue(srv);

            expect(control.errors?.["isValueTooHigh"]).toBeFalsy();
            expect(markAsDirtySpy).not.toHaveBeenCalled();
        });

        it("should validate the vRAM control and default a missing total to zero for AI", () => {
            const srv = baseServer({
                type: "AI",
                totalVram: undefined,
                vm: [
                    {
                        uid: "1",
                        name: "VM1",
                        vCpu: 0,
                        disk: 0,
                        vRam: 5,
                        quantity: 1,
                        annualOperatingTime: 8760,
                        electricityConsumption: 0,
                    },
                ],
            });
            const control = component.serverForm.get("vram")!;

            component.verifyValue(srv);

            expect(control.errors?.["isValueTooHigh"]).toBeTrue();
        });
    });

    describe("changeServer", () => {
        it("should update the current host, apply defaults and persist the server", async () => {
            const ctx = await setup({
                server: baseServer({ type: "Compute" }),
            });
            // Prime the "server" computed cache, as would happen via template bindings
            // before the user interacts with the host dropdown.
            ctx.component.server();
            const host: Host = {
                code: 9,
                value: "New Host",
                characteristic: [{ code: "vCPU", value: 10 }],
            };

            ctx.component.changeServer({ value: host });

            expect((ctx.component as any).current.host).toBe(host);
            expect(ctx.digitalServiceStore.setServer).toHaveBeenCalled();
            const savedServer = ctx.digitalServiceStore.setServer.calls.mostRecent()
                .args[0] as DigitalServiceServerConfig;
            expect(savedServer.host).toBe(host);
        });
    });

    describe("addDatacenter", () => {
        it("should create the datacenter, refresh the list and assign it to the server", async () => {
            const createdDatacenters: InDatacenterRest[] = [
                { name: "NewDC|uid", location: "France", pue: 1.5 },
            ];
            inDatacentersService.get.and.returnValue(of(createdDatacenters));

            await component.addDatacenter({
                name: "NewDC",
                location: "France",
                pue: 1.5,
            });

            expect(inDatacentersService.create).toHaveBeenCalled();
            expect(digitalServiceStore.setInDatacenters).toHaveBeenCalledWith(
                createdDatacenters,
            );
            expect(digitalServiceStore.setServer).toHaveBeenCalled();
        });
    });

    describe("previousStep", () => {
        it("should reset transient fields and navigate back", async () => {
            const ctx = await setup({
                server: baseServer({
                    type: "Compute",
                    annualElectricConsumption: 100,
                    lifespan: 5,
                    totalDisk: 50,
                    totalVCpu: 8,
                    totalVram: 10,
                }),
            });

            ctx.component.previousStep();

            expect(ctx.digitalServiceStore.setServer).toHaveBeenCalled();
            const savedServer = ctx.digitalServiceStore.setServer.calls.mostRecent()
                .args[0] as DigitalServiceServerConfig;
            expect(savedServer.annualElectricConsumption).toBeUndefined();
            expect(savedServer.lifespan).toBeUndefined();
            expect(savedServer.totalDisk).toBeUndefined();
            expect(savedServer.totalVCpu).toBeUndefined();
            expect(savedServer.totalVram).toBeUndefined();
            expect(ctx.router.navigate).toHaveBeenCalledWith(["../panel-create"], {
                relativeTo: (ctx.component as any).route,
            });
        });
    });

    describe("nextStep", () => {
        it("should submit the form and close for a Dedicated server", async () => {
            const ctx = await setup({
                server: baseServer({ mutualizationType: "Dedicated" }),
            });
            const close = spyOn(ctx.component, "close");

            await ctx.component.nextStep();

            expect(ctx.digitalServiceBusiness.submitServerForm).toHaveBeenCalled();
            expect(close).toHaveBeenCalled();
        });

        it("should navigate to the VM panel for a Shared server", async () => {
            const ctx = await setup({
                server: baseServer({ mutualizationType: "Shared" }),
            });

            await ctx.component.nextStep();

            expect(ctx.router.navigate).toHaveBeenCalledWith(["../panel-vm"], {
                relativeTo: (ctx.component as any).route,
            });
        });

        it("should do nothing for any other mutualization type", async () => {
            const ctx = await setup({
                server: baseServer({ mutualizationType: "Other" }),
            });
            const close = spyOn(ctx.component, "close");

            await ctx.component.nextStep();

            expect(ctx.digitalServiceBusiness.submitServerForm).not.toHaveBeenCalled();
            expect(ctx.router.navigate).not.toHaveBeenCalled();
            expect(close).not.toHaveBeenCalled();
        });
    });

    describe("close", () => {
        it("should reset the server and close the panel", () => {
            component.close();

            expect(digitalServiceStore.setServer).toHaveBeenCalledWith(
                {} as DigitalServiceServerConfig,
            );
            expect(digitalServiceBusiness.closePanel).toHaveBeenCalled();
        });
    });

    describe("server computed", () => {
        it("should assign a default host and normalize a fresh server", async () => {
            const serverTypesData: Host[] = [
                {
                    code: 1,
                    value: "Server Compute M",
                    type: "Compute",
                    characteristic: [
                        { code: "vCPU", value: 8 },
                        { code: "annualElectricityConsumption", value: 500 },
                        { code: "lifespan", value: 5 },
                    ],
                },
            ];
            const ctx = await setup({
                server: baseServer({
                    type: "Compute",
                    quantity: -1,
                    datacenter: {} as ServerDC,
                }),
                serverTypes: serverTypesData,
                inDatacenters: [{ name: "Default DC", location: "France", pue: 1.5 }],
            });

            const result = ctx.component.server();

            expect(result.host?.value).toBe("Server Compute M");
            expect(result.quantity).toBe(1);
            expect(result.annualOperatingTime).toBe(8760);
            expect(result.datacenter?.name).toBe("Default DC");
            expect(ctx.component.serverTypes()).toEqual(serverTypesData);
            expect(ctx.component.datacenterOptions()).toEqual([
                {
                    location: "France",
                    name: "Default DC",
                    pue: 1.5,
                    displayLabel: undefined,
                    uid: "",
                },
            ]);
        });

        it("should keep an already assigned and known host", async () => {
            const customHost: Host = {
                code: 2,
                value: "Custom Storage Host",
                type: "Storage",
                characteristic: [{ code: "disk", value: 400 }],
            };
            const ctx = await setup({
                server: baseServer({
                    id: 5,
                    type: "Storage",
                    quantity: 3,
                    host: customHost,
                    totalDisk: 999,
                    annualElectricConsumption: 111,
                    lifespan: 7,
                    datacenter: { name: "Existing DC", location: "France", pue: 1 },
                }),
                serverTypes: [customHost],
                inDatacenters: [{ name: "Existing DC", location: "France", pue: 1 }],
            });

            const result = ctx.component.server();

            expect(result.host).toBe(customHost);
            expect(result.quantity).toBe(3);
            expect(result.totalDisk).toBe(999);
            expect(result.datacenter?.name).toBe("Existing DC");
        });
    });

    describe("createLabelKey", () => {
        it("should return common.add for a new Dedicated server", async () => {
            const ctx = await setup({
                server: baseServer({ mutualizationType: "Dedicated" }),
            });
            expect(ctx.component.createLabelKey()).toBe("common.add");
        });

        it("should return common.save for an existing Dedicated server", async () => {
            const ctx = await setup({
                server: baseServer({ mutualizationType: "Dedicated", id: 1 }),
            });
            expect(ctx.component.createLabelKey()).toBe("common.save");
        });

        it("should return common.next for a Shared server", async () => {
            const ctx = await setup({
                server: baseServer({ mutualizationType: "Shared" }),
            });
            expect(ctx.component.createLabelKey()).toBe("common.next");
        });

        it("should return common.add as a fallback for any other mutualization type", async () => {
            const ctx = await setup({
                server: baseServer({ mutualizationType: "Other" }),
            });
            expect(ctx.component.createLabelKey()).toBe("common.add");
        });
    });
});
