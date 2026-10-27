/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { TranslateService } from "@ngx-translate/core";
import { StatusCountMap } from "src/app/core/interfaces/digital-service.interfaces";
import {
    ApplicationGraphLevel,
    ApplicationMultiCriteriaResponse,
    CriteriaCalculated,
    FootprintCalculated,
    Impact,
} from "src/app/core/interfaces/footprint.interface";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import { Constants } from "src/constants";
import { DecimalsPipe } from "../../pipes/decimal.pipe";
import { IntegerPipe } from "../../pipes/integer.pipe";
import { getColorFormatter, getLabelFormatter } from "./graphs-mapper";

type GraphStatus = { ok: number; error: number; total: number };
type CriteriaTotal = { impact: number; sip: number; max: Impact | null };

interface GraphBuckets {
    categories: Map<string, Map<string, Impact>>;
    categoryStatuses: Map<string, GraphStatus>;
    criteriaStatuses: Map<string, GraphStatus>;
    criteriaTotals: Map<string, CriteriaTotal>;
}

const emptyStatus = (): GraphStatus => ({ ok: 0, error: 0, total: 0 });

const emptyTotal = (): CriteriaTotal => ({ impact: 0, sip: 0, max: null });

const graphStatus = (
    countValue: number | undefined,
    consistentCount = 0,
    inconsistentCount = 0,
): GraphStatus => {
    const total = countValue ?? consistentCount + inconsistentCount;
    const ok =
        countValue === undefined
            ? consistentCount
            : Math.max(0, total - inconsistentCount);
    return { ok, error: inconsistentCount, total };
};

const addGraphStatus = (target: GraphStatus, status: GraphStatus): void => {
    target.ok += status.ok;
    target.error += status.error;
    target.total += status.total;
};

const graphCategory = (
    nodeLabel: string | null | undefined,
    repartitionLabel: string,
    graphLevel: ApplicationGraphLevel,
) =>
    graphLevel === "global" || !nodeLabel
        ? repartitionLabel
        : `${nodeLabel} / ${repartitionLabel}`;

const collectGraphBuckets = (
    response: ApplicationMultiCriteriaResponse[],
    graphLevel: ApplicationGraphLevel,
): GraphBuckets => {
    const buckets: GraphBuckets = {
        categories: new Map(),
        categoryStatuses: new Map(),
        criteriaStatuses: new Map(),
        criteriaTotals: new Map(),
    };
    for (const criterion of response) {
        const criterionStatus =
            buckets.criteriaStatuses.get(criterion.criteria) ?? emptyStatus();
        const criterionTotal =
            buckets.criteriaTotals.get(criterion.criteria) ?? emptyTotal();
        for (const node of criterion.nodes ?? []) {
            addGraphStatus(
                criterionStatus,
                graphStatus(
                    node.countValue,
                    node.consistentCount,
                    node.inconsistentCount,
                ),
            );
            for (const repartition of node.repartitions ?? []) {
                const category = graphCategory(node.label, repartition.label, graphLevel);
                const impact = {
                    criteria: criterion.criteria,
                    sumImpact: repartition.impact,
                    sumSip: repartition.sip,
                } as Impact;
                const categoryImpacts =
                    buckets.categories.get(category) ?? new Map<string, Impact>();
                categoryImpacts.set(criterion.criteria, impact);
                buckets.categories.set(category, categoryImpacts);

                const categoryStatus =
                    buckets.categoryStatuses.get(category) ?? emptyStatus();
                addGraphStatus(
                    categoryStatus,
                    graphStatus(
                        repartition.countValue,
                        repartition.consistentCount,
                        repartition.inconsistentCount,
                    ),
                );
                buckets.categoryStatuses.set(category, categoryStatus);

                criterionTotal.impact += repartition.impact ?? 0;
                criterionTotal.sip += repartition.sip ?? 0;
                if (
                    !criterionTotal.max ||
                    (repartition.sip ?? 0) > (criterionTotal.max.sumSip ?? 0)
                ) {
                    criterionTotal.max = {
                        ...impact,
                        criteria: category,
                    } as Impact;
                }
            }
        }
        buckets.criteriaStatuses.set(criterion.criteria, criterionStatus);
        buckets.criteriaTotals.set(criterion.criteria, criterionTotal);
    }
    return buckets;
};

