/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { TranslateService } from "@ngx-translate/core";
import { StatusCountMap } from "src/app/core/interfaces/digital-service.interfaces";
import { FootprintCalculated, Impact } from "src/app/core/interfaces/footprint.interface";
import { Constants } from "src/constants";
import { DecimalsPipe } from "../../pipes/decimal.pipe";
import { IntegerPipe } from "../../pipes/integer.pipe";
import { getColorFormatter, getLabelFormatter } from "./graphs-mapper";

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
    return isInverted
        ? translate.instant(`criteria.${dimension}`).title
        : existingTranslation(dimension, selectedView, translate);
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
    for (const [
        index,
        impact,
    ] of criteriaCalculated?.impactsWithMaxDimensions?.entries() ?? []) {
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
export const createRadialChartConfig = (
    footprintCalculated: FootprintCalculated[],
    criteriaCountMap: StatusCountMap,
    isInverted: boolean,
    selectedView: string,
    enableDataInconsistency: boolean,
    translate: TranslateService,
    integerPipe: any,
    useCustomColors: boolean = false,
    getColorFn?: (data: string) => string,
    defaultColor?: string[],
) => {
    // useCustomColors and getColorFn for Application view only, getColorFn is a function that returns a color based on the data value
    // defaultColor for Equipment view getColorFn not available
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
