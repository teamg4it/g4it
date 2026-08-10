/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    ChangeDetectionStrategy,
    Component,
    computed,
    effect,
    inject,
    input,
    signal,
} from "@angular/core";
import { TranslatePipe, TranslateService } from "@ngx-translate/core";
import { CheckboxChangeEvent, CheckboxModule } from "primeng/checkbox";
import { Filter, TransformedDomain } from "src/app/core/interfaces/filter.interface";
import { FilterService } from "src/app/core/service/business/filter.service";

import { ScrollingModule } from "@angular/cdk/scrolling";
import { NgClass } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { AccordionModule } from "primeng/accordion";
import { Button } from "primeng/button";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import { BaseFilterSidebarComponent } from "../../base-filter-sidebar/base-filter-sidebar.component";
import {
    getSimpleViewportHeight,
    getTreeViewportHeight,
    isFilterActive,
} from "../../filter-helpers";

@Component({
    selector: "dataviz-filter-application",
    templateUrl: "./dataviz-filter-application.component.html",
    styleUrl: "./dataviz-filter-application.component.scss",
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        Button,
        BaseFilterSidebarComponent,
        AccordionModule,
        CheckboxModule,
        FormsModule,
        NgClass,
        TranslatePipe,
        ScrollingModule,
    ],
})
export class DatavizFilterApplicationComponent {
    allFilters = input<Filter<string | TransformedDomain>>({});
    allUnusedFilters = signal<Filter<TransformedDomain>>({});
    localFilters = signal<Filter<string | TransformedDomain>>({});
    private readonly filterService = inject(FilterService);
    private readonly translate = inject(TranslateService);
    protected footprintStore = inject(FootprintStoreService);
    private readonly globalStore = inject(GlobalStoreService);
    filterSidebarVisible = false;

    constructor() {
        // React to allFilters changes using effect
        effect(() => {
            const filters = this.allFilters();
            if (filters && Object.keys(filters).length > 0) {
                this.selectedFilters();
            }
        });
    }

    overlayVisible: boolean = false;
    tabs = Constants.APPLICATION_FILTERS;
    all = Constants.ALL;
    empty = Constants.EMPTY;

    selectedFilterNames = computed(() => {
        const filters = this.footprintStore.applicationSelectedFilters();
        return Object.keys(filters).filter((tab) => this.filterActive(filters[tab]));
    });

    isFilterApplied = computed(() => {
        const filtersToCheck = this.localFilters();
        const selectedFiltersArr = Object.keys(filtersToCheck);
        return selectedFiltersArr.reduce(
            (acc, key) => {
                acc[key] = this.filterActive(filtersToCheck[key]) ?? false;
                return acc;
            },
            {} as Record<string, boolean>,
        );
    });

    selectedFilters() {
        // Initialize with all available filters when data changes
        // No cloning needed - just pass input directly to store
        this.footprintStore.setApplicationSelectedFilters(this.allFilters());
    }

    // TrackBy functions for better performance
    trackByIndex(index: number): number {
        return index;
    }

    trackByLabel(index: number, item: TransformedDomain | string): string {
        return typeof item === "string" ? item : item.label;
    }

    // Use shared viewport height calculations
    getSimpleViewportHeight(field: string): string {
        return getSimpleViewportHeight(this.allUnusedFilters()[field]);
    }

    getTreeViewportHeight(field: string): string {
        return getTreeViewportHeight(this.allUnusedFilters()[field]);
    }

    filterActive(filter: any) {
        return isFilterActive(filter);
    }

    onFilterSelected(selectedValues: string[], tab: string, selection: string) {
        const f = { ...this.localFilters() };
        f[tab] = this.filterService.getUpdateSelectedValues(
            selectedValues,
            this.allFilters()[tab] as string[],
            selection,
        );
        this.localFilters.set(f);
    }

    onTreeChange(event: CheckboxChangeEvent, item: TransformedDomain) {
        // Update signal with mutated tree for reactive change detection
        this.allUnusedFilters.update((filters) => {
            if (item.label === Constants.ALL) {
                for (const domain of filters["domain"]) {
                    domain.checked = event.checked;
                    for (const child of domain["children"] ?? []) {
                        child.checked = event.checked;
                    }
                }
            } else {
                for (const child of item["children"] ?? []) {
                    child.checked = event.checked;
                }
            }
            return filters;
        });
        this.setAllCheckBox();
    }

