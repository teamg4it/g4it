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
    OnDestroy,
    OnInit,
    Signal,
    signal,
} from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { firstValueFrom } from "rxjs";
import {
    AiRecommendation,
    DigitalService,
    DigitalServiceFootprint,
    DigitalServiceNetworksImpact,
    DigitalServiceServersImpact,
    DigitalServiceTerminalsImpact,
} from "src/app/core/interfaces/digital-service.interfaces";
import {
    OutPhysicalEquipmentRest,
    OutVirtualEquipmentRest,
} from "src/app/core/interfaces/output.interface";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { DigitalServicesAiDataService } from "src/app/core/service/data/digital-services-ai-data.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { OutPhysicalEquipmentsService } from "src/app/core/service/data/in-out/out-physical-equipments.service";
import { OutVirtualEquipmentsService } from "src/app/core/service/data/in-out/out-virtual-equipments.service";
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

@Component({
    selector: "app-digital-services-footprint-dashboard",
    templateUrl: "./digital-services-footprint-dashboard.component.html",
})
export class DigitalServicesFootprintDashboardComponent
    extends AbstractDashboard
    implements OnInit, OnDestroy
{
    private digitalServiceStore = inject(DigitalServiceStoreService);
    private outPhysicalEquipmentsService = inject(OutPhysicalEquipmentsService);
    private outVirtualEquipmentsService = inject(OutVirtualEquipmentsService);
    private digitalServicesAiData = inject(DigitalServicesAiDataService);

    chartType = signal("radial");
    showInconsitencyBtn = false;
    constants = Constants;
    noData = true;
    selectedUnit: string = "Raw";
    selectedCriteria: string = "Global Vision";
    selectedParam: string = "";
    selectedDetailName: string = "";
    selectedDetailParam: string = "";
    showInconsitency = false;
    // barCharChild == true => is the new bar chart generated after clicking on a bar chart
    barChartChild: boolean = false;
    options: EChartsOption = {};
    digitalService: DigitalService = {} as DigitalService;
    aiRecommendation: AiRecommendation = {} as AiRecommendation;

    title = "";
    content = "";

    impacts: any[] = [];

    globalVisionChartData: DigitalServiceFootprint[] | undefined;

    outPhysicalEquipments: OutPhysicalEquipmentRest[] = [];
    outVirtualEquipments: OutVirtualEquipmentRest[] = [];
    onlyOneCriteria = false;

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

    constructor(
        private digitalServicesDataService: DigitalServicesDataService,
        private digitalServicesService: DigitalServiceBusinessService,
        override globalStore: GlobalStoreService,
        override translate: TranslateService,
        override integerPipe: IntegerPipe,
        override decimalsPipe: DecimalsPipe,
    ) {
        super(translate, integerPipe, decimalsPipe, globalStore);
    }

    async ngOnInit() {
        this.digitalService = await firstValueFrom(
            this.digitalServicesDataService.digitalService$,
        );

        if (this.digitalService.isAi) {
            try {
                this.aiRecommendation = await firstValueFrom(
                    this.digitalServicesAiData.getAiRecommendations(
                        this.digitalService.uid,
                    ),
                );
            } catch (error) {
                console.error("Error fetching AI recommendations:", error);
            }
        }

        const [outPhysicalEquipments, outVirtualEquipments] = await Promise.all([
            firstValueFrom(
                this.outPhysicalEquipmentsService.get(this.digitalService.uid),
            ),
            firstValueFrom(
                this.outVirtualEquipmentsService.getByDigitalService(
                    this.digitalService.uid,
                ),
            ),
        ]);
        this.outPhysicalEquipments = outPhysicalEquipments;
        this.outVirtualEquipments = outVirtualEquipments;

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
            this.globalVisionChartData[0].impacts.forEach((impact) => {
                this.calculatedCriteriaList.push(impact.criteria);
            });
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

        const criteriaMap = new Map<string, { raw: number; peopleeq: number }>();

        globalFootprintData.forEach((tierData) => {
            tierData.impacts.forEach((impactData) => {
                const { criteria, unitValue, sipValue } = impactData;
                if (criteriaMap.has(criteria)) {
                    criteriaMap.get(criteria)!.raw += unitValue;
                    criteriaMap.get(criteria)!.peopleeq += sipValue;
                } else {
                    criteriaMap.set(criteria, {
                        raw: unitValue,
                        peopleeq: sipValue,
                    });
                }
            });
        });

        this.impacts.forEach((impact) => {
            const criteria = impact.name;
            impact.title = this.translate.instant(`criteria.${criteria}.title`);
            impact.unite = this.translate.instant(`criteria.${criteria}.unite`);
            if (criteriaMap.has(criteria)) {
                impact.raw = criteriaMap.get(criteria)!.raw;
                impact.peopleeq = criteriaMap.get(criteria)!.peopleeq;
            }
        });
    }

    getTitleOrContent(textType: string) {
        this.selectedLang = this.translate.currentLang;
        const isBarChart = this.chartType() === "bar";
        const isServer = this.selectedParam === "Server";
        const isCloudService = this.selectedParam === Constants.CLOUD_SERVICE;
        const isBarChartChild = this.barChartChild === true;

        let translationKey: string;

        if (this.digitalService.isAi) {
            if (textType === "digital-services-card-title") {
                translationKey = "digital-services-cards.global-vision-ai";
            } else if (
                textType === "digital-services-card-content" &&
                this.aiRecommendation != null
            ) {
                try {
                    const recommendationsArr = JSON.parse(
                        this.aiRecommendation.recommendations,
                    );
                    if (
                        !Array.isArray(recommendationsArr) ||
                        recommendationsArr.length === 0
                    )
                        return "";
                    // Dynamic titles
                    const headers = Object.keys(recommendationsArr[0]);
                    // HTML table generation for recommendation
                    let table = `
                    <div style='overflow-x:auto;'>
                    <h4 style='font-weight:bold; margin-top:0px; font-size:1rem;'>Recommendations</h4>
                    <table style='width:100%;border-collapse:collapse;min-width:600px;'>
                    <thead><tr>`;
                    headers.forEach((h) => {
                        table += `<th style='padding:14px 18px;text-align:center;font-size:1rem;'>${h.charAt(0).toUpperCase() + h.slice(1)}</th>`;
                    });
                    table += `</tr></thead><tbody>`;
                    recommendationsArr.forEach((rec: any) => {
                        table += `<tr>`;
                        headers.forEach((h) => {
                            table += `<td style='padding:14px 18px;font-size:0.98rem;text-align:center;'>${rec[h]}</td>`;
                        });
                        table += `</tr>`;
                    });
                    table += `</tbody></table></div>`;
                    return table;
                } catch (error) {
                    console.error("Error parsing AI recommendations:", error);
                    return "";
                }
            }
        }

        if (isBarChart) {
            if (isBarChartChild && isServer) {
                translationKey = "digital-services-cards.server-lifecycle.";
            } else if (isBarChartChild && isCloudService) {
                translationKey = "digital-services-cards.cloud-lifecycle.";
            } else {
                translationKey = `digital-services-cards.${this.selectedParam.toLowerCase().replace(/\s+/g, "-")}.`;
            }
        } else {
            const criteriaKey = this.selectedCriteria.toLowerCase().replace(/\s+/g, "-");
            if (
                !Object.keys(this.globalStore.criteriaList()).includes(
                    this.selectedCriteria,
                )
            ) {
                translationKey = `digital-services-cards.${criteriaKey}.`;
            } else {
                return this.translate.instant(
                    this.getTranslationKey(this.selectedCriteria, textType),
                );
            }
        }

        return this.translate.instant(
            `${translationKey}${textType === "digital-services-card-title" ? "title" : "content"}`,
        );
    }

    getTranslationKey(param: string, textType: string) {
        const key = "criteria." + param.toLowerCase().replace(/ /g, "-") + "." + textType;
        return key;
    }

    getTNSTranslation(input: string) {
        return this.translate.instant("digital-services." + input);
    }

    updateInconsistent(event: any): void {
        this.showInconsitencyBtn = event;
    }

    ngOnDestroy() {
        // Clean store data
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
