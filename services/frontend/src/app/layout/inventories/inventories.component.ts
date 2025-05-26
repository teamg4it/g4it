/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, DestroyRef, inject, OnInit, ViewChild } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Event, NavigationEnd, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { Subject, takeUntil } from "rxjs";
import { sortByProperty } from "sort-by-property";
import {
    Inventory,
    InventoryCriteriaRest,
} from "src/app/core/interfaces/inventory.interfaces";
import { Note } from "src/app/core/interfaces/note.interface";
import { Role } from "src/app/core/interfaces/roles.interfaces";
import { Organization } from "src/app/core/interfaces/user.interfaces";
import { InventoryService } from "src/app/core/service/business/inventory.service";
import { UserService } from "src/app/core/service/business/user.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import { FilePanelComponent } from "./file-panel/file-panel.component";

@Component({
    selector: "app-inventories",
    templateUrl: "./inventories.component.html",
    providers: [ConfirmationService, MessageService],
})
export class InventoriesComponent implements OnInit {
    private destroyRef = inject(DestroyRef);
    private global = inject(GlobalStoreService);

    @ViewChild(FilePanelComponent) filePanelComponent: FilePanelComponent | undefined;
    sidebarVisible: boolean = false;
    sidebarPurpose: string = "";
    sidebarType = "FILE"; // or NOTE
    id: number = 0;
    name: any = "";
    inventories: Map<string, Inventory[]> = new Map();
    inventoriesForSimulationsAll: Inventory[] = [];
    inventoriesOpen: Set<number> = new Set();
    inventoryBlocksOpen: Set<string> = new Set();
    filterMode: any;
    ngUnsubscribe = new Subject<void>();
    inventoryInterval: any;
    inventoriesToReload: Set<number> = new Set();
    waitingLoop = 10000;
    doLoop = true;
    enableSearchField = true;
    searchFieldTouched = true;
    selectedInventory: Inventory = {} as Inventory;
    selectedOrganization!: string;
    isAllowedInventory: boolean = false;

    constructor(
        private inventoryService: InventoryService,
        public router: Router,
        private messageService: MessageService,
        private translate: TranslateService,
        public userService: UserService,
    ) {}

    async ngOnInit() {
        this.userService.currentOrganization$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((organization: Organization) => {
                this.selectedOrganization = organization.name;
            });

        this.userService.roles$.subscribe((roles: Role[]) => {
            this.isAllowedInventory =
                roles.includes(Role.InventoryRead) || roles.includes(Role.InventoryWrite);
        });
        this.inventoriesOpen = localStorage.getItem("inventoriesOpen")
            ? new Set(
                  localStorage
                      .getItem("inventoriesOpen")
                      ?.split(",")
                      .filter((v) => v !== "NaN")
                      .map((v) => parseInt(v)),
              )
            : new Set();

        if (localStorage.getItem("inventoryBlocksOpen") == null) {
            this.inventoryBlocksOpen = new Set([
                Constants.INVENTORY_TYPE.INFORMATION_SYSTEM,
                Constants.INVENTORY_TYPE.SIMULATION,
            ]);
            this.updateLocalStorageBlock();
        } else {
            this.inventoryBlocksOpen = new Set(
                localStorage.getItem("inventoryBlocksOpen")?.split(","),
            );
        }

        await this.reloadInventories();
        if (this.doLoop) {
            this.loopLoadInventories();
        }

        this.router.events
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((event: Event) => {
                if (event instanceof NavigationEnd) {
                    clearInterval(this.inventoryInterval);
                    if (event.url.includes("/footprint")) {
                        return;
                    }

                    if (this.isAllowedInventory) {
                        this.reloadInventories().then(() => {
                            if (this.doLoop) {
                                this.loopLoadInventories();
                            }
                        });
                    }
                }
            });
    }

    loopLoadInventories() {
        this.inventoryInterval = setInterval(async () => {
            if (this.inventoriesToReload.size === 0) {
                clearInterval(this.inventoryInterval);
            } else {
                for (const id of this.inventoriesToReload) {
                    await this.reloadInventory(id);
                }
            }
        }, this.waitingLoop);
    }

    async reloadInventoriesAndLoop(id: number) {
        await this.reloadInventories();
        this.inventoriesOpen.clear();
        this.inventoriesOpen.add(id);
        this.updateLocalStorage();
        this.loopLoadInventories();
    }

    async reloadInventoryAndLoop(id: number) {
        this.inventoriesToReload.add(id);
        await this.reloadInventory(id);
        this.loopLoadInventories();
    }

    async reloadInventory(id: number) {
        let result = await this.inventoryService.getInventories(id);

        const inventory = result[0];

        if (inventory.type) {
            const index = this.inventories
                .get(inventory.type)
                ?.findIndex((inventory) => inventory.id === id);

            if (index !== undefined && index > -1) {
                this.inventories.get(inventory.type)![index] = inventory;
            }
        }

        this.setInventoryToReload(inventory);
    }

