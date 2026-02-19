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
    DestroyRef,
    inject,
    OnInit,
    signal,
    Signal,
    WritableSignal,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MenuItem } from "primeng/api";
import { delay, finalize, firstValueFrom } from "rxjs";
import {
    OrganizationCriteriaRest,
    WorkspaceCriteriaRest,
} from "src/app/core/interfaces/administration.interfaces";
import {
    ConstantApplicationFilter,
    Filter,
    TransformedDomain,
    TransformedDomainItem,
} from "src/app/core/interfaces/filter.interface";
import { ApplicationFootprint } from "src/app/core/interfaces/footprint.interface";
import {
    Inventory,
    InventoryCriteriaRest,
} from "src/app/core/interfaces/inventory.interfaces";
import { Organization, Workspace } from "src/app/core/interfaces/user.interfaces";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { InventoryService } from "src/app/core/service/business/inventory.service";
import { UserService } from "src/app/core/service/business/user.service";
import { EvaluationDataService } from "src/app/core/service/data/evaluation-data.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import { Constants } from "src/constants";

@Component({
    selector: "app-inventories-application-footprint",
    templateUrl: "./inventories-application-footprint.component.html",
})
export class InventoriesApplicationFootprintComponent implements OnInit {
    protected readonly footprintStore = inject(FootprintStoreService);
    private readonly globalStore = inject(GlobalStoreService);
    protected readonly userService = inject(UserService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    private readonly inventoryService = inject(InventoryService);
    private readonly destroyRef = inject(DestroyRef);
    private readonly evaluationService = inject(EvaluationDataService);
    currentLang: string = this.translate.currentLang;
    criteriakeys = Object.keys(this.translate.translations[this.currentLang]["criteria"]);
    private readonly filterService = inject(FilterService);
    inventory: WritableSignal<Inventory> = signal({} as Inventory);

    selectedCriteria: string = "";
    criteres: MenuItem[] = [];
    showTabMenu = false;
    criterias = [
        Constants.MUTLI_CRITERIA,
        ...Object.keys(this.globalStore.criteriaList()),
    ];
    inventoryId = +this.activatedRoute.snapshot.paramMap.get("inventoryId")! || 0;
    multiCriteria = Constants.MUTLI_CRITERIA;
    allUnmodifiedFilters = signal({});
    savedFilers: Filter<string | TransformedDomain> = {};
    filteredFilters: Signal<Filter<string | TransformedDomain>> = computed(() => {
        return this.getFilteredFilters(
            this.allUnmodifiedFilters(),
            this.footprintStore.appGraphType(),
            this.footprintStore.appDomain(),
            this.footprintStore.appSubDomain(),
        );
    });
    footprint = signal<ApplicationFootprint[]>([]);
    criteriaFootprint: ApplicationFootprint = {} as ApplicationFootprint;
    allUnmodifiedFootprint: ApplicationFootprint[] = [];
    filterFields = Constants.APPLICATION_FILTERS;
    selectedUnit: string = "Raw";
    displayPopup = false;
    organization: OrganizationCriteriaRest = { criteria: [] };
    workspace: WorkspaceCriteriaRest = {
        organizationId: 0,
        name: "",
        status: "",
        dataRetentionDays: 0,
        criteriaIs: [],
        criteriaDs: [],
    };
    selectedCriterias: string[] = [];
    filterSidebarVisible = false;

    impacts: Signal<any> = computed(() => {
        const filterImpacts = this.formatLifecycleCriteriaImpact(this.footprint()).map(
            (f) => ({
                ...f,
                impacts: f.impacts.filter((impact) => {
                    return this.filterService.getFilterincludes(
                        this.footprintStore.applicationSelectedFilters(),
                        impact,
                    );
                }),
            }),
        );
        return this.footprintService
            .filterCriteriaImpact(filterImpacts)
            .sort(
                (a, b) =>
                    this.criteriakeys.indexOf(a.name) - this.criteriakeys.indexOf(b.name),
            );
    });

    showDomainByApplication = computed(() => {
        if ((this.allUnmodifiedFilters() as any)["domain"]?.length > 2) {
            const domainSelected: any = this.footprintStore
                .applicationSelectedFilters()
                [
                    "domain"
                ].find((d) => (d as TransformedDomain).label === this.footprintStore.appDomain());
            if (domainSelected?.children.length <= 1) {
                return true;
            }
        }
        return false;
    });

    showBackButton = computed(() => {
        if ((this.allUnmodifiedFilters() as any)["domain"]?.length <= 2) {
            if (
                ((this.allUnmodifiedFilters() as any)["domain"][1] as TransformedDomain)
                    ?.children?.length <= 1 &&
                this.footprintStore.appGraphType() === "subdomain"
            ) {
                return false;
            }

            if (this.footprintStore.appGraphType() === "domain") {
                return false;
            }
        }
        return true;
    });

    constructor(
        private readonly activatedRoute: ActivatedRoute,
        public readonly footprintService: FootprintService,
        private readonly translate: TranslateService,
    ) {}

    ngOnInit() {
        this.asyncInit();
    }
    private async asyncInit() {
        await this.initInventory();
        const criteria = this.activatedRoute.snapshot.paramMap.get("criteria");
        this.selectedCriteria = criteria!;
        this.globalStore.setLoading(true);

        this.getOrganizationAndWorkspace();

        let footprint: ApplicationFootprint[] = [];
        const currentWorkspaceName = (
            await firstValueFrom(this.userService.currentWorkspace$)
        ).name;
        footprint = await firstValueFrom(
            this.footprintService.initApplicationFootprint(
                this.inventoryId,
                currentWorkspaceName,
            ),
        );

        this.mapCriteres(footprint);

        this.footprintStore.setApplicationCriteria(criteria || Constants.MUTLI_CRITERIA);

        this.footprint.set(footprint);
        this.allUnmodifiedFootprint = structuredClone(footprint);
        this.footprint.set(
            this.footprint().map((footprintData) => ({
                ...footprintData,
                unit: this.translate.instant(`criteria.${footprintData.criteria}.unite`),
            })),
        );

        const uniqueFilterSet = this.footprintService.getUniqueValues(
            this.footprint(),
            Constants.APPLICATION_FILTERS,
            false,
        );

        let unmodifyFilter: Filter<string | TransformedDomain> = {};
        for (const filter of Constants.APPLICATION_FILTERS) {
            unmodifyFilter[filter.field] = [
                filter.field === "domain"
                    ? {
                          label: Constants.ALL,
                          checked: true,
                          visible: true,
                          children: [],
                      }
                    : Constants.ALL,
                ...this.getValues(uniqueFilterSet, filter),
            ];
        }
        this.allUnmodifiedFilters.set(unmodifyFilter);
        if ((this.allUnmodifiedFilters() as any)?.domain?.length <= 2) {
            if ((this.allUnmodifiedFilters() as any).domain[1]?.children?.length <= 1) {
                this.footprintStore.setDomain(
                    (this.allUnmodifiedFilters() as any).domain[1]?.label,
                );
                this.footprintStore.setSubDomain(
                    (this.allUnmodifiedFilters() as any).domain[1]?.children[0]?.label,
                );
                this.footprintStore.setGraphType("subdomain");
            } else {
                this.footprintStore.setDomain(
                    (this.allUnmodifiedFilters() as any).domain[1]?.label,
                );
                this.footprintStore.setSubDomain("");
                this.footprintStore.setGraphType("domain");
            }
        } else {
            this.footprintStore.setGraphType("global");
        }
        this.globalStore.setLoading(false);

        // React on criteria url param change
        this.activatedRoute.paramMap.subscribe((params) => {
            const criteria = params.get("criteria")!;
            this.footprintStore.setApplicationCriteria(criteria);
            this.selectedCriteria = criteria;
            if (criteria !== Constants.MUTLI_CRITERIA) {
                this.criteriaFootprint = this.footprint().find(
                    (f) => f.criteria === criteria,
                )!;
            }
        });
    }
    getOrganizationAndWorkspace() {
        this.userService.currentOrganization$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((organization: Organization) => {
                this.organization.criteria = organization.criteria!;
            });
        this.userService.currentWorkspace$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((workspace: Workspace) => {
                this.workspace.organizationId = workspace.organizationId!;
                this.workspace.name = workspace.name;
                this.workspace.status = workspace.status;
                this.workspace.dataRetentionDays = workspace.dataRetentionDays!;
                this.workspace.criteriaIs = workspace.criteriaIs!;
                this.workspace.criteriaDs = workspace.criteriaDs!;
            });
    }

    async initInventory() {
        let result = await this.inventoryService.getInventories(this.inventoryId);
        if (result.length > 0) this.inventory.set(result[0]);
    }

    private mapCriteres(footprint: ApplicationFootprint[]): void {
        this.showTabMenu = true;
        const applicationFootprints: ApplicationFootprint[] = structuredClone(footprint);
        applicationFootprints?.sort((a, b) => {
            return (
                this.criteriakeys.indexOf(a.criteria) -
                this.criteriakeys.indexOf(b.criteria)
            );
        });
        this.criteres = applicationFootprints.map((footprint) => {
            return {
                label: this.translate.instant(`criteria.${footprint.criteria}.title`),
                routerLink: `../${footprint.criteria}`,
                id: `${footprint.criteria}`,
            };
        });

        if (this.criteres.length > 1) {
            this.criteres.unshift({
                label: this.translate.instant("criteria-title.multi-criteria.title"),
                routerLink: `../${Constants.MUTLI_CRITERIA}`,
                id: "multi-criteria",
            });
        }
    }

    private getFilteredFilters(
        unmodifyFilter: Filter<string | TransformedDomain>,
        graphType: string,
        domain: string,
        subdomain: string,
    ): Filter<string | TransformedDomain> {
        let nonModifyFilter = { ...unmodifyFilter };
        if (graphType === "global") {
            return nonModifyFilter;
        }
        if (domain) {
            nonModifyFilter["domain"] = unmodifyFilter["domain"]?.filter(
                (d) =>
                    (d as TransformedDomain)?.label === domain ||
                    (d as TransformedDomain)?.label === Constants.ALL,
            );
        }
        if (subdomain) {
            nonModifyFilter["domain"] = unmodifyFilter["domain"]
                ?.filter(
                    (e) =>
                        (e as TransformedDomain).label === domain ||
                        (e as TransformedDomain).label === Constants.ALL,
                )
                .map((d) => {
                    if (!(d as TransformedDomain)?.label) {
                        return d;
                    }

                    return {
                        ...(d as TransformedDomain),
                        children: (d as TransformedDomain).children.filter(
                            (c: TransformedDomainItem) => c.label === subdomain,
                        ),
                    };
                });
        }
        if (graphType === "application") {
            // copy the footprint so that reference doesnot change the original
            const applicationFootprint: ApplicationFootprint[] = structuredClone(
                this.allUnmodifiedFootprint,
            );
            let criteriaFootprint = applicationFootprint.find(
                (item) => item.criteria === this.footprintStore.applicationCriteria(),
            );

            const appFilterConstant: ConstantApplicationFilter[] =
                Constants.APPLICATION_FILTERS.filter((f) => !f?.children);
            if (criteriaFootprint?.impacts) {
                criteriaFootprint.impacts = criteriaFootprint.impacts.filter(
                    (impact) =>
                        this.footprintStore.appApplication() === impact.applicationName,
                );
            }
            if (criteriaFootprint) {
                const uniqueFilters = this.footprintService.getUniqueValues(
                    [criteriaFootprint],
                    appFilterConstant,
                    false,
                );
                let modifiedFilter: Filter<string | TransformedDomain> = {};
                for (const filter of appFilterConstant) {
                    this.pushFilter(filter, modifiedFilter, uniqueFilters);
                }
                nonModifyFilter = { ...nonModifyFilter, ...modifiedFilter };
            }
        }
        return nonModifyFilter;
    }

    private getValues(
        filters: {
            [key: string]: string[] | TransformedDomain[];
        },
        filter: ConstantApplicationFilter,
    ) {
        const filterItem = filters[filter.field];
        const lifecyleMap = LifeCycleUtils.getLifeCycleMap();

        return filterItem
            .map((item) => this.mapItem(item, filter, lifecyleMap))
            .map((item: any) => item || Constants.UNSPECIFIED)
            .sort((a, b) => String(a).localeCompare(String(b)));
    }

    pushFilter(
        filter: ConstantApplicationFilter,
        modifiedFilter: Filter<string | TransformedDomain>,
        uniqueFilters: {
            [key: string]: string[] | TransformedDomain[];
        },
    ) {
        modifiedFilter[filter.field] = [
            filter.field === "domain"
                ? {
                      label: Constants.ALL,
                      checked: true,
                      visible: true,
                      children: [],
                  }
                : Constants.ALL,
            ...this.getValues(uniqueFilters, filter),
        ];
    }

    private mapItem(
        item: string | TransformedDomain,
        filter: ConstantApplicationFilter,
        lifecyleMap: Map<string, string>,
    ): string | TransformedDomain {
        if (filter.translated) {
            return this.mapLifecycle(item as string, lifecyleMap);
        }
        return item === "" ? Constants.EMPTY : item;
    }

    private mapLifecycle(lifecycle: string, lifecyleMap: Map<string, string>): string {
        return (
            this.translate.instant("acvStep." + lifecyleMap.get(lifecycle)) || lifecycle
        );
    }

    formatLifecycles(lifeCycles: string[]): string[] {
        const lifecycleMap = LifeCycleUtils.getLifeCycleMap();
        const lifecyclesList = Array.from(lifecycleMap.keys());

        return lifeCycles.map((lifeCycle) => {
            lifeCycle = lifeCycle.replace("acvStep.", "");
            if (
                lifeCycle !== Constants.ALL &&
                lifeCycle !== Constants.UNSPECIFIED &&
                lifecyclesList.includes(lifeCycle)
            ) {
                return this.translate.instant("acvStep." + lifecycleMap.get(lifeCycle));
            } else {
                return lifeCycle;
            }
        });
    }

    formatLifecycleImpact(footprint: ApplicationFootprint[]): ApplicationFootprint[] {
        if (!footprint?.length) {
            return [];
        }
        const lifecycleMap = LifeCycleUtils.getLifeCycleMap();
        const lifecyclesList = Array.from(lifecycleMap.keys());

        return footprint?.map((element) => {
            for (const impact of element?.impacts || []) {
                if (
                    impact.lifeCycle !== Constants.ALL &&
                    impact.lifeCycle !== Constants.UNSPECIFIED &&
                    lifecyclesList.includes(impact.lifeCycle)
                ) {
                    impact.lifeCycle = this.translate.instant(
                        "acvStep." + lifecycleMap.get(impact.lifeCycle),
                    );
                }
            }
            return element;
        });
    }

    formatLifecycleCriteriaImpact(
        footprint: ApplicationFootprint[],
    ): ApplicationFootprint[] {
        const lifecycleMap = LifeCycleUtils.getLifeCycleMap();
        const lifecyclesList = Array.from(lifecycleMap.keys());

        for (const element of footprint) {
            for (const impact of element.impacts) {
                if (
                    impact.lifeCycle !== Constants.ALL &&
                    impact.lifeCycle !== Constants.UNSPECIFIED &&
                    lifecyclesList.includes(impact.lifeCycle)
                ) {
                    impact.lifeCycle = this.translate.instant(
                        "acvStep." + lifecycleMap.get(impact.lifeCycle),
                    );
                }
            }
        }
        return footprint;
    }

    handleChartChange(criteria: any) {
        if (this.activatedRoute.snapshot.paramMap.get("criteria") === criteria) {
            this.router.navigate(["../", "multi-criteria"], {
                relativeTo: this.route,
            });
            return;
        }
        this.router.navigate(["../", criteria], {
            relativeTo: this.route,
        });
    }

    displayPopupFct() {
        const defaultCriteria = Object.keys(this.globalStore.criteriaList()).slice(0, 5);
        this.selectedCriterias =
            this.inventory().criteria! ??
            this.workspace?.criteriaIs ??
            this.organization?.criteria ??
            defaultCriteria;
        this.displayPopup = true;
    }

    saveInventory(inventoryCriteria: InventoryCriteriaRest) {
        this.displayPopup = false;
        this.globalStore.setLoading(true);

        this.inventoryService
            .updateInventoryCriteria(inventoryCriteria)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((res: Inventory) => {
                this.inventory().criteria = res.criteria;

                this.evaluationService
                    .launchEvaluating(this.inventory().id)
                    .pipe(
                        takeUntilDestroyed(this.destroyRef),
                        delay(500),
                        finalize(() => this.globalStore.setLoading(false)),
                    )
                    .subscribe((res: number) => {
                        this.asyncInit();
                    });
            });
    }

    handleFilters(event: { enableConsistency: boolean; unitType: string }) {
        this.selectedUnit = event.unitType;
        if (event.enableConsistency !== this.inventory().enableDataInconsistency) {
            // update
            this.globalStore.setLoading(true);
            const inv: InventoryCriteriaRest = {
                id: this.inventory().id,
                enableDataInconsistency: event.enableConsistency,
                name: this.inventory().name,
                criteria: this.inventory().criteria!,
                note: this.inventory().note!,
            };
            this.inventoryService
                .updateInventoryCriteria(inv)
                .pipe(
                    takeUntilDestroyed(this.destroyRef),
                    finalize(() => this.globalStore.setLoading(false)),
                )
                .subscribe((res: Inventory) => {
                    this.inventory.set(res);
                });
        }
    }
}