const buildGraphFootprints = (
    response: ApplicationMultiCriteriaResponse[],
    buckets: GraphBuckets,
    isAxisInverted: boolean,
): FootprintCalculated[] => {
    if (isAxisInverted) {
        return response.map((criterion) => ({
            data: criterion.criteria,
            impacts: [...buckets.categories.entries()].map(
                ([category, values]) =>
                    ({
                        criteria: category,
                        sumImpact: values.get(criterion.criteria)?.sumImpact ?? 0,
                        sumSip: values.get(criterion.criteria)?.sumSip ?? 0,
                    }) as Impact,
            ),
            total: buckets.criteriaTotals.get(criterion.criteria) ?? {
                impact: 0,
                sip: 0,
                max: null,
            },
            status: buckets.criteriaStatuses.get(criterion.criteria) ?? emptyStatus(),
        }));
    }
    return [...buckets.categories.entries()].map(([category, values]) => {
        const impacts = response.map(
            (criterion) =>
                values.get(criterion.criteria) ??
                ({
                    criteria: criterion.criteria,
                    sumImpact: 0,
                    sumSip: 0,
                } as Impact),
        );
        const total = impacts.reduce(
            (sum, impact) => ({
                impact: sum.impact + (impact.sumImpact ?? 0),
                sip: sum.sip + (impact.sumSip ?? 0),
            }),
            { impact: 0, sip: 0 },
        );
        return {
            data: category,
            impacts,
            total,
            status: buckets.categoryStatuses.get(category) ?? emptyStatus(),
        };
    });
};

const buildCriteriaCountMap = (
    buckets: GraphBuckets,
    isAxisInverted: boolean,
): StatusCountMap => {
    const result: StatusCountMap = {};
    if (isAxisInverted) {
        for (const [category, status] of buckets.categoryStatuses) {
            result[category] = { status };
        }
    } else {
        for (const [criteria, status] of buckets.criteriaStatuses) {
            result[criteria] = { status };
        }
    }
    return result;
};

const buildMaxDimensionImpacts = (
    response: ApplicationMultiCriteriaResponse[],
    criteriaTotals: Map<string, CriteriaTotal>,
) =>
    response.map((criterion) => {
        const total = criteriaTotals.get(criterion.criteria) ?? emptyTotal();
        const max = total.max;
        return {
            name: criterion.criteria,
            title: criterion.criteria,
            unite: criterion.unit,
            raw: total.impact,
            peopleeq: total.sip,
            maxCriteria: {
                name: max?.criteria ?? "",
                raw: max?.sumImpact ?? 0,
                peopleeq: max?.sumSip ?? 0,
            },
        };
    });

export const transformApplicationMultiCriteriaGraph = (
    response: ApplicationMultiCriteriaResponse[],
    graphLevel: ApplicationGraphLevel,
    isAxisInverted: boolean,
): CriteriaCalculated => {
    const buckets = collectGraphBuckets(response, graphLevel);
    const footprints = buildGraphFootprints(response, buckets, isAxisInverted);
    return {
        footprints,
        hasError: [...buckets.categoryStatuses.values()].some(
            (status) => status.error > 0,
        ),
        total: footprints.reduce(
            (sum, footprint) => ({
                impact: sum.impact + footprint.total.impact,
                sip: sum.sip + footprint.total.sip,
            }),
            { impact: 0, sip: 0 },
        ),
        criteriasCount: buildCriteriaCountMap(buckets, isAxisInverted),
        impactsWithMaxDimensions: buildMaxDimensionImpacts(
            response,
            buckets.criteriaTotals,
        ),
    };
};

/**
 * Configuration options for radial chart creation
 */
export interface RadialChartConfig {
    footprintCalculated: FootprintCalculated[];
    criteriaCountMap: StatusCountMap;
    isInverted: boolean;
    selectedView: string;
    enableDataInconsistency: boolean;
    translate: TranslateService;
    integerPipe: IntegerPipe;
    useCustomColors?: boolean;
    getColorFn?: (data: string) => string;
    defaultColor?: string[];
}

/**
 * Translates a parameter using existing translations or returns the parameter itself
 */
export const existingTranslation = (
    param: string,
    view: string,
    translate: TranslateService,
    type: string = "series",
): string => {
    let key = view + "." + param;
    if (param === "other") {
        key = type === "legend" ? "common.otherLegend" : "common.other";
    }
    if (param === Constants.EMPTY) {
        return translate.instant("common.empty");
    }
    return translate.instant(key) === key ? param : translate.instant(key);
};

