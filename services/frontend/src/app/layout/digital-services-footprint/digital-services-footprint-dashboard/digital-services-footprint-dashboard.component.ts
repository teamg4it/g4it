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
    OnDestroy,
    OnInit,
    QueryList,
    Signal,
    signal,
    ViewChildren,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Title } from "@angular/platform-browser";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { firstValueFrom, lastValueFrom, Subscription } from "rxjs";
import { WorkspaceWithOrganization } from "src/app/core/interfaces/administration.interfaces";
import {
    AiRecommendation,
    DigitalService,
    DigitalServiceFootprint,
    DigitalServiceNetworksImpact,
    DigitalServiceServersImpact,
    DigitalServiceTerminalsImpact,
    DSCriteriaRest,
    GraphDescriptionContent,
} from "src/app/core/interfaces/digital-service.interfaces";
import {
    OutPhysicalEquipmentRest,
    OutVirtualEquipmentRest,
} from "src/app/core/interfaces/output.interface";
import { Organization, Workspace } from "src/app/core/interfaces/user.interfaces";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesAiDataService } from "src/app/core/service/data/digital-services-ai-data.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { OutPhysicalEquipmentsService } from "src/app/core/service/data/in-out/out-physical-equipments.service";
import { OutVirtualEquipmentsService } from "src/app/core/service/data/in-out/out-virtual-equipments.service";
import { ShareDigitalServiceDataService } from "src/app/core/service/data/share-digital-service-data.service";
import {
    convertToGlobalVision,
    transformOutPhysicalEquipmentsToNetworkData,
    transformOutPhysicalEquipmentstoServerData,
    transformOutPhysicalEquipmentsToTerminalData,
    transformOutVirtualEquipmentsToCloudData,
} from "src/app/core/service/mapper/digital-service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import { AbstractDashboard } from "../../inventories-footprint/abstract-dashboard";
import { BarChartComponent } from "./bar-chart/bar-chart.component";

