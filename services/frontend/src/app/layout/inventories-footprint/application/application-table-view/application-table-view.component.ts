import { Component, computed, effect, inject, input, signal } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { GraphDescriptionContent } from "src/app/core/interfaces/digital-service.interfaces";
import { ApplicationFiltersRest, Filter } from "src/app/core/interfaces/filter.interface";
import {
    ApplicationFootprint,
    ApplicationImpact,
    ApplicationIndicatorsPage,
    ApplicationIndicatorsPageRequest,
} from "src/app/core/interfaces/footprint.interface";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { SharedChartsModule } from "src/app/core/shared/common-chart-module";
import { SharedModule } from "src/app/core/shared/shared.module";
import { FootprintStoreService } from "src/app/core/store/footprint.store";

@Component({
    selector: "app-application-table-view",
    standalone: true,
    imports: [SharedModule, SharedChartsModule],
    templateUrl: "./application-table-view.component.html",
})
export class ApplicationTableViewComponent {
    footprint = input<ApplicationFootprint[]>([]);
    isMulticriteria = input<boolean>(false);
    inventoryId = input<number>(0);
    filterMetadata = input<ApplicationFiltersRest | null>(null);

    private readonly footprintStore = inject(FootprintStoreService);
    private readonly filterService = inject(FilterService);
    private readonly footprintService = inject(FootprintService);
    private readonly footprintDataService = inject(FootprintDataService);
    protected readonly translate = inject(TranslateService);
    protected readonly integerPipe = inject(IntegerPipe);
    protected readonly decimalsPipe = inject(DecimalsPipe);
    readonly first = signal(0);
    readonly rows = signal(10);
    readonly sortField = signal("sip");
    readonly sortOrder = signal<-1 | 1>(-1);
    readonly loading = signal(false);
    private readonly pageData = signal<ApplicationIndicatorsPage>({
        content: [],
        pageNumber: 0,
        pageSize: 10,
        totalElements: 0,
        totalPages: 0,
    });
    readonly totalRecords = computed(() => this.pageData().totalElements);
    readonly tableView = computed(() =>
        this.pageData().content.map((row) => {
            const criteria = this.translate.instant(`criteria.${row.criteria}`);
            return {
                ...row,
                criteria: criteria.title ?? row.criteria,
                unit: row.unit ?? criteria.unite,
            };
        }),
    );
    private readonly pageFilters = computed<ApplicationIndicatorsPageRequest>(() => {
        const metadata = this.filterMetadata();
        const filters: ApplicationIndicatorsPageRequest = metadata
            ? this.footprintService.mapApplicationSummaryFilters(
                  this.footprintStore.applicationSelectedFilters(),
                  metadata,
              )
            : {};
        const criterion = this.footprintStore.applicationCriteria();
        if (!this.isMulticriteria() && criterion) filters.criterion = [criterion];
        return filters;
    });
    private previousFiltersKey?: string;

    private readonly loadPageEffect = effect((onCleanup) => {
        const inventoryId = this.inventoryId();
        const filters = this.pageFilters();
        const filtersKey = JSON.stringify(filters);
        if (
            this.previousFiltersKey !== undefined &&
            this.previousFiltersKey !== filtersKey
        ) {
            this.first.set(0);
        }
        this.previousFiltersKey = filtersKey;
        const first = this.first();
        const rows = this.rows();
        const sortField = this.sortField();
        const sortDirection = this.sortOrder() === 1 ? "asc" : "desc";
        if (!inventoryId) return;

        this.loading.set(true);
        const subscription = this.footprintDataService
            .getApplicationIndicatorsPage(
                inventoryId,
                filters,
                Math.floor(first / rows),
                rows,
                sortField,
                sortDirection,
            )
            .subscribe({
                next: (page) => {
                    this.pageData.set(page);
                    this.loading.set(false);
                },
                error: () => {
                    this.pageData.set({
                        content: [],
                        pageNumber: Math.floor(first / rows),
                        pageSize: rows,
                        totalElements: 0,
                        totalPages: 0,
                    });
                    this.loading.set(false);
                },
            });
        onCleanup(() => subscription.unsubscribe());
    });

    onPageChange(event: {
        first?: number | null;
        rows?: number | null;
        sortField?: string | string[] | null;
        sortOrder?: number | null;
    }): void {
        const rows = event.rows ?? this.rows();
        const first = event.first ?? 0;
        const sortField = typeof event.sortField === "string" ? event.sortField : "sip";
        const sortOrder = event.sortOrder === 1 ? 1 : -1;
        if (sortField !== this.sortField() || sortOrder !== this.sortOrder()) {
            this.first.set(0);
        } else {
            this.first.set(first);
        }
        this.rows.set(rows);
        this.sortField.set(sortField);
        this.sortOrder.set(sortOrder);
    }

    filterImpacts(selectedFilters: Filter, footprint: ApplicationFootprint[]) {
        let data: ApplicationImpact[] = [];
        for (const footprintItem of footprint) {
            for (const impact of footprintItem?.impacts ?? []) {
                if (this.filterService.getFilterincludes(selectedFilters, impact)) {
                    const ifExists = data.find(
                        (d) =>
                            d.applicationName === impact.applicationName &&
                            d.criteria === footprintItem.criteria,
                    );
                    if (ifExists) {
                        ifExists.sip += impact.sip;
                        ifExists.impact += impact.impact;
                    } else {
                        data.push({ ...impact, criteria: footprintItem.criteria });
                    }
                }
            }
        }
        return data.map((d) => {
            const criteria = this.translate.instant("criteria." + d.criteria);
            return {
                applicationName: d.applicationName,
                criteria: criteria.title,
                sip: d.sip,
                impact: d.impact,
                unit: criteria.unite,
            };
        });
    }

    getContentText = computed((): GraphDescriptionContent => {
        let translationKey: string;
        let textResourceDescription: string = "";
        let descriptionData = "";
        let key = "";
        translationKey = `ds-graph-description.global-vision.`;
        descriptionData = this.translate.instant(`${translationKey}description`, {
            criteria: this.footprint()
                .map(
                    (impact) =>
                        this.translate.instant(`criteria.${impact.criteria}`).title,
                )
                .join(", "),
            module: this.translate.instant("ds-graph-module.inventory"),
        });
        key = translationKey;

        return {
            description: descriptionData,
            scale: this.translate.instant(`${key}scale`),
            analysis: this.translate.instant(
                `ds-graph-description.global-vision.analysis`,
                {
                    module: this.translate.instant("ds-graph-module.inventory"),
                },
            ),
            toGoFurther: this.translate.instant(
                `ds-graph-description.global-vision.inventory-to-go-further`,
            ),
        };
    });

    trackByRow(index: number, item: any): string {
        return `${item.applicationName}-${item.criteria}`;
    }
}
