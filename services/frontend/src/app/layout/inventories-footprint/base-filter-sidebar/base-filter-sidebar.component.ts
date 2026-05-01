/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, EventEmitter, Input, Output, signal } from "@angular/core";
import { Filter } from "src/app/core/interfaces/filter.interface";
import { Constants } from "src/constants";
import {
    getActiveFilterNames,
    isFilterActive,
    mapFilterActiveStatus,
} from "../filter-helpers";

export type FilterTab = string | { field: string; children?: any[] };

@Component({
    selector: "app-base-filter-sidebar",
    templateUrl: "./base-filter-sidebar.component.html",
    styleUrls: ["./base-filter-sidebar.component.scss"],
})
export class BaseFilterSidebarComponent {
    // Inputs
    @Input() visible = false;
    @Input() tabs: FilterTab[] = [];
    @Input() allFilters: Filter<any> = {};
    @Input() localFilters = signal<Filter<any>>({});
    @Input() selectedFilterCount = 0;
    @Input() translationKeyPrefix = "inventories-footprint.filter-tabs";
    @Input() hasTreeStructure = false;
    @Input() useCustomContent = false; // When true, uses ng-content instead of default template

    // Outputs
    @Output() visibleChange = new EventEmitter<boolean>();
    @Output() filtersApplied = new EventEmitter<Filter<any>>();
    @Output() filtersCancelled = new EventEmitter<void>();
    @Output() checkboxChanged = new EventEmitter<{
        selectedValues: any[];
        tab: string;
        selection: string;
    }>();
    @Output() treeChanged = new EventEmitter<{ event: any; item: any }>();
    @Output() treeChildChanged = new EventEmitter<{ event: any; item: any }>();

    all = Constants.ALL;
    empty = Constants.EMPTY;

    localSelectedFilterNames = computed(() => {
        return getActiveFilterNames(this.localFilters());
    });

    isFilterApplied = computed(() => {
        return mapFilterActiveStatus(this.localFilters());
    });

    filterActive(filter: any) {
        return isFilterActive(filter);
    }

    getTabField(tab: FilterTab): string {
        return typeof tab === "string" ? tab : tab.field;
    }

    hasChildren(tab: FilterTab): boolean {
        return typeof tab !== "string" && !!tab.children?.length;
    }

    closeFilterSidebar(): void {
        this.visible = false;
        this.visibleChange.emit(false);
        this.filtersCancelled.emit();
    }

    applyFilters(): void {
        this.filtersApplied.emit(this.localFilters());
        this.visible = false;
        this.visibleChange.emit(false);
    }

    onCheckboxChange(selectedValues: any[], tab: string, selection: string): void {
        this.checkboxChanged.emit({ selectedValues, tab, selection });
    }

    onTreeChange(event: any, item: any): void {
        this.treeChanged.emit({ event, item });
    }

    onTreeChildChanged(event: any, item: any): void {
        this.treeChildChanged.emit({ event, item });
    }
}
