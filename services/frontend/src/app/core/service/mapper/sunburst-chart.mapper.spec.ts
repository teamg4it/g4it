import { Constants } from "src/constants";
import { createSunburstChartConfig } from "./sunburst-chart.mapper";

describe("createSunburstChartConfig", () => {
    it("applies default sunburst options and root palette colors", () => {
        const chart: any = createSunburstChartConfig({
            data: [{ name: "Devices" }, { name: "Infrastructure" }],
        });

        expect(chart.tooltip).toEqual({ trigger: "item" });
        expect(chart.color).toEqual(Constants.COLOR);
        expect(chart.series).toHaveSize(1);
        expect(chart.series[0]).toEqual(
            jasmine.objectContaining({
                type: "sunburst",
                radius: ["10%", "85%"],
                label: { minAngle: 5 },
                itemStyle: { borderRadius: "5" },
            }),
        );
        expect(chart.series[0].data.map((node: any) => node.itemStyle.color)).toEqual(
            Constants.COLOR.slice(0, 2),
        );
    });

    it("colors descendants from their root and preserves node styles and data", () => {
        const chart: any = createSunburstChartConfig({
            data: [
                {
                    name: "Root",
                    itemStyle: { borderColor: "black" },
                    children: [
                        {
                            name: "Child",
                            children: [{ name: "Leaf", value: 3 }],
                        },
                    ],
                },
            ],
        });
        const [root] = chart.series[0].data;
        const [child] = root.children;
        const [leaf] = child.children;

        expect(root.itemStyle).toEqual({
            borderColor: "black",
            color: Constants.COLOR[0],
        });
        expect(child.itemStyle.color).toEqual(jasmine.any(String));
        expect(child.itemStyle.color).not.toBe(root.itemStyle.color);
        expect(leaf.itemStyle.color).not.toBe(child.itemStyle.color);
        expect(leaf.value).toBe(3);
    });

    it("allows chart and series options to override defaults", () => {
        const chart: any = createSunburstChartConfig({
            data: [],
            chartOptions: { tooltip: { trigger: "axis" }, backgroundColor: "white" },
            seriesOptions: { radius: ["20%", "70%"], clockwise: false },
        });

        expect(chart.tooltip).toEqual({ trigger: "axis" });
        expect(chart.backgroundColor).toBe("white");
        expect(chart.series[0].radius).toEqual(["20%", "70%"]);
        expect(chart.series[0].clockwise).toBeFalse();
    });

    it("preserves absent data", () => {
        const chart: any = createSunburstChartConfig({ data: undefined });

        expect(chart.series[0].data).toBeUndefined();
    });
});