    onTreeChildChanged(event: CheckboxChangeEvent, item: TransformedDomain) {
        // Update signal with mutated tree for reactive change detection
        this.allUnusedFilters.update((filters) => {
            if (item.children?.some((child) => child.checked)) {
                item.checked = true;
            } else {
                item.checked = false;
            }
            return filters;
        });
        this.setAllCheckBox();
    }

    setAllCheckBox(): void {
        if (this.checkIfAllNotCheck()) {
            this.setAllCheckBoxValue(false);
        } else {
            this.setAllCheckBoxValue(true);
        }
    }

    setAllCheckBoxValue(checked: boolean): void {
        // Update signal with mutated "All" checkbox
        this.allUnusedFilters.update((filters) => {
            const allItem = filters["domain"]?.find(
                (domain) => domain.label === Constants.ALL,
            );
            if (allItem) {
                allItem.checked = checked;
            }
            return filters;
        });
    }

    checkIfAllNotCheck(): boolean {
        return this.allUnusedFilters()
            ["domain"].filter((domain) => domain.label !== Constants.ALL)
            .some(
                (domain) =>
                    !domain.checked || domain.children.some((child) => !child.checked),
            );
    }

    openFilterSidebar(): void {
        // Show sidebar immediately
        this.filterSidebarVisible = true;

        // Clone only once for editing session - allows discard on cancel
        const clonedFilters = structuredClone(
            this.allFilters(),
        ) as Filter<TransformedDomain>;

        // Collapse domains for better UX
        if (clonedFilters["domain"]) {
            for (const domain of clonedFilters["domain"]) {
                domain.collapsed = true;
            }
        }

        // Get current selection state from store
        const currentFilters = this.footprintStore.applicationSelectedFilters();

        // For tree-based filters (domain), merge the checked state
        if (clonedFilters["domain"] && currentFilters["domain"]) {
            clonedFilters["domain"] = this.mergeFilterState(
                clonedFilters["domain"],
                currentFilters["domain"] as TransformedDomain[],
            );
        }

        // Set signal with cloned working copy
        this.allUnusedFilters.set(clonedFilters);
        this.localFilters.set({ ...currentFilters });
    }

    private mergeFilterState(
        allFilters: TransformedDomain[],
        selectedFilters: TransformedDomain[],
    ): TransformedDomain[] {
        return allFilters.map((domain) => {
            const selected = selectedFilters.find((s) => s.label === domain.label);
            if (selected) {
                const mergedChildren =
                    domain.children?.map((child) => {
                        const selectedChild = selected.children?.find(
                            (sc) => sc.label === child.label,
                        );
                        return {
                            ...child,
                            checked: selectedChild?.checked ?? true,
                        };
                    }) ?? [];

                return {
                    ...domain,
                    checked: selected.checked ?? true,
                    children: mergedChildren,
                };
            }
            return domain;
        });
    }

    closeFilterSidebar(): void {
        // Discard changes and reset
        this.filterSidebarVisible = false;
        this.localFilters.set({});
        this.allUnusedFilters.set({});
    }

    applyFilters(): void {
        // Show loader and close sidebar immediately for responsive UX
        this.globalStore.setLoading(true);
        this.filterSidebarVisible = false;

        // Prepare final filters - sync tree state from signal
        const workingFilters = this.allUnusedFilters();
        const finalFilters = workingFilters["domain"]
            ? { ...this.localFilters(), domain: workingFilters["domain"] }
            : this.localFilters();

        // Defer store update to unblock UI and allow loader/sidebar close to render
        setTimeout(() => {
            // Update store - triggers 17+ computed signals across components
            this.footprintStore.setApplicationSelectedFilters(finalFilters);
            this.localFilters.set({});
            this.allUnusedFilters.set({});

            // Keep loader visible while downstream computations complete
            setTimeout(() => this.globalStore.setLoading(false), 10);
        }, 10);
    }
}
