/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    Component,
    DestroyRef,
    OnInit,
    Signal,
    WritableSignal,
    computed,
    effect,
    inject,
    signal,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MenuItem } from "primeng/api";
import { delay, finalize, firstValueFrom, forkJoin, map } from "rxjs";
import {
    OrganizationCriteriaRest,
    WorkspaceCriteriaRest,
} from "src/app/core/interfaces/administration.interfaces";
import { Filter } from "src/app/core/interfaces/filter.interface";
import {
    ChartData,
    ComputedSelection,
    Criteria,
    Criterias,
    Datacenter,
    Impact,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
    PhysicalEquipmentsElecConsumption,
    Stat,
} from "src/app/core/interfaces/footprint.interface";
import { StatGroup } from "src/app/core/interfaces/indicator.interface";
import { InVirtualEquipmentRest } from "src/app/core/interfaces/input.interface";
import {
    Inventory,
    InventoryCriteriaRest,
} from "src/app/core/interfaces/inventory.interfaces";
import { OutVirtualEquipmentRest } from "src/app/core/interfaces/output.interface";
import { Organization, Workspace } from "src/app/core/interfaces/user.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { InventoryUtilService } from "src/app/core/service/business/inventory-util.service";
import { InventoryService } from "src/app/core/service/business/inventory.service";
import { UserService } from "src/app/core/service/business/user.service";
import { EvaluationDataService } from "src/app/core/service/data/evaluation-data.service";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { InVirtualEquipmentsService } from "src/app/core/service/data/in-out/in-virtual-equipments.service";
import { OutVirtualEquipmentsService } from "src/app/core/service/data/in-out/out-virtual-equipments.service";
import { transformCriterion } from "src/app/core/service/mapper/array";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import { Constants } from "src/constants";