/**
 * Gets the translated name for a criteria or dimension based on axis inversion
 */
export const getCriteriaDimensionTranslation = (
    isInverted: boolean,
    dimension: string,
    selectedView: string,
    translate: TranslateService,
): string => {
    if (isInverted) {
        return translate.instant(`criteria.${dimension}`).title;
    }

    if (selectedView === "lifeCycle" || selectedView === Constants.ACV_STEP) {
        const translationKey = `acvStep.${dimension}`;
        const translatedDimension = translate.instant(translationKey);
        return translatedDimension === translationKey ? dimension : translatedDimension;
    }

    return existingTranslation(dimension, selectedView, translate);
};

/**
 * Sorts data source alphabetically
 */
export const sortAlphabetically = (dataSource: string[]): string[] => {
    return [...dataSource].sort((a, b) => a.localeCompare(b));
};

/**
 * Sorts data source by lifecycle order and translates using acvStep
 */
export const sortByLifecycleOrder = (
    dataSource: string[],
    translate: TranslateService,
): string[] => {
    return [...dataSource]
        .sort(
            (a, b) =>
                LifeCycleUtils.getLifeCycleList().indexOf(a) -
                LifeCycleUtils.getLifeCycleList().indexOf(b),
        )
        .map((criteria) => translate.instant(`acvStep.${criteria}`));
};

/**
 * Creates angle axis configuration for radial charts
 */
export const createAngleAxisConfig = (
    footprintCalculated: FootprintCalculated[],
    criteriaCountMap: StatusCountMap,
    isInverted: boolean,
    selectedView: string,
    enableDataInconsistency: boolean,
    translate: TranslateService,
) => {
    return {
        type: "category" as const,
        data: footprintCalculated[0].impacts.map((impact: Impact) => {
            return {
                value: impact.criteria,
                textStyle: {
                    color: getColorFormatter(
                        !!criteriaCountMap[impact.criteria]?.status?.error,
                        enableDataInconsistency,
                    ),
                },
            };
        }),
        axisLabel: {
            formatter: (value: any) => {
                const title = getCriteriaDimensionTranslation(
                    !isInverted,
                    value,
                    selectedView,
                    translate,
                );
                const hasError = !!criteriaCountMap[value]?.status?.error;
                return getLabelFormatter(hasError, enableDataInconsistency, title);
            },
            rich: Constants.CHART_RICH as any,
            margin: 26,
        },
    };
};

/**
 * Creates radius axis configuration for radial charts
 */
export const createRadiusAxisConfig = (translate: TranslateService) => {
    return {
        name: translate.instant("common.peopleeq"),
        nameLocation: "end" as const,
        nameGap: 21,
        nameTextStyle: {
            fontStyle: "italic" as const,
        },
    };
};

/**
 * Creates legend configuration for radial charts
 */
export const createLegendConfig = (
    footprintCalculated: FootprintCalculated[],
    isInverted: boolean,
    selectedView: string,
    translate: TranslateService,
) => {
    return {
        type: "scroll" as const,
        bottom: 0,
        data: footprintCalculated.map((item: FootprintCalculated) => item.data),
        formatter: (param: any) => {
            return getCriteriaDimensionTranslation(
                isInverted,
                param,
                selectedView,
                translate,
            );
        },
    };
};

/**
 * Creates tooltip configuration for radial multicriteria charts
 */
export const createTooltipConfig = (
    footprintCalculated: FootprintCalculated[],
    isInverted: boolean,
    selectedView: string,
    translate: TranslateService,
    integerPipe: IntegerPipe,
) => {
    return {
        show: true,
        formatter: (params: any) => {
            const dataIndex = params.dataIndex;
            const seriesIndex = params.seriesIndex;
            const impact = footprintCalculated[seriesIndex].impacts[dataIndex];
            const dimension = footprintCalculated[seriesIndex].data;

            const name = getCriteriaDimensionTranslation(
                isInverted,
                dimension,
                selectedView,
                translate,
            );
            return `
                <div style="display: flex; align-items: center; height: 30px;">
                    <span style="display: inline-block; width: 10px; height: 10px; background-color: ${
                        params.color
                    }; border-radius: 50%; margin-right: 5px;"></span>
                    <span style="font-weight: bold; margin-right: 15px;">${name}</span>
                    <div>${getCriteriaDimensionTranslation(
                        !isInverted,
                        impact.criteria,
                        selectedView,
                        translate,
                    )} : ${integerPipe.transform(
                        impact.sumSip,
                    )} ${translate.instant("common.peopleeq-min")} </div>
                </div>
            `;
        },
    };
};

