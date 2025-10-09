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
})
export class DatavizFilterApplicationComponent implements OnChanges {
    @Input() allFilters: Filter<string | TransformedDomain> = {};
    allUnusedFilters: Filter<TransformedDomain> = {};
    private readonly filterService = inject(FilterService);
    private readonly translate = inject(TranslateService);
    protected footprintStore = inject(FootprintStoreService);

    overlayVisible: boolean = false;
    tabs = Constants.APPLICATION_FILTERS;
    all = Constants.ALL;
    empty = Constants.EMPTY;

    selectedFilterNames = computed(() => {
        const filters = this.footprintStore.applicationSelectedFilters();
        return Object.keys(filters)
            .filter((tab) => this.filterActive(filters[tab]))
            .map((tab) =>
                this.translate.instant(
                    `inventories-footprint.filter-tabs-application.${tab}`,
                ),
            )
            .join(", ");
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
            filter.length === 0 ||
            (typeof filter[0] === "object" && filter[0]["checked"] === false) ||
            (typeof filter[0] === "string" && !filter.includes("All"))
        );
    }

    onFilterSelected(selectedValues: string[], tab: string, selection: string) {
        const f = this.footprintStore.applicationSelectedFilters();
        f[tab] = this.filterService.getUpdateSelectedValues(
            selectedValues,
            this.allFilters[tab] as string[],
            selection,
        );
        this.footprintStore.setApplicationSelectedFilters(f);
    }

    onTreeChange(event: CheckboxChangeEvent, item: TransformedDomain) {
        if (item.label === Constants.ALL) {
            this.allUnusedFilters["domain"].forEach((domain) => {
                domain.checked = event.checked;
                domain["children"]?.forEach((child) => (child.checked = event.checked));
            });
        } else {
            item["children"]?.forEach((child) => (child.checked = event.checked));
        }
        this.setAllCheckBox();
        const f = this.footprintStore.applicationSelectedFilters();
        f["domain"] = this.allUnusedFilters["domain"];
        this.footprintStore.setApplicationSelectedFilters(f);
    }

    onTreeChildChanged(event: CheckboxChangeEvent, item: TransformedDomain) {
        if (!item.children?.some((child) => child.checked)) {
            item.checked = false;
        } else {
            item.checked = true;
        }
        this.setAllCheckBox();
        const f = this.footprintStore.applicationSelectedFilters();
        f["domain"] = this.allUnusedFilters["domain"];
        this.footprintStore.setApplicationSelectedFilters(f);
    }

    setAllCheckBox(): void {
        if (!this.checkIfAllNotCheck()) {
            this.setAllCheckBoxValue(true);
        } else {
            this.setAllCheckBoxValue(false);
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
}
