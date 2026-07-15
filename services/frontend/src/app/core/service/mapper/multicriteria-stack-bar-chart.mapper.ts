/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { StatusCountMap } from "src/app/core/interfaces/digital-service.interfaces";
import { FootprintCalculated, Impact } from "src/app/core/interfaces/footprint.interface";
import { Constants } from "src/constants";
import { IntegerPipe } from "../../pipes/integer.pipe";
import {
    getColorFormatter,
    getLabelFormatter,
    getUniqueColorFromText,
} from "./graphs-mapper";
import { getCriteriaDimensionTranslation } from "./inventory-multicriteria-graph-mapper";

/**
 * Configuration options for stack bar chart creation
 */
export interface StackBarChartConfig {
    footprints: FootprintCalculated[];
    criteriaCountMap: StatusCountMap;
    selectedView: string;
    enableDataInconsistency: boolean;
    isAxisInverted: boolean;
    translate: TranslateService;
    integerPipe: IntegerPipe;
}

/**
 * Creates tooltip formatter for stack bar chart
 */
const createStackBarTooltipFormatter = (
    isAxisInverted: boolean,
    selectedView: string,
    translate: TranslateService,
    integerPipe: IntegerPipe,
) => {
    return (params: any) => {
        return `
        <div style="display: flex; align-items: center; height: 30px;">
                <span style="display: inline-block; width: 10px; height: 10px; background-color: ${
                    params.color
                }; border-radius: 50%; margin-right: 5px;"></span>
                <span style="font-weight: bold; margin-right: 15px;">${params.seriesName}</span>
                <div>${getCriteriaDimensionTranslation(
                    !isAxisInverted,
                    params.name,
                    selectedView,
                    translate,
                )} : ${integerPipe.transform(
                    params.data,
                )} ${translate.instant("common.peopleeq-min")} </div>
            </div>`;
    };
};

/**
 * Creates stack bar chart configuration for multicriteria views with >5 criteria
 */
export const createStackBarChartConfig = (config: StackBarChartConfig): EChartsOption => {
    let showZoom = true;
    const {
        footprints,
        criteriaCountMap,
        selectedView,
        enableDataInconsistency,
        isAxisInverted,
        translate,
        integerPipe,
    } = config;

    // Sort footprints in descending order based on total.sip
    const sortedFootprints = [...footprints]
        .sort((a, b) => b.total.sip - a.total.sip)
        .map((footprint) => ({
            ...footprint,
            impacts: [...footprint.impacts].sort((a, b) =>
                a.criteria.localeCompare(b.criteria),
            ),
        }));

    // Extract x-axis categories
    const xAxis = sortedFootprints.map((f) => f.data);
    if (xAxis.length < Constants.MAX_NUMBER_OF_BARS_TO_BE_DISPLAYED) {
        showZoom = false;
    } // Show zoom if more than 10 footprints
    // Collect all unique criteria names
    const allCriteria = new Set<string>();
    sortedFootprints.forEach((item) => {
        item.impacts.forEach((impact: Impact) => {
            allCriteria.add(impact.criteria);
        });
    });

    // Build series array - one series per criterion
    const criteriaNamesArray = Array.from(allCriteria);
    const series: any[] = criteriaNamesArray.map((criteriaName, criteriaIndex) => {
        // For each criterion, collect its value across all footprints/dimensions
        const data = sortedFootprints.map((footprint) => {
            const impact = footprint.impacts.find(
                (imp: Impact) => imp.criteria === criteriaName,
            );
            return impact ? impact.sumSip : 0;
        });

        return {
            name:
                getCriteriaDimensionTranslation(
                    isAxisInverted,
                    criteriaName,
                    selectedView,
                    translate,
                ) ?? criteriaName,
            data: data,
            type: "bar",
            stack: "total",
            emphasis: {
                focus: "series",
            },
            itemStyle: {
                color: getUniqueColorFromText(criteriaName),
            },
        };
    });
    return {
        tooltip: {
            show: true,
            formatter: createStackBarTooltipFormatter(
                isAxisInverted,
                selectedView,
                translate,
                integerPipe,
            ),
        },
        grid: {
            left: "3%",
            right: "4%",
            bottom: "3%",
            containLabel: true,
        },
        legend: {
            selectedMode: false,
        },
        dataZoom: [
            {
                show: showZoom,
                startValue: xAxis[0],
                endValue: xAxis[Constants.MAX_NUMBER_OF_BARS_TO_BE_DISPLAYED - 1],
            },
        ],
        xAxis: [
            {
                type: "category",
                data: xAxis,

                axisLabel: {
                    rotate: 30,
                    color: (value: any) => {
                        const hasError = !!criteriaCountMap?.[value]?.status?.error;
                        return getColorFormatter(hasError, enableDataInconsistency);
                    },
                    formatter: (value: any) => {
                        const hasError = !!criteriaCountMap?.[value]?.status?.error;
                        const translatedValue = getCriteriaDimensionTranslation(
                            !isAxisInverted,
                            value,
                            selectedView,
                            translate,
                        );

                        return getLabelFormatter(
                            hasError,
                            enableDataInconsistency,
                            translatedValue,
                        );
                    },
                    rich: Constants.CHART_RICH as any,
                },
            },
        ],
        yAxis: [
            {
                type: "value",
            },
        ],
        series: series,
        color: Constants.BLUE_COLOR,
    } as any;
};