/**
 * Creates series configuration for radial multicriteria charts
 */
export const createSeriesConfig = (
    footprintCalculated: FootprintCalculated[],
    translate: TranslateService,
    useCustomColors: boolean = false,
    getColorFn?: (data: string) => string,
) => {
    return footprintCalculated.map((item: FootprintCalculated) => {
        const baseConfig: any = {
            name: item.data,
            type: "bar",
            coordinateSystem: "polar",
            data: item.impacts.map((impact: Impact) => ({
                value: impact.sumSip,
                label: {
                    formatter: () => {
                        return [
                            impact.sumImpact,
                            translate.instant(`criteria.${impact.criteria}`).unit,
                        ].join(" ");
                    },
                },
            })),
            stack: "a",
            emphasis: {
                focus: "series",
            },
        };

        // used for application view only
        if (useCustomColors && getColorFn) {
            baseConfig.itemStyle = {
                color: getColorFn(item.data),
            };
        }

        return baseConfig;
    });
};

/**
 * Generates text description for multi - criteria impacts
 */
export const getTextDescription = (
    translationKey: string,
    criteriaCalculated: any,
    translate: TranslateService,
    integerPipe: IntegerPipe,
    decimalsPipe: DecimalsPipe,
): { textDescription: string; textImpacts: any[] } => {
    let textDescription = "";
    let textImpacts = [];
    const firstPrefix = translate.instant(
        `${translationKey}text-description-first-prefix`,
    );
    const iteratePrefix = translate.instant(
        `${translationKey}text-description-iterate-prefix`,
    );
    const topImpacts = [...(criteriaCalculated?.impactsWithMaxDimensions ?? [])]
        .sort((a, b) => (b.peopleeq ?? 0) - (a.peopleeq ?? 0))
        .slice(0, 3);
    for (const [index, impact] of topImpacts.entries()) {
        const prefix = index === 0 ? firstPrefix : iteratePrefix;

        if (index === 0) {
            textDescription += translate.instant(`${translationKey}text-description`);
        }
        textImpacts.push({
            text:
                prefix +
                translate.instant(`${translationKey}text-description-iterate`, {
                    impactName: impact.title,
                    impactValue: integerPipe.transform(impact.peopleeq),
                    resource: impact.maxCriteria.name,
                    resourceValue: integerPipe.transform(impact.maxCriteria.peopleeq),
                    rawValue: decimalsPipe.transform(impact.raw),
                    unit: impact.unite,
                    resourceRawValue: decimalsPipe.transform(impact.maxCriteria.raw),
                    resourceUnit: impact.unite,
                }),
            impactName: impact.name,
            impactNameVisible: impact.title,
        });
    }
    return { textDescription, textImpacts };
};

/**
 * Creates complete radial chart configuration for multicriteria views
 */
export const createRadialChartConfig = (config: RadialChartConfig) => {
    const {
        footprintCalculated,
        criteriaCountMap,
        isInverted,
        selectedView,
        enableDataInconsistency,
        translate,
        integerPipe,
        useCustomColors = false,
        getColorFn,
        defaultColor,
    } = config;

    return {
        tooltip: createTooltipConfig(
            footprintCalculated,
            isInverted,
            selectedView,
            translate,
            integerPipe,
        ),
        angleAxis: createAngleAxisConfig(
            footprintCalculated,
            criteriaCountMap,
            isInverted,
            selectedView,
            enableDataInconsistency,
            translate,
        ),
        radiusAxis: createRadiusAxisConfig(translate),
        polar: {
            radius: "62%",
            center: ["50%", "47%"],
        },
        series: createSeriesConfig(
            footprintCalculated,
            translate,
            useCustomColors,
            getColorFn,
        ),
        avoidLabelOverlap: true,
        legend: createLegendConfig(
            footprintCalculated,
            isInverted,
            selectedView,
            translate,
        ),
        ...(defaultColor && { color: defaultColor }),
    };
};
