/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    Component,
    computed,
    inject,
    Input,
    OnChanges,
    signal,
    SimpleChanges,
} from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { CheckboxChangeEvent } from "primeng/checkbox";
import { Filter, TransformedDomain } from "src/app/core/interfaces/filter.interface";
import { FilterService } from "src/app/core/service/business/filter.service";

import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { Constants } from "src/constants";

@Component({
    selector: "dataviz-filter-application",
    templateUrl: "./dataviz-filter-application.component.html",
    styleUrl: "./dataviz-filter-application.component.scss",
})
export class DatavizFilterApplicationComponent implements OnChanges {
    @Input() allFilters: Filter<string | TransformedDomain> = {};
    allUnusedFilters: Filter<TransformedDomain> = {};
    localFilters = signal<Filter<string | TransformedDomain>>({});
    private readonly filterService = inject(FilterService);
    private readonly translate = inject(TranslateService);
    protected footprintStore = inject(FootprintStoreService);
    filterSidebarVisible = false;

    overlayVisible: boolean = false;
    tabs = Constants.APPLICATION_FILTERS;
    all = Constants.ALL;
    empty = Constants.EMPTY;

    selectedFilterNames = computed(() => {
        const filters = this.footprintStore.applicationSelectedFilters();
        return Object.keys(filters).filter((tab) => this.filterActive(filters[tab]));
    });

    localSelectedFilterNames = computed(() => {
        const filters = this.localFilters();
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

    ngOnChanges(changes: SimpleChanges) {
        if (changes["allFilters"]) {
            this.selectedFilters();
        }
    }

    selectedFilters() {
        this.allUnusedFilters = structuredClone(
            this.allFilters,
        ) as Filter<TransformedDomain>;
        this.footprintStore.setApplicationSelectedFilters(this.allUnusedFilters);
    }

    filterActive(filter: any) {
        return (
            filter?.length === 0 ||
            (typeof filter?.[0] === "object" && filter?.[0]?.["checked"] === false) ||
            (typeof filter?.[0] === "string" && !filter?.includes("All"))
        );
    }

    onFilterSelected(selectedValues: string[], tab: string, selection: string) {
        const f = { ...this.localFilters() };
        f[tab] = this.filterService.getUpdateSelectedValues(
            selectedValues,
            this.allFilters[tab] as string[],
            selection,
        );
        this.localFilters.set(f);
    }

    onTreeChange(event: CheckboxChangeEvent, item: TransformedDomain) {
        if (item.label === Constants.ALL) {
            for (const domain of this.allUnusedFilters["domain"]) {
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
        this.setAllCheckBox();
        const f = { ...this.localFilters() };
        f["domain"] = this.allUnusedFilters["domain"];
        this.localFilters.set(f);
    }

    onTreeChildChanged(event: CheckboxChangeEvent, item: TransformedDomain) {
        if (item.children?.some((child) => child.checked)) {
            item.checked = true;
        } else {
            item.checked = false;
        }
        this.setAllCheckBox();
        const f = { ...this.localFilters() };
        f["domain"] = this.allUnusedFilters["domain"];
        this.localFilters.set(f);
    }

    setAllCheckBox(): void {
        if (this.checkIfAllNotCheck()) {
            this.setAllCheckBoxValue(false);
        } else {
            this.setAllCheckBoxValue(true);
        }
    }

    setAllCheckBoxValue(checked: boolean): void {
        this.allUnusedFilters["domain"] = this.allUnusedFilters["domain"].map(
            (domain) => {
                if (domain.label === Constants.ALL) {
                    return { ...domain, checked };
                } else {
                    return domain;
                }
            },
        );
    }

    checkIfAllNotCheck(): boolean {
        return this.allUnusedFilters["domain"]
            .filter((domain) => domain.label !== Constants.ALL)
            .some(
                (domain) =>
                    !domain.checked || domain.children.some((child) => !child.checked),
            );
    }

    openFilterSidebar(): void {
        // Create a deep copy of current filters from store
        this.localFilters.set(
            structuredClone(this.footprintStore.applicationSelectedFilters()),
        );
        this.allUnusedFilters = structuredClone(
            this.allFilters,
        ) as Filter<TransformedDomain>;
        this.filterSidebarVisible = true;
    }

    closeFilterSidebar(): void {
        // Discard changes and reset
        this.filterSidebarVisible = false;
        this.localFilters.set({});
        this.allUnusedFilters = {};
    }

    applyFilters(): void {
        // Save local changes to store
        const filters = this.localFilters();
        this.footprintStore.setApplicationSelectedFilters(filters);
        this.filterSidebarVisible = false;
        this.localFilters.set({});
    }
}