@Component({
    selector: "app-inventories-footprint",
    templateUrl: "./inventories-footprint.component.html",
})
export class InventoriesFootprintComponent implements OnInit {
    protected footprintStore = inject(FootprintStoreService);
    private readonly globalStore = inject(GlobalStoreService);
    private readonly outVirtualEquipmentService = inject(OutVirtualEquipmentsService);
    private readonly inVirtualEquipmentsService = inject(InVirtualEquipmentsService);
    private readonly digitalServiceStore = inject(DigitalServiceStoreService);
    protected readonly userService = inject(UserService);
    private readonly inventoryUtilService = inject(InventoryUtilService);
    private readonly inventoryService = inject(InventoryService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    private readonly filterService = inject(FilterService);
    private readonly evaluationService = inject(EvaluationDataService);
    private readonly destroyRef = inject(DestroyRef);
    filterSidebarVisible = false;
    selectedUnit: string = "Raw";

    selectedView: string = "";

    echartsData: any = [];

    chartData: ChartData<ComputedSelection> = {};

    selectedLang: string = this.translate.currentLang;

    criterias = [
        Constants.MUTLI_CRITERIA,
        ...Object.keys(this.globalStore.criteriaList()),
    ];

    criteres: MenuItem[] = [
        {
            label: this.translate.instant("criteria-title.multi-criteria.title"),
            routerLink: `../${Constants.MUTLI_CRITERIA}`,
            id: "multi-criteria",
        },
    ];

    equipmentStats = signal<Stat[]>([]);
    cloudStats = signal<Stat[]>([]);
    datacenterStats = signal<Stat[]>([]);

    statGroups: Signal<StatGroup[]> = computed(() => {
        const eqStats = this.equipmentStats();
        const clStats = this.cloudStats();
        const dcStats = this.datacenterStats();
        console.log(eqStats);
        console.log(clStats);

        return [
            {
                subtitle: this.translate.instant("common.infrastructure"),
                items: [eqStats[0], eqStats[1], clStats[0]],
            },
            {
                subtitle: this.translate.instant("common.energy"),
                items: [eqStats[3], eqStats[2], dcStats[1]],
            },
        ] as StatGroup[];
    });

    allUnmodifiedFootprint: WritableSignal<Criterias> = signal({} as Criterias);
    allUnmodifiedFilters: Filter<string> = {};
    allUnmodifiedDatacenters: WritableSignal<Datacenter[]> = signal([] as Datacenter[]);
    allUnmodifiedEquipments: WritableSignal<
        [
            PhysicalEquipmentAvgAge[],
            PhysicalEquipmentLowImpact[],
            PhysicalEquipmentsElecConsumption[],
        ]
    > = signal([[], [], []]);
    allUnmodifiedCriteriaFootprint: Criteria = {} as Criteria;

    order = LifeCycleUtils.getLifeCycleList();
    lifeCycleMap = LifeCycleUtils.getLifeCycleMap();

    filterFields = Constants.EQUIPMENT_FILTERS;
    multiCriteria = Constants.MUTLI_CRITERIA;
    inventoryId = +this.activatedRoute.snapshot.paramMap.get("inventoryId")! || 0;
    dimensions = Constants.EQUIPMENT_DIMENSIONS;
    transformedInVirtualEquipments: WritableSignal<InVirtualEquipmentRest[]> = signal([]);
    inventory: WritableSignal<Inventory> = signal({} as Inventory);
    selectedCriteria: string = "";
    currentLang: string = this.translate.currentLang;
    criteriakeys = Object.keys(this.translate.translations[this.currentLang]["criteria"]);
    displayPopup = false;
    selectedCriterias: string[] = [];
    isCollapsed = false;
    organization: OrganizationCriteriaRest = { criteria: [] };
    workspace: WorkspaceCriteriaRest = {
        organizationId: 0,
        name: "",
        status: "",
        dataRetentionDays: 0,
        criteriaIs: [],
        criteriaDs: [],
    };

    impacts: Signal<any> = computed(() => {
        const allImpacts = Object.entries(this.allUnmodifiedFootprint()).flatMap(
            ([criteriaName, criteriaData]) => ({
                criteria: criteriaName,
                unit: criteriaData.unit,
                criteriaTitle: this.translate.instant(`criteria.${criteriaName}.title`),
                impacts: criteriaData.impacts.filter((impact) => {
                    return this.filterService.equipmentAllFilterMatch(
                        this.footprintStore.filters(),
                        impact as any,
                    );
                }),
            }),
        );
        return this.footprintService
            .filterCriteriaImpact(allImpacts as any)
            .sort(
                (a, b) =>
                    this.criteriakeys.indexOf(a.name) - this.criteriakeys.indexOf(b.name),
            );
    });
    constructor(
        private readonly activatedRoute: ActivatedRoute,
        private readonly footprintDataService: FootprintDataService,
        private readonly footprintService: FootprintService,
        private readonly translate: TranslateService,
        private readonly digitalBusinessService: DigitalServiceBusinessService,
    ) {
        effect(() => {
            (async () => {
                const res = await this.inventoryUtilService.computeEquipmentStats(
                    this.allUnmodifiedEquipments(),
                    this.footprintStore.filters(),
                    this.filterFields,
                    this.allUnmodifiedFootprint(),
                );
                this.equipmentStats.set(res);
            })();

            (async () => {
                const res = await this.inventoryUtilService.computeCloudStats(
                    this.transformedInVirtualEquipments(),
                    this.footprintStore.filters(),
                    this.filterFields,
                );
                this.cloudStats.set(res);
            })();

            (async () => {
                const res = await this.inventoryUtilService.computeDataCenterStats(
                    this.footprintStore.filters(),
                    this.filterFields,
                    this.allUnmodifiedDatacenters(),
                );
                this.datacenterStats.set(res);
            })();
        });
    }

    ngOnInit() {
        this.getOnInitData();
    }

    async getOnInitData() {
        await this.initInventory();
        const criteria = this.activatedRoute.snapshot.paramMap.get("criteria");
        this.selectedCriteria = criteria!;
        this.getOrganizationAndWorkspace();
        const currentWorkspaceName = (
            await firstValueFrom(this.userService.currentWorkspace$)
        ).name;
        this.globalStore.setLoading(true);
        this.digitalBusinessService.initCountryMap();
        this.getDataApis(currentWorkspaceName, criteria);
    }

    async initInventory() {
        let result = await this.inventoryService.getInventories(this.inventoryId);
        if (result.length > 0) this.inventory.set(result[0]);
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

    getDataApis(currentWorkspaceName: string, criteria: string | null) {
        forkJoin([
            this.footprintDataService
                .getFootprint(this.inventoryId)
                .pipe(
                    map((data) =>
                        this.inventoryUtilService.removeWorkspaceNameFromCriteriaType(
                            data,
                            currentWorkspaceName,
                        ),
                    ),
                ),
            this.footprintDataService.getDatacenters(this.inventoryId),
            this.footprintDataService
                .getPhysicalEquipments(this.inventoryId)
                .pipe(
                    map((data) =>
                        this.inventoryUtilService.removeWorkspaceNameFromType(
                            data,
                            currentWorkspaceName,
                        ),
                    ),
                ),
            this.outVirtualEquipmentService.getByInventory(this.inventoryId),
            this.inVirtualEquipmentsService.getByInventory(this.inventoryId),
        ]).subscribe((results) => {
            const [
                footprint,
                datacenters,
                physicalEquipments,
                outVirtualEquipments,
                inVirtualEquipments,
            ] = results;

            this.processFootprintData(
                footprint,
                inVirtualEquipments,
                outVirtualEquipments,
            );

            this.initializeCriteriaMenu(footprint, criteria!);

            this.initializeFootprintData(footprint, datacenters, physicalEquipments);

            // React on criteria url param change
            this.activatedRoute.paramMap.subscribe((params) => {
                const criteria = params.get("criteria")!;
                this.footprintStore.setCriteria(criteria);
                this.selectedCriteria = criteria;
                if (criteria !== Constants.MUTLI_CRITERIA) {
                    this.allUnmodifiedCriteriaFootprint =
                        this.allUnmodifiedFootprint()[criteria];
                }
            });
        });
    }
    processFootprintData(
        footprint: Criterias,
        inVirtualEquipments: InVirtualEquipmentRest[],
        outVirtualEquipments: OutVirtualEquipmentRest[],
    ) {
        this.transformedInVirtualEquipments.set(
            this.transformInVirtualEquipment(inVirtualEquipments),
        );
        const transformedOutVirtualEquipments =
            this.transformOutVirtualEquipment(outVirtualEquipments);
        this.tranformAcvStepFootprint(footprint);

        for (const equipment of transformedOutVirtualEquipments) {
            const matchedFootprint = footprint[equipment.criteria];

            if (matchedFootprint) {
                matchedFootprint.impacts.push(equipment);
            } else {
                footprint[equipment.criteria] = {
                    label: equipment?.criteria?.toLocaleLowerCase().replaceAll("-", "_"),
                    unit: equipment.unit!,
                    impacts: [equipment],
                };
            }
        }
    }
    initializeFootprintData(
        footprint: Criterias,
        datacenters: Datacenter[],
        physicalEquipments: [
            PhysicalEquipmentAvgAge[],
            PhysicalEquipmentLowImpact[],
            PhysicalEquipmentsElecConsumption[],
        ],
    ) {
        this.allUnmodifiedFootprint.set(structuredClone(footprint));
        this.allUnmodifiedDatacenters.set(datacenters);
        this.allUnmodifiedEquipments.set(physicalEquipments);
        this.allUnmodifiedFilters = {};

        const uniqueFilterSet = this.footprintService.getUniqueValues(
            this.allUnmodifiedFootprint(),
            Constants.EQUIPMENT_FILTERS,
            true,
        );

        for (const field of Constants.EQUIPMENT_FILTERS) {
            this.allUnmodifiedFilters[field] = [
                Constants.ALL,
                ...uniqueFilterSet[field]
                    .map((item: any) => {
                        const value = typeof item === "string" ? item?.trim() : item;
                        return value?.length ? value : Constants.EMPTY;
                    })
                    .sort((a, b) => String(a).localeCompare(String(b))),
            ];
        }
        // Compute stats after data is loaded
        this.computeStats();
        this.globalStore.setLoading(false);
    }

    async computeStats() {
        const eqStats = await this.inventoryUtilService.computeEquipmentStats(
            this.allUnmodifiedEquipments(),
            this.footprintStore.filters(),
            this.filterFields,
            this.allUnmodifiedFootprint(),
        );
        this.equipmentStats.set(eqStats);

        const clStats = await this.inventoryUtilService.computeCloudStats(
            this.transformedInVirtualEquipments(),
            this.footprintStore.filters(),
            this.filterFields,
        );
        this.cloudStats.set(clStats);
    }

    initializeCriteriaMenu(footprint: Criterias, criteria: string) {
        const footprintCriteriaKeys = Object.keys(footprint);
        const sortedCriteriaKeys = Object.keys(this.globalStore.criteriaList()).filter(
            (key) => footprintCriteriaKeys.includes(key),
        );
        this.criteres = sortedCriteriaKeys.map((key: string) => {
            return {
                label: this.translate.instant(`criteria.${key}.title`),
                routerLink: `../${key}`,
                id: `${key}`,
            };
        });
        if (this.criteres.length > 1) {
            this.criteres.unshift({
                label: this.translate.instant("criteria-title.multi-criteria.title"),
                routerLink: `../${Constants.MUTLI_CRITERIA}`,
            });
        }
        this.footprintStore.setCriteria(criteria || Constants.MUTLI_CRITERIA);
    }

    transformOutVirtualEquipment(
        outVirtualEquipments: OutVirtualEquipmentRest[],
    ): Impact[] {
        return outVirtualEquipments
            .filter((item) => item.infrastructureType === "CLOUD_SERVICES")
            .map(
                (item) =>
                    ({
                        criteria: transformCriterion(item.criterion),
                        acvStep: LifeCycleUtils.getLifeCycleMapReverse().get(
                            item.lifecycleStep,
                        ),
                        country: this.digitalServiceStore.countryMap()[item.location],
                        equipment: `Cloud ${item.provider.toUpperCase()}`,
                        status: Constants.CLOUD_SERVICES,
                        entity: item.commonFilters?.[0] ?? null,
                        impact: item.unitImpact,
                        sip: item.peopleEqImpact,
                        statusIndicator: item.statusIndicator,
                        countValue: item.countValue,
                        quantity: item.quantity,
                        unit: item.unit,
                    }) as Impact,
            );
    }

    transformInVirtualEquipment(
        inVirtualEquipments: InVirtualEquipmentRest[],
    ): InVirtualEquipmentRest[] {
        return inVirtualEquipments
            .filter((item) => item.infrastructureType === "CLOUD_SERVICES")
            .map((item) => ({
                ...item,
                country: this.digitalServiceStore.countryMap()[item.location],
                equipment: `Cloud ${item?.provider?.toUpperCase()}`,
                status: Constants.CLOUD_SERVICES,
                entity: item.commonFilters?.[0] ?? null,
                quantity: item.quantity,
            }));
    }

    tranformAcvStepFootprint(footprint: Criterias): void {
        for (const key in footprint) {
            if (
                footprint[key].impacts?.length &&
                LifeCycleUtils.getLifeCycleList().includes(
                    footprint[key]?.impacts[0]?.acvStep,
                )
            ) {
                footprint[key].impacts = footprint[key].impacts.map((i) => {
                    return {
                        ...i,
                        acvStep:
                            LifeCycleUtils.getLifeCycleMapReverse().get(i.acvStep) ??
                            i.acvStep,
                    };
                });
            }
        }
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
                        this.getOnInitData();
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
}
