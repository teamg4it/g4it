/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import {
    color as echartsColor,
    type EChartsOption,
    type SunburstSeriesOption,
} from "echarts";
import { Constants } from "src/constants";

/**
 * Color the sunburst nodes based on their depth and sibling index.
 * @param nodes the list of nodes to color
 * @param parentColor the color of the parent node, if any
 * @param depth the depth of the current node, starting at 0
 * @returns the list of nodes with colors applied
 */
function colorSunburstNodes(
    nodes: SunburstSeriesOption["data"],
    parentColor?: string,
    depth = 0,
): SunburstSeriesOption["data"] {
    if (!Array.isArray(nodes)) return nodes;

    return nodes.map((node: any, index) => {
        const baseColor =
            parentColor ?? Constants.COLOR[index % Constants.COLOR.length];
        // Start children at 28% lighter, add 20% per depth and up to 8% across siblings; cap at 70%.
        const amount = Math.min(
            0.7,
            0.28 +
            Math.max(depth - 1, 0) * 0.2 +
            (index / Math.max(nodes.length - 1, 1)) * 0.08,
        );
        const color = depth === 0 ? baseColor : echartsColor.lift(baseColor, amount);

        return {
            ...node,
            ...(node.children
                ? { children: colorSunburstNodes(node.children, baseColor, depth + 1) }
                : {}),
            itemStyle: { ...node.itemStyle, color },
        };
    });
}

/**
 * Create a sunburst chart configuration for ECharts.
 * @param param the parameters for the chart configuration
 * @param param.data the data for the sunburst chart
 * @param param.seriesOptions optional series options to override defaults
 * @param param.chartOptions optional chart options to override defaults
 * @returns the ECharts option object for the sunburst chart
 */
export function createSunburstChartConfig({ data, seriesOptions, chartOptions }: {
    data: SunburstSeriesOption["data"]
    seriesOptions?: Omit<SunburstSeriesOption, "type" | "data">
    chartOptions?: Omit<EChartsOption, "series">
}): EChartsOption {
    return {
        tooltip: { trigger: "item" },
        ...chartOptions,
        color: Constants.COLOR,
        series: [
            {
                type: "sunburst",
                radius: ['10%', '85%'],
                label: { minAngle: 5 },
                levels: [
                    {},
                    { radius: ['10%', '46%'] },
                    { radius: ['46%', '82%'] },
                    {
                        radius: ['82%', '85%'],
                        label: {
                            position: 'outside',
                        },
                    },
                ],
                itemStyle: {
                    borderRadius: '5',
                },
                ...seriesOptions,
                data: colorSunburstNodes(data),
            },
        ],
    }
}