    async reloadInventories() {
        this.global.setLoading(true);

        const allInventories = await this.inventoryService.getInventories();

        this.inventoriesToReload = new Set();
        allInventories.forEach((inventory) => {
            this.setInventoryToReload(inventory);
        });

        this.inventoriesForSimulationsAll = allInventories
            .filter(
                (inventory: Inventory) =>
                    inventory.type === Constants.INVENTORY_TYPE.SIMULATION,
            )
            .sort(sortByProperty("name", "asc"));

        this.inventories.set(
            Constants.INVENTORY_TYPE.INFORMATION_SYSTEM,
            allInventories
                .filter(
                    (inventory: Inventory) =>
                        inventory.type === Constants.INVENTORY_TYPE.INFORMATION_SYSTEM,
                )
                .sort(sortByProperty("date", "desc")),
        );
        this.inventories.set(Constants.INVENTORY_TYPE.SIMULATION, [
            ...this.inventoriesForSimulationsAll,
        ]);

        this.global.setLoading(false);
    }

    setInventoryToReload(inventory: Inventory) {
        let doAddTaskLoading = false;
        let doAddTaskEvaluating = false;

        if (inventory.lastTaskLoading) {
            doAddTaskLoading =
                !Constants.EVALUATION_BATCH_COMPLETED_FAILED_STATUSES.includes(
                    inventory.lastTaskLoading.status,
                );
        }

        if (inventory.lastTaskEvaluating) {
            doAddTaskEvaluating =
                !Constants.EVALUATION_BATCH_COMPLETED_FAILED_STATUSES.includes(
                    inventory.lastTaskEvaluating.status,
                );
        }

        if (doAddTaskLoading || doAddTaskEvaluating) {
            this.inventoriesToReload.add(inventory.id);
        } else if (!doAddTaskLoading && !doAddTaskEvaluating) {
            this.inventoriesToReload.delete(inventory.id);
        }
    }

    openSidebarForUploadInventory(id: number) {
        this.sidebarVisible = true;
        this.sidebarType = "FILE";
        this.sidebarPurpose = "upload";
        this.id = id;

        for (let value of this.inventories.values()) {
            const inventory = value.find((inventory) => inventory.id === id);
            if (inventory) {
                this.name = inventory.name;
                break;
            }
        }
    }

    openSidebarForNote(id: number) {
        this.sidebarVisible = true;
        this.sidebarType = "NOTE";
        this.id = id;

        for (let value of this.inventories.values()) {
            const inventory = value.find((inventory) => inventory.id === id);
            if (inventory) {
                this.selectedInventory = inventory;
                break;
            }
        }
    }

    noteSaveValue(event: any) {
        this.selectedInventory.note = {
            content: event,
        } as Note;

        this.inventoryService
            .updateInventory(this.selectedInventory)
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((res) => {
                this.sidebarVisible = false;
                this.messageService.add({
                    severity: "success",
                    summary: this.translate.instant("common.note.save"),
                    sticky: false,
                });
            });
    }

    noteDelete(event: any) {
        this.selectedInventory.note = undefined;
        this.inventoryService
            .updateInventory(this.selectedInventory)
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((res) => {
                this.messageService.add({
                    severity: "success",
                    summary: this.translate.instant("common.note.delete"),
                    sticky: false,
                });
                this.reloadInventory(this.selectedInventory.id);
            });
    }

    trackByFn(index: any) {
        return index;
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
        clearInterval(this.inventoryInterval);
    }

    searchList() {
        this.searchFieldTouched = true;
        if (this.filterMode) {
            const filteredData = this.inventoriesForSimulationsAll.filter((value) => {
                const searchStr = this.filterMode.toLowerCase();
                return value.name.toLowerCase().includes(searchStr);
            });
            this.inventories.set(Constants.INVENTORY_TYPE.SIMULATION, filteredData);
        } else {
            this.inventories.set(Constants.INVENTORY_TYPE.SIMULATION, [
                ...this.inventoriesForSimulationsAll,
            ]);
        }
    }

    onClick($event: MouseEvent) {
        $event.stopPropagation();
    }

    openTab(event: any, inventoryType: string) {
        this.enableSearchField = true;
        this.inventoryBlocksOpen.add(inventoryType);
        this.updateLocalStorageBlock();
    }

    closeTab(event: any, inventoryType: string) {
        this.inventoryBlocksOpen.delete(inventoryType);
        this.enableSearchField = false;
        this.updateLocalStorageBlock();
    }

    childOpenTab(event: any) {
        this.inventoriesOpen.add(event);
        this.updateLocalStorage();
    }

    childCloseTab(event: any) {
        this.inventoriesOpen.delete(event);
        this.updateLocalStorage();
    }

    updateLocalStorage() {
        localStorage.setItem("inventoriesOpen", [...this.inventoriesOpen].join(","));
    }
    updateLocalStorageBlock() {
        localStorage.setItem(
            "inventoryBlocksOpen",
            [...this.inventoryBlocksOpen].join(","),
        );
    }

    handleSaveInventory(inventoryCriteria: InventoryCriteriaRest, type: string) {
        this.inventoryService
            .updateInventoryCriteria(inventoryCriteria)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((res: Inventory) => {
                let inventoryArray = this.inventories.get(type);
                if (inventoryArray) {
                    let findCriteriaIndex = inventoryArray.findIndex(
                        (inv) => inv.id === inventoryCriteria.id,
                    );
                    // -1 means no criteria satisfy the condition
                    if (findCriteriaIndex !== -1) {
                        inventoryArray[findCriteriaIndex] = {
                            ...inventoryArray[findCriteriaIndex],
                            ...res,
                        };
                        this.inventories.set(type, inventoryArray);
                    }
                }
            });
    }
}
