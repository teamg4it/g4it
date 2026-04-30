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
import { debounceTime, Subject } from "rxjs";
import { Filter } from "src/app/core/interfaces/filter.interface";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { Constants } from "src/constants";

@Component({
    selector: "dataviz-filter",
    templateUrl: "./dataviz-filter.component.html",
    styleUrl: "./dataviz-filter.component.scss",
})
export class DatavizFilterComponent implements OnChanges {
    protected footprintStore = inject(FootprintStoreService);
    private readonly filterService = inject(FilterService);
    private readonly translate = inject(TranslateService);

    overlayVisible: boolean = false;
    filterSidebarVisible = false;
    localFilters = signal<Filter<string>>({});

    @Input() allFilters: Filter<string> = {};
    tabs = Constants.EQUIPMENT_FILTERS;
    all = Constants.ALL;
    empty = Constants.EMPTY;

    selectedFilterNames = computed(() => {
        const selectedFilters = this.footprintStore.filters();
        console.log(selectedFilters);
        return Object.keys(selectedFilters).filter((tab) =>
            this.filterActive(selectedFilters[tab]),
        );
    });

    localSelectedFilterNames = computed(() => {
        const selectedFilters = this.localFilters();
        return Object.keys(selectedFilters).filter((tab) =>
            this.filterActive(selectedFilters[tab]),
        );
    });

    isFilterApplied = computed(() => {
        const filtersToCheck = this.localFilters();
        const selectedFiltersArr = Object.keys(filtersToCheck);
        console.log(Object.keys(selectedFiltersArr));
        return selectedFiltersArr.reduce(
            (acc, key) => {
                acc[key] = this.filterActive(filtersToCheck[key]) ?? false;
                return acc;
            },
            {} as Record<string, boolean>,
        );
    });

    ngOnChanges(changes: SimpleChanges) {
        console.log("All filters updated:", this.allFilters);
        if (changes["allFilters"]) {
            this.footprintStore.setFilters(this.allFilters);
        }
    }

    ngOnInit() {
        console.log(this.footprintStore.filters());
    }

    filterActive(filter: any) {
        return (
            filter.length === 0 ||
            (typeof filter[0] === "object" && filter[0]["checked"] === false) ||
            (typeof filter[0] === "string" && !filter.includes("All"))
        );
    }

    onFilterSelected(selectedValues: string[], tab: string, selection: string) {
        const updatedFilter = this.filterService.getUpdateSelectedValues(
            selectedValues,
            this.allFilters[tab],
            selection,
        );
        // Update local copy instead of store directly
        const currentFilters = { ...this.localFilters() };
        currentFilters[tab] = updatedFilter;
        this.localFilters.set(currentFilters);
    }

    private readonly checkboxChange$ = new Subject<any>();

    constructor() {
        this.checkboxChange$.pipe(debounceTime(200)).subscribe((change) => {
            this.onFilterSelected(change.selectedValues, change.tab, change.selection);
        });
    }

    onCheckboxChange(selectedValues: string[], tab: string, selection: string): void {
        this.checkboxChange$.next({ selectedValues, tab, selection });
    }

    openFilterSidebar(): void {
        // Create a deep copy of current filters from store
        this.localFilters.set(structuredClone(this.footprintStore.filters()));
        this.filterSidebarVisible = true;
    }

    closeFilterSidebar(): void {
        // Discard changes
        this.filterSidebarVisible = false;
        this.localFilters.set({});
    }

    applyFilters(): void {
        // Save local changes to store
        const filters = this.localFilters();
        Object.keys(filters).forEach((tab) => {
            this.footprintStore.setCustomFilters(filters[tab], tab);
        });
        this.filterSidebarVisible = false;
        this.localFilters.set({});
    }
}
