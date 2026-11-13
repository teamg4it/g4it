/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Injectable } from "@angular/core";
import { createStore, select, setProp, withProps } from "@ngneat/elf";
import { selectAllEntities, setEntities, withEntities } from "@ngneat/elf-entities";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";

interface InventoryProps {
    selected?: string;
}

const inventoryStore = createStore(
    { name: "Inventory" },
    withProps<InventoryProps>({ selected: "" }),
    withEntities<Inventory, "id">()
);

@Injectable({ providedIn: "root" })
export class InventoryRepository {
    inventories$ = inventoryStore.pipe(selectAllEntities());
    selectedInventory$ = inventoryStore.pipe(select((state) => state.selected));

    updateSelectedInventory(id: any) {
        inventoryStore.update(setProp("selected", id));
    }

    setInventories(inventories: Inventory[]) {
        const inventoriesWithIds = inventories.map((inventory) => ({
            ...inventory,
            id: inventory.id,
        }));
        inventoryStore.update(setEntities(inventoriesWithIds));
    }
}