@Component({
    selector: "app-digital-services-footprint-dashboard",
    templateUrl: "./digital-services-footprint-dashboard.component.html",
    styleUrls: ["./digital-services-footprint-dashboard.component.scss"],
})
export class DigitalServicesFootprintDashboardComponent
    extends AbstractDashboard
    implements OnInit, OnDestroy
{
    private readonly digitalServiceStore = inject(DigitalServiceStoreService);
    private readonly outPhysicalEquipmentsService = inject(OutPhysicalEquipmentsService);
    private readonly outVirtualEquipmentsService = inject(OutVirtualEquipmentsService);
    private readonly digitalServicesAiData = inject(DigitalServicesAiDataService);
    private readonly destroyRef = inject(DestroyRef);
    private readonly shareDigitalService = inject(ShareDigitalServiceDataService);
    private readonly route = inject(ActivatedRoute);
    chartType = signal("radial");
    showInconsitencyBtn = false;
    constants = Constants;
    noData = true;
    selectedUnit: string = "Raw";
    selectedCriteria: string = "Global Vision";
    selectedCriteriaPopup: string[] = [];
    selectedParam: string = "";
    selectedDetailName: string = "";
    selectedDetailParam: string = "";
    showInconsitency = false;
    // barCharChild == true => is the new bar chart generated after clicking on a bar chart
    barChartChild: boolean = false;
    options: EChartsOption = {};
    digitalService: DigitalService = {} as DigitalService;
    aiRecommendation: AiRecommendation = {} as AiRecommendation;
    showDataButton = false;
    displaySetViewPopup = false;
    title = "";
    content = "";

    impacts: any[] = [];
    topThreeImpacts: any[] = [];
    topPieThreeImpacts: any[] = [];
    barChartTopThreeImpact: any[] = [];
    barChartTopThreeResourceImpact: any[] = [];

    globalVisionChartData: DigitalServiceFootprint[] | undefined;

    outPhysicalEquipments: OutPhysicalEquipmentRest[] = [];
    outVirtualEquipments: OutVirtualEquipmentRest[] = [];
    onlyOneCriteria = false;
    displayCriteriaPopup = false;
    workspace: WorkspaceWithOrganization = {} as WorkspaceWithOrganization;
    organization!: Organization;
    textDescriptionImpacts: {
        text: string;
        impactName: string;
        impactNameVisible: string;
    }[] = [];

    textDescriptionResourceImpacts: {
        text: string;
        impactName: string;
        impactNameVisible: string;
    }[] = [];
    @ViewChildren(BarChartComponent) barChartComponents?: QueryList<BarChartComponent>;

    cloudData = computed(() => {
        if (this.outVirtualEquipments === undefined) return [];
        return transformOutVirtualEquipmentsToCloudData(
            this.outVirtualEquipments,
            this.digitalServiceStore.countryMap(),
        );
    });

    networkData: Signal<DigitalServiceNetworksImpact[]> = computed(() => {
        return transformOutPhysicalEquipmentsToNetworkData(
            this.outPhysicalEquipments,
            this.digitalServiceStore.networkTypes(),
        );
    });

    serverData: Signal<DigitalServiceServersImpact[]> = computed(() => {
        return transformOutPhysicalEquipmentstoServerData(
            this.outPhysicalEquipments,
            this.outVirtualEquipments,
            this.digitalServiceStore.serverTypes(),
        );
    });

    terminalData: Signal<DigitalServiceTerminalsImpact[]> = computed(() => {
        return transformOutPhysicalEquipmentsToTerminalData(
            this.outPhysicalEquipments,
            this.digitalServiceStore.terminalDeviceTypes(),
        );
    });

    calculatedCriteriaList: string[] = [];
    sub!: Subscription;
    constructor(
        private readonly digitalServicesDataService: DigitalServicesDataService,
        private readonly digitalServiceBusinessService: DigitalServiceBusinessService,
        public userService: UserService,
        override globalStore: GlobalStoreService,
        override translate: TranslateService,
        override integerPipe: IntegerPipe,
        override decimalsPipe: DecimalsPipe,
        private readonly titleService: Title,
        private readonly router: Router,
    ) {
        super(translate, integerPipe, decimalsPipe, globalStore);
    }

    ngOnInit() {
        this.asyncInit();
    }
    private async asyncInit() {
        this.digitalService = await firstValueFrom(
            this.digitalServicesDataService.digitalService$,
        );

        this.userService.currentOrganization$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((organization: Organization) => {
                this.organization = organization;
            });
        this.userService.currentWorkspace$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((workspace: Workspace) => {
                this.workspace.organizationName = this.organization.name;
                this.workspace.organizationId = this.organization.id;
                this.workspace.workspaceName = workspace.name;
                this.workspace.workspaceId = workspace.id;
                this.workspace.status = workspace.status;
                this.workspace.dataRetentionDays = workspace.dataRetentionDays!;
                this.workspace.displayLabel = `${workspace.name} - (${this.organization.name})`;
                this.workspace.criteriaDs = workspace.criteriaDs!;
                this.workspace.criteriaIs = workspace.criteriaIs!;
            });
        const titleKey = this.digitalService.isAi
            ? "welcome-page.eco-mind-ai.title"
            : "digital-services.page-title";
        this.translate
            .get(titleKey)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((translatedTitle: string) => {
                this.titleService.setTitle(`${translatedTitle} - G4IT`);
            });

        this.sub = this.route.parent!.paramMap.subscribe((params) => {
            const dsVersionUid = params.get("digitalServiceVersionId") ?? "";
            this.updateRecomendation(dsVersionUid);
        });
    }

    async updateRecomendation(dsVersionUid: string): Promise<void> {
        if (this.digitalService.isAi) {
            try {
                this.aiRecommendation = await firstValueFrom(
                    this.digitalServicesAiData.getAiRecommendations(dsVersionUid),
                );
            } catch (error) {
                console.error("Error fetching AI recommendations:", error);
            }
        }
        const isShared = this.digitalServiceStore.isSharedDS();
        const [_, _1, sharedToken] = this.router.url.split("/");
        const physicalEquipments$ = isShared
            ? this.shareDigitalService.getOutSharedPhysicalEquipments(
                  this.digitalService.uid,
                  sharedToken,
              )
            : this.outPhysicalEquipmentsService.get(dsVersionUid);
        const virtualEquipments$ = isShared
            ? this.shareDigitalService.getOutSharedVirtualEquipments(
                  this.digitalService.uid,
                  sharedToken,
              )
            : this.outVirtualEquipmentsService.getByDigitalService(dsVersionUid);

        // code added for digital service output physical equipment not visible
        const MAX_RETRIES = 10;
        const DELAY_MS = 500;
        const LOADER_TIMEOUT_MS = 1200;

        const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

        let outPhysicalEquipments: OutPhysicalEquipmentRest[] = [];
        let outVirtualEquipments: OutVirtualEquipmentRest[] = [];

        try {
            for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                if (!this.globalStore.loading()) {
                    this.globalStore.setLoading(true);
                }

                [outPhysicalEquipments, outVirtualEquipments] = await Promise.all([
                    outPhysicalEquipments.length === 0
                        ? firstValueFrom(physicalEquipments$)
                        : Promise.resolve(outPhysicalEquipments),

                    outVirtualEquipments.length === 0
                        ? firstValueFrom(virtualEquipments$)
                        : Promise.resolve(outVirtualEquipments),
                ]);

                // stop early if both have data
                if (outPhysicalEquipments.length > 0 && outVirtualEquipments.length > 0) {
                    break;
                }

                // delay before next retry
                if (attempt < MAX_RETRIES) {
                    await delay(DELAY_MS);
                }
            }
            this.outPhysicalEquipments = outPhysicalEquipments;
            this.outVirtualEquipments = outVirtualEquipments;
        } finally {
            setTimeout(() => {
                this.globalStore.setLoading(false);
            }, LOADER_TIMEOUT_MS);
        }

        this.retrieveFootprintData();
        if (this.impacts?.length === 1) {
            this.onlyOneCriteria = true;
            this.selectedCriteria = this.impacts[0]?.name;
            this.chartType.set("pie");
        }
    }

    initImpacts(): void {
        this.selectedLang = this.translate.currentLang;
        const criteriaKeys = Object.keys(this.globalStore.criteriaList());
        this.impacts = (
            this.calculatedCriteriaList.length > 0
                ? criteriaKeys.filter((criteria) =>
                      this.calculatedCriteriaList.includes(criteria),
                  )
                : criteriaKeys
        ).map((criteria) => {
            return { name: criteria, title: "", unite: "", raw: null, peopleeq: null };
        });
    }

    retrieveFootprintData() {
        this.calculatedCriteriaList = [];

        this.globalVisionChartData = convertToGlobalVision(
            this.outPhysicalEquipments,
            this.outVirtualEquipments,
        );
        this.showInconsitencyBtn = this.globalVisionChartData
            .flatMap((footprint) => footprint?.impacts)
            .some((footprint) =>
                Constants.DATA_QUALITY_ERROR.includes(footprint?.status),
            );

        if (this.globalVisionChartData.length > 0) {
            for (const impact of this.globalVisionChartData[0].impacts) {
                this.calculatedCriteriaList.push(impact.criteria);
            }
        }
        this.initImpacts();
        this.setCriteriaButtons(this.globalVisionChartData);
        if (this.globalVisionChartData.length > 0) {
            this.noData = false;
        } else {
            this.noData = true;
        }
    }

    handleChartChange(criteria: string) {
        if (this.selectedCriteria === "Global Vision") {
            this.selectedCriteria = criteria;
            this.chartType.set("pie");
            this.barChartChild = false;
        } else if (this.selectedCriteria == criteria) {
            this.selectedCriteria = "Global Vision";
            this.chartType.set("radial");
            this.barChartChild = false;
        } else if (this.selectedCriteria != criteria) {
            this.selectedCriteria = criteria;
        }
    }

    setCriteriaButtons(globalFootprintData: DigitalServiceFootprint[]): void {
        if (globalFootprintData?.length == 0) {
            this.initImpacts();
            return;
        }

        const criteriaMap = new Map<
            string,
            {
                raw: number;
                peopleeq: number;
                maxCriteria: { name: string; peopleeq: number; raw: number };
            }
        >();

        for (const tierData of globalFootprintData) {
            for (const impactData of tierData.impacts) {
                const { criteria, unitValue, sipValue } = impactData;
                if (criteriaMap.has(criteria)) {
                    criteriaMap.get(criteria)!.raw += unitValue;
                    criteriaMap.get(criteria)!.peopleeq += sipValue;
                    if (sipValue > criteriaMap.get(criteria)!.maxCriteria.peopleeq) {
                        criteriaMap.get(criteria)!.maxCriteria.name = tierData.tier;
                        criteriaMap.get(criteria)!.maxCriteria.peopleeq = sipValue;
                        criteriaMap.get(criteria)!.maxCriteria.raw = unitValue;
                    }
                } else {
                    criteriaMap.set(criteria, {
                        raw: unitValue,
                        peopleeq: sipValue,
                        maxCriteria: {
                            name: tierData.tier,
                            peopleeq: sipValue,
                            raw: unitValue,
                        },
                    });
                }
            }
        }
        for (const impact of this.impacts) {
            const criteria = impact.name;
            impact.title = this.translate.instant(`criteria.${criteria}.title`);
            impact.unite = this.translate.instant(`criteria.${criteria}.unite`);
            if (criteriaMap.has(criteria)) {
                impact.raw = criteriaMap.get(criteria)!.raw;
                impact.peopleeq = criteriaMap.get(criteria)!.peopleeq;
                impact.maxCriteria = criteriaMap.get(criteria)!.maxCriteria;
            }
        }
        this.topThreeImpacts = [...this.impacts]
            .map((impact) => ({
                ...impact,
                maxCriteria: {
                    ...impact.maxCriteria,
                    name: this.translate.instant(
                        `digital-services.${impact.maxCriteria.name}`,
                    ),
                },
            }))
            .sort((a, b) => b.peopleeq - a.peopleeq)
            .slice(0, 3);
    }

    getEcoMindRecomendation(): string {
        if (this.aiRecommendation?.recommendations != null && this.digitalService.isAi) {
            try {
                const recommendationsArr = JSON.parse(
                    this.aiRecommendation.recommendations,
                );
                if (!Array.isArray(recommendationsArr) || recommendationsArr.length === 0)
                    return "";
                // Dynamic titles
                const headers = Object.keys(recommendationsArr[0]);
                // HTML table generation for recommendation
                let table = `
                    <div style='overflow-x:auto;'>
                     <table style='width:100%;border-collapse:collapse;min-width:600px;'>
                    <thead><tr>`;
                for (const h of headers) {
                    table += `<th style='padding:14px 18px;text-align:center;font-size:1rem;'>${h.charAt(0).toUpperCase() + h.slice(1)}</th>`;
                }
                table += `</tr></thead><tbody>`;
                for (const rec of recommendationsArr) {
                    table += `<tr>`;
                    for (const h of headers) {
                        table += `<td style='padding:14px 18px;font-size:0.98rem;text-align:center;'>${rec[h]}</td>`;
                    }
                    table += `</tr>`;
                }
                table += `</tbody></table></div>`;
                return table;
            } catch (error) {
                console.error("Error parsing AI recommendations:", error);
                return "";
            }
        }
        return "";
    }

    getBarTranslateKey(): string {
        const isServer = this.selectedParam === "Server";
        const isCloudService = this.selectedParam === Constants.CLOUD_SERVICE;
        const isTerminal = this.selectedParam === Constants.TERMINAL;
        const isBarChartChild = this.barChartChild === true;

        let translationKey = "";
        if (!isBarChartChild && isServer) {
            translationKey = `ds-graph-description.server.`;
        } else if (isBarChartChild && isServer) {
            translationKey = `ds-graph-description.${this.selectedParam.toLowerCase().replaceAll(/\s+/g, "-")}-${this.barChartComponents?.first?.serversRadioButtonSelected}.`;
        } else if (!isBarChartChild && isCloudService) {
            translationKey = `ds-graph-description.${this.selectedParam.toLowerCase().replaceAll(/\s+/g, "-")}-${this.barChartComponents?.first?.cloudRadioButtonSelected}.`;
        } else if (isBarChartChild && isCloudService) {
            translationKey = "ds-graph-description.cloud-lifecycle.";
        } else if (!isBarChartChild && isTerminal) {
            translationKey = `ds-graph-description.${this.selectedParam.toLowerCase().replaceAll(/\s+/g, "-")}-${this.barChartComponents?.first?.terminalsRadioButtonSelected}.`;
        } else if (isBarChartChild && isTerminal) {
            translationKey = "ds-graph-description.terminal-lifecycle.";
        } else {
            translationKey = `ds-graph-description.${this.selectedParam.toLowerCase().replaceAll(/\s+/g, "-")}.`;
        }
        return translationKey;
    }

    getContentText(): GraphDescriptionContent {
        const isBarChart = this.chartType() === "bar";
        const isIncludeCriteria = Object.keys(this.globalStore.criteriaList()).includes(
            this.selectedCriteria,
        );
        let translationKey: string;
        let textDescription: string = "";
        let textResourceDescription: string = "";
        if (isBarChart) {
            translationKey = this.getBarTranslateKey();
            const description = this.getBarChartTextDescription(translationKey);
            textDescription = description.textDescription;
            textResourceDescription = description.textResourceDescription;
        } else {
            const criteriaKey = this.selectedCriteria
                .toLowerCase()
                .replaceAll(/\s+/g, "-");
            if (isIncludeCriteria) {
                //Criteria View
                translationKey = "ds-graph-description.criteria.";
                textDescription = this.getCriteriaTextDescription(
                    translationKey,
                    criteriaKey,
                );
            } else {
                // Global Vision
                translationKey = `ds-graph-description.${criteriaKey}.`;
                textDescription = this.getGlobalVisionTextDescription(translationKey);
            }
        }
        const key =
            "criteria." + this.selectedCriteria.toLowerCase().replaceAll(" ", "-") + ".";

        return {
            description: this.translate.instant(`${translationKey}description`, {
                criteria: this.impacts.flatMap((impact) => impact.title).join(", "),
            }),
            scale: isIncludeCriteria
                ? this.translate.instant(`${key}scale`)
                : this.translate.instant(`${translationKey}scale`),
            textDescription: textDescription,
            textResourceDescription: textResourceDescription,
            analysis: this.translate.instant(`${translationKey}analysis`),
            toGoFurther: this.translate.instant(`${translationKey}to-go-further`),
        };
    }

    getBarChartTextDescription(translationKey: string): {
        textDescription: string;
        textResourceDescription: string;
    } {
        let textDescription = "";
        let textImpacts = [];
        let textResourceImpacts = [];
        let textResourceDescription = "";
        for (const [index, impact] of this.barChartTopThreeImpact.entries()) {
            if (index === 0) {
                textDescription += this.translate.instant(
                    `${translationKey}text-description`,
                    {
                        cloudInstanceName: this.selectedDetailName,
                    },
                );
            }
            textImpacts.push({
                text: this.translate.instant(
                    `${translationKey}text-description-iterate`,
                    {
                        impactName: impact.name,
                        impactValue: this.integerPipe.transform(impact.totalSipValue),
                        rawValue: this.decimalsPipe.transform(impact.totalRawValue),
                        unit: impact.unit,
                    },
                ),
                impactName: impact.name,
                impactNameVisible: impact.name,
                impactType: impact?.type,
            });
        }

        if (!this.barChartChild && this.selectedParam !== "Network") {
            for (const [index, impact] of this.barChartTopThreeResourceImpact.entries()) {
                if (index === 0) {
                    textResourceDescription += this.translate.instant(
                        `${translationKey}resource-text-description`,
                        {
                            cloudInstanceName: this.selectedDetailName,
                        },
                    );
                }
                textResourceImpacts.push({
                    text: this.translate.instant(
                        `${translationKey}text-description-iterate`,
                        {
                            impactName: impact.name,
                            impactValue: this.integerPipe.transform(impact.totalSipValue),
                            rawValue: this.decimalsPipe.transform(impact.totalRawValue),
                            unit: impact.unit,
                        },
                    ),
                    impactName: impact.name,
                    impactNameVisible: impact.name,
                    impactType: impact?.type,
                });
            }
        }

        this.textDescriptionResourceImpacts = textResourceImpacts;
        this.textDescriptionImpacts = textImpacts;
        return { textDescription, textResourceDescription };
    }

    getGlobalVisionTextDescription(translationKey: string): string {
        let textDescription = "";
        let textImpacts = [];
        const firstPrefix = this.translate.instant(
            `${translationKey}text-description-first-prefix`,
        );
        const iteratePrefix = this.translate.instant(
            `${translationKey}text-description-iterate-prefix`,
        );
        for (const [index, impact] of this.topThreeImpacts.entries()) {
            const prefix = index === 0 ? firstPrefix : iteratePrefix;

            if (index === 0) {
                textDescription += this.translate.instant(
                    `${translationKey}text-description`,
                );
            }

            textImpacts.push({
                text:
                    prefix +
                    this.translate.instant(`${translationKey}text-description-iterate`, {
                        impactName: impact.title,
                        impactValue: this.integerPipe.transform(impact.peopleeq),
                        resource: impact.maxCriteria.name,
                        resourceValue: this.integerPipe.transform(
                            impact.maxCriteria.peopleeq,
                        ),
                        rawValue: this.decimalsPipe.transform(impact.raw),
                        unit: impact.unite,
                        resourceRawValue: this.decimalsPipe.transform(
                            impact.maxCriteria.raw,
                        ),
                        resourceUnit: impact.unite,
                    }),
                impactName: impact.name,
                impactNameVisible: impact.title,
            });
        }
        this.textDescriptionImpacts = textImpacts;
        this.textDescriptionResourceImpacts = [];
        return textDescription;
    }

    getCriteriaTextDescription(translationKey: string, criteriaKey: string): string {
        let textDescription = "";
        let textImpacts = [];
        for (const [index, impact] of this.topPieThreeImpacts.entries()) {
            if (index === 0) {
                textDescription += this.translate.instant(
                    `${translationKey}text-description`,
                    {
                        resource: this.translate.instant(`criteria.${criteriaKey}.title`),
                    },
                );
            }
            textImpacts.push({
                text: this.translate.instant(
                    `${translationKey}text-description-iterate`,
                    {
                        impactName: impact.name,
                        impactValue: this.integerPipe.transform(impact.value),
                        resource: this.translate.instant(`criteria.${criteriaKey}.title`),
                        resourceValue: this.integerPipe.transform(impact.percentage),
                        rawValue: this.decimalsPipe.transform(impact.unitValue),
                        unit: impact.unit,
                    },
                ),
                impactName: impact.tier,
                impactNameVisible: impact.name,
            });
        }
        this.textDescriptionImpacts = textImpacts;
        this.textDescriptionResourceImpacts = [];
        return textDescription;
    }

    getTNSTranslation(input: string) {
        return this.translate.instant("digital-services." + input);
    }

    displayPopupFct() {
        const defaultCriteria = Object.keys(this.globalStore.criteriaList()).slice(0, 5);
        const criteriasCalculated = this.impacts.flatMap((impact) => impact.name);
        this.selectedCriteriaPopup =
            this.digitalService.criteria ??
            criteriasCalculated ??
            this.workspace.criteriaDs ??
            this.organization.criteria ??
            defaultCriteria;
        this.displayCriteriaPopup = true;
    }
    handleSaveDs(DSCriteria: DSCriteriaRest) {
        this.digitalServiceBusinessService
            .updateDsCriteria(this.digitalService.uid, DSCriteria)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(() => {
                this.digitalServicesDataService
                    .get(this.digitalService.uid)
                    .pipe(takeUntilDestroyed(this.destroyRef))
                    .subscribe();
                this.displayCriteriaPopup = false;
                this.digitalServiceStore.setEnableCalcul(true);
                // Launch Calculation after Saving Criteria
                this.digitalServiceBusinessService.triggerLaunchCalcul();
            });
    }

    async updateDataConsistencyInDS(event: any): Promise<void> {
        if (event === false || event === true) {
            this.digitalService.enableDataInconsistency = event;
            if (event === false) {
                this.showInconsitency = false;
            }
            this.digitalService = await lastValueFrom(
                this.digitalServicesDataService.update(this.digitalService),
            );
        }
    }

    handleImpactClick(impactName: any) {
        if (this.chartType() == "radial") {
            this.handleChartChange(impactName);
            return;
        }
        if (this.chartType() == "pie") {
            this.selectedParam = impactName;
            this.chartType.set("bar");
            this.barChartChild = false;
            return;
        }
        if (this.chartType() == "bar") {
            if (this.selectedParam !== "Network") {
                this.selectedDetailName = impactName.impactName;
                this.selectedDetailParam = impactName.impactType;
                this.barChartChild = true;
            }
        }
    }

    ngOnDestroy() {
        // Clean store data
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
        this.sub?.unsubscribe();
    }
}
