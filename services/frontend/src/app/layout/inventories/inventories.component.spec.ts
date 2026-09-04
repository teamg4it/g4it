/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from "@angular/core/testing";
import { ActivatedRoute, Event, NavigationEnd, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { BehaviorSubject, Subject, of } from "rxjs";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { Role } from "src/app/core/interfaces/roles.interfaces";
import { Workspace } from "src/app/core/interfaces/user.interfaces";
import { InventoryService } from "src/app/core/service/business/inventory.service";
import { UserService } from "src/app/core/service/business/user.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import { InventoriesComponent } from "./inventories.component";

describe("InventoriesComponent", () => {
    let component: InventoriesComponent;
    let fixture: ComponentFixture<InventoriesComponent>;
    let inventories: Inventory[];
    let inventoryService: jasmine.SpyObj<InventoryService>;
    let globalStore: jasmine.SpyObj<GlobalStoreService>;
    let routerEvents: Subject<Event>;
    let currentWorkspace$: BehaviorSubject<Workspace>;
    let roles$: BehaviorSubject<Role[]>;

    const createInventory = (id: number, type: string, name: string): Inventory =>
        ({
            id,
            type,
            name,
            creationDate: new Date(),
            lastUpdateDate: new Date(),
            workspace: "Workspace",
            dataCenterCount: 0,
            physicalEquipmentCount: 0,
            virtualEquipmentCount: 0,
            applicationCount: 0,
            enableDataInconsistency: false,
            tasks: [],
        }) as Inventory;

    beforeEach(async () => {
        localStorage.clear();
        inventories = [
            createInventory(1, Constants.INVENTORY_TYPE.INFORMATION_SYSTEM, "06-2023"),
            createInventory(2, Constants.INVENTORY_TYPE.SIMULATION, "Zulu"),
            createInventory(3, Constants.INVENTORY_TYPE.SIMULATION, "Alpha"),
        ];
        inventoryService = jasmine.createSpyObj("InventoryService", [
            "getInventories",
            "updateInventory",
            "updateInventoryCriteria",
        ]);
        inventoryService.getInventories.and.callFake(async (id?: number) =>
            id === undefined
                ? inventories
                : inventories.filter((inventory) => inventory.id === id),
        );
        inventoryService.updateInventory.and.returnValue(of({} as any));
        inventoryService.updateInventoryCriteria.and.returnValue(of({} as Inventory));
        globalStore = jasmine.createSpyObj("GlobalStoreService", ["setLoading"]);
        routerEvents = new Subject<Event>();
        currentWorkspace$ = new BehaviorSubject({ name: "Workspace" } as Workspace);
        roles$ = new BehaviorSubject<Role[]>([Role.InventoryRead]);

        await TestBed.configureTestingModule({
            imports: [InventoriesComponent],
            providers: [
                { provide: InventoryService, useValue: inventoryService },
                { provide: GlobalStoreService, useValue: globalStore },
                {
                    provide: Router,
                    useValue: { events: routerEvents, url: "/inventories" },
                },
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            queryParamMap: {
                                get: (key: string) =>
                                    ({ renew: "true", inventoryId: "2" })[key] ?? null,
                            },
                        },
                    },
                },
                { provide: UserService, useValue: { currentWorkspace$, roles$ } },
                {
                    provide: MessageService,
                    useValue: jasmine.createSpyObj("MessageService", ["add"]),
                },
                {
                    provide: TranslateService,
                    useValue: jasmine.createSpyObj("TranslateService", ["instant"]),
                },
            ],
        })
            .overrideComponent(InventoriesComponent, { set: { template: "" } })
            .compileComponents();

        fixture = TestBed.createComponent(InventoriesComponent);
        component = fixture.componentInstance;
        component.doLoop = false;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize state, persisted panels, inventories, and renew parameters", async () => {
        localStorage.setItem("inventoriesOpen", "1,NaN,3");
        localStorage.setItem("inventoryBlocksOpen", Constants.INVENTORY_TYPE.SIMULATION);

        component.ngOnInit();
        await Promise.resolve();
        await Promise.resolve();

        expect(component.selectedWorkspace).toBe("Workspace");
        expect(component.isAllowedInventory).toBeTrue();
        expect([...component.inventoriesOpen]).toEqual([1, 3]);
        expect([...component.inventoryBlocksOpen]).toEqual([
            Constants.INVENTORY_TYPE.SIMULATION,
        ]);
        expect(
            component.inventoriesForSimulationsAll.map((inventory) => inventory.name),
        ).toEqual(["Alpha", "Zulu"]);
        expect(component.inventoryRenewServicePopup).toBeTrue();
        expect(component.renewInventoryId).toBe(2);
    });

    it("should initialize default inventory blocks when no persisted state exists", async () => {
        component.ngOnInit();
        await Promise.resolve();

        expect([...component.inventoryBlocksOpen]).toEqual([
            Constants.INVENTORY_TYPE.INFORMATION_SYSTEM,
            Constants.INVENTORY_TYPE.SIMULATION,
        ]);
        expect(localStorage.getItem("inventoryBlocksOpen")).toContain(
            Constants.INVENTORY_TYPE.INFORMATION_SYSTEM,
        );
    });

    it("should reload inventories and track only incomplete tasks", async () => {
        inventories[0].lastTaskLoading = { status: "STARTED" } as any;
        inventories[1].lastTaskEvaluating = { status: "COMPLETED" } as any;

        await component.reloadInventories();

        expect(globalStore.setLoading).toHaveBeenCalledWith(true);
        expect(globalStore.setLoading).toHaveBeenCalledWith(false);
        expect([...component.inventoriesToReload]).toEqual([1]);
    });

    it("should replace a reloaded inventory", async () => {
        await component.reloadInventories();
        const updated = { ...inventories[0], name: "Updated" };
        inventoryService.getInventories.and.callFake(async (id?: number) =>
            id === undefined ? inventories : [updated],
        );

        await component.reloadInventory(1);

        expect(
            component.inventories.get(Constants.INVENTORY_TYPE.INFORMATION_SYSTEM)?.[0]
                .name,
        ).toBe("Updated");
    });

    it("should filter and restore simulation inventories", async () => {
        await component.reloadInventories();
        component.filterMode = "alp";
        component.searchList();

        expect(
            component.inventories
                .get(Constants.INVENTORY_TYPE.SIMULATION)
                ?.map((item) => item.name),
        ).toEqual(["Alpha"]);

        component.filterMode = "";
        component.searchList();
        expect(component.inventories.get(Constants.INVENTORY_TYPE.SIMULATION)).toEqual(
            component.inventoriesForSimulationsAll,
        );
    });

    it("should open upload and note sidebars for the selected inventory", async () => {
        await component.reloadInventories();

        component.openSidebarForUploadInventory(2);
        expect(component.sidebarVisible).toBeTrue();
        expect(component.sidebarType).toBe("FILE");
        expect(component.sidebarPurpose).toBe("upload");
        expect(component.name).toBe("Zulu");

        component.openSidebarForNote(1);
        expect(component.sidebarType).toBe("NOTE");
        expect(component.selectedInventory).toBe(inventories[0]);
    });

    it("should save and delete notes", () => {
        const focusNoteButton = spyOn(component, "focusNoteButton");
        component.selectedInventory = inventories[0];

        component.noteSaveValue("Saved note");
        expect(component.selectedInventory.note?.content).toBe("Saved note");
        expect(focusNoteButton).toHaveBeenCalled();
        expect(component.sidebarVisible).toBeFalse();

        const reloadInventory = spyOn(component, "reloadInventory").and.resolveTo();
        component.noteDelete(null);
        expect(component.selectedInventory.note).toBeUndefined();
        expect(reloadInventory).toHaveBeenCalledWith(1);
    });

    it("should persist accordion and inventory open state", () => {
        component.openTab(null, Constants.INVENTORY_TYPE.SIMULATION);
        component.closeTab(null, Constants.INVENTORY_TYPE.SIMULATION);
        component.childOpenTab({ index: "4" });
        component.childCloseTab({ index: "4" });

        expect(component.enableSearchField).toBeFalse();
        expect(
            component.inventoryBlocksOpen.has(Constants.INVENTORY_TYPE.SIMULATION),
        ).toBeFalse();
        expect(localStorage.getItem("inventoriesOpen")).toBe("");
    });

    it("should stop event propagation and return the row index", () => {
        const event = jasmine.createSpyObj<MouseEvent>("MouseEvent", ["stopPropagation"]);

        component.onClick(event);

        expect(event.stopPropagation).toHaveBeenCalled();
        expect(component.trackByFn(4)).toBe(4);
    });

    it("should reload permitted pages after navigation outside a footprint", async () => {
        component.ngOnInit();
        await Promise.resolve();
        await Promise.resolve();
        const reloadInventories = spyOn(component, "reloadInventories").and.resolveTo();
        const loopLoadInventories = spyOn(component, "loopLoadInventories");
        component.doLoop = true;

        routerEvents.next(new NavigationEnd(1, "/inventories", "/inventories"));
        await Promise.resolve();

        expect(reloadInventories).toHaveBeenCalled();
        expect(loopLoadInventories).toHaveBeenCalled();
    });

    it("should focus note and load buttons after their delays", fakeAsync(() => {
        const noteContainer = document.createElement("div");
        noteContainer.id = "note1";
        const loadContainer = document.createElement("div");
        loadContainer.id = "load1";
        const noteButton = document.createElement("button");
        const loadButton = document.createElement("button");
        noteContainer.appendChild(noteButton);
        loadContainer.appendChild(loadButton);
        document.body.append(noteContainer, loadContainer);
        const noteFocus = spyOn(noteButton, "focus");
        const loadFocus = spyOn(loadButton, "focus");
        component.id = 1;
        component.sidebarPurpose = "upload";

        component.focusNoteButton();
        component.focusLoadButton();
        tick(200);

        expect(noteFocus).toHaveBeenCalled();
        expect(loadFocus).toHaveBeenCalled();
        noteContainer.remove();
        loadContainer.remove();
    }));

    it("should clear subscriptions and polling on destruction", () => {
        spyOn(component.ngUnsubscribe, "next");
        spyOn(component.ngUnsubscribe, "complete");

        fixture.destroy();

        expect(component.ngUnsubscribe.next).toHaveBeenCalled();
        expect(component.ngUnsubscribe.complete).toHaveBeenCalled();
    });
});
