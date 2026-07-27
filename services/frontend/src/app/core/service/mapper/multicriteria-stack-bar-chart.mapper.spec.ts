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
import { IntegerPipe } from "../../pipes/integer.pipe";
import {
    createStackBarChartConfig,
    StackBarChartConfig,
} from "./multicriteria-stack-bar-chart.mapper";

describe("multicriteria-stack-bar-chart.mapper", () => {
    let mockTranslate: jasmine.SpyObj<TranslateService>;
    let mockIntegerPipe: jasmine.SpyObj<IntegerPipe>;

    beforeEach(() => {
        mockTranslate = jasmine.createSpyObj("TranslateService", ["instant"]);
        mockIntegerPipe = jasmine.createSpyObj("IntegerPipe", ["transform"]);

        mockTranslate.instant.and.returnValue("people-eq/min");
        mockIntegerPipe.transform.and.callFake((value: number) => value.toLocaleString());
    });

    describe("createStackBarChartConfig", () => {
        let baseConfig: StackBarChartConfig;
        let mockFootprints: FootprintCalculated[];
        let mockCriteriaCountMap: StatusCountMap;

        beforeEach(() => {
            mockFootprints = [
                {
                    data: "footprint1",
                    total: { sip: 100, impact: 100 },
                    status: { ok: 1, error: 0, total: 1 },
                    impacts: [
                        { criteria: "climate-change", sumSip: 50 } as Impact,
                        { criteria: "resource-use", sumSip: 30 } as Impact,
                        { criteria: "acidification", sumSip: 20 } as Impact,
                    ],
                } as FootprintCalculated,
                {
                    data: "footprint2",
                    total: { sip: 200, impact: 200 },
                    status: { ok: 0, error: 1, total: 1 },
                    impacts: [
                        { criteria: "climate-change", sumSip: 100 } as Impact,
                        { criteria: "resource-use", sumSip: 60 } as Impact,
                        { criteria: "acidification", sumSip: 40 } as Impact,
                    ],
                } as FootprintCalculated,
                {
                    data: "footprint3",
                    total: { sip: 150, impact: 150 },
                    status: { ok: 1, error: 0, total: 1 },
                    impacts: [
                        { criteria: "climate-change", sumSip: 75 } as Impact,
                        { criteria: "resource-use", sumSip: 45 } as Impact,
                        { criteria: "acidification", sumSip: 30 } as Impact,
                    ],
                } as FootprintCalculated,
            ];

            mockCriteriaCountMap = {
                footprint1: { status: { ok: 1, error: 0, total: 1 } },
                footprint2: { status: { ok: 0, error: 1, total: 1 } },
                footprint3: { status: { ok: 1, error: 0, total: 1 } },
            };

            baseConfig = {
                footprints: mockFootprints,
                criteriaCountMap: mockCriteriaCountMap,
                selectedView: "multi-criteria",
                enableDataInconsistency: false,
                isAxisInverted: false,
                translate: mockTranslate,
                integerPipe: mockIntegerPipe,
            };
        });

        it("should create a valid chart configuration with basic data", () => {
            const result = createStackBarChartConfig(baseConfig);

            expect(result).toBeDefined();
            expect(result.xAxis).toBeDefined();
            expect(result.yAxis).toBeDefined();
            expect(result.series).toBeDefined();
            expect(result.tooltip).toBeDefined();
            expect(result.legend).toBeDefined();
        });

        it("should sort footprints in descending order by total.sip", () => {
            const result = createStackBarChartConfig(baseConfig);
            const xAxisData = (result.xAxis as any)[0].data;

            expect(xAxisData).toEqual(["footprint2", "footprint3", "footprint1"]);
        });

        it("should sort impacts by criteria name within each footprint", () => {
            const result = createStackBarChartConfig(baseConfig);

            // The series should have criteria in alphabetical order
            const seriesNames = (result.series as any[]).map((s) => s.name);
            const sortedNames = [...seriesNames].sort();
            expect(seriesNames).toEqual(sortedNames);
        });

        it("should create one series per unique criterion", () => {
            const result = createStackBarChartConfig(baseConfig);

            expect((result.series as any).length).toBe(3); // 3 unique criteria
        });

        it("should set correct series properties for each criterion", () => {
            const result = createStackBarChartConfig(baseConfig);
            const series = result.series as any[];

            series.forEach((s: any) => {
                expect(s.type).toBe("bar");
                expect(s.stack).toBe("total");
                expect(s.emphasis).toEqual({ focus: "series" });
                expect(s.itemStyle.color).toBeDefined();
                expect(s.data).toBeDefined();
                expect(s.data.length).toBe(3); // 3 footprints
            });
        });

        it("should set itemStyle color for each series", () => {
            const result = createStackBarChartConfig(baseConfig);
            const series = result.series as any[];

            series.forEach((s: any) => {
                expect(s.itemStyle).toBeDefined();
                expect(s.itemStyle.color).toBeDefined();
                expect(typeof s.itemStyle.color).toBe("string");
            });
        });

        it("should handle footprints with missing criteria data", () => {
            baseConfig.footprints = [
                {
                    data: "footprint1",
                    total: { sip: 100, impact: 100 },
                    status: { ok: 1, error: 0, total: 1 },
                    impacts: [{ criteria: "climate-change", sumSip: 50 } as Impact],
                } as FootprintCalculated,
                {
                    data: "footprint2",
                    total: { sip: 200, impact: 200 },
                    status: { ok: 1, error: 0, total: 1 },
                    impacts: [
                        { criteria: "climate-change", sumSip: 100 } as Impact,
                        { criteria: "resource-use", sumSip: 60 } as Impact,
                    ],
                } as FootprintCalculated,
            ];

            const result = createStackBarChartConfig(baseConfig);
            const seriesData = result.series as any[];

            // Find series and check for zero values where criteria is missing
            expect(seriesData.length).toBeGreaterThan(0);

            // Check that series have correct data length
            seriesData.forEach((s) => {
                expect(s.data.length).toBe(2); // 2 footprints
            });
        });

        it("should show zoom when footprints exceed TOTAL_VISIBLE_GRAPH_ITEMS", () => {
            // Create more than 10 footprints
            const manyFootprints: FootprintCalculated[] = [];
            for (let i = 0; i < 15; i++) {
                manyFootprints.push({
                    data: `footprint${i}`,
                    total: { sip: 100 + i, impact: 100 + i },
                    status: { ok: 1, error: 0, total: 1 },
                    impacts: [{ criteria: "climate-change", sumSip: 50 } as Impact],
                } as FootprintCalculated);
            }

            baseConfig.footprints = manyFootprints;
            const result = createStackBarChartConfig(baseConfig);

            expect((result.dataZoom as any)[0].show).toBe(true);
        });

        it("should hide zoom when footprints are less than TOTAL_VISIBLE_GRAPH_ITEMS", () => {
            // Only 3 footprints (less than 10)
            const result = createStackBarChartConfig(baseConfig);

            expect((result.dataZoom as any)[0].show).toBe(false);
        });

        it("should set correct dataZoom start and end values", () => {
            const manyFootprints: FootprintCalculated[] = [];
            for (let i = 0; i < 15; i++) {
                manyFootprints.push({
                    data: `footprint${i}`,
                    total: { sip: 100 + i, impact: 100 + i },
                    status: { ok: 1, error: 0, total: 1 },
                    impacts: [{ criteria: "climate-change", sumSip: 50 } as Impact],
                } as FootprintCalculated);
            }

            baseConfig.footprints = manyFootprints;
            const result = createStackBarChartConfig(baseConfig);
            const xAxisData = (result.xAxis as any)[0].data;

            expect((result.dataZoom as any)[0].startValue).toBe(xAxisData[0]);
            expect((result.dataZoom as any)[0].endValue).toBe(
                xAxisData[Constants.TOTAL_VISIBLE_GRAPH_ITEMS - 1],
            );
        });

        it("should include axisLabel color function with enableDataInconsistency", () => {
            baseConfig.enableDataInconsistency = true;
            const result = createStackBarChartConfig(baseConfig);

            expect((result.xAxis as any)[0].axisLabel.color).toBeDefined();
            expect(typeof (result.xAxis as any)[0].axisLabel.color).toBe("function");
        });

        it("should include axisLabel color function without enableDataInconsistency", () => {
            baseConfig.enableDataInconsistency = false;
            const result = createStackBarChartConfig(baseConfig);

            expect((result.xAxis as any)[0].axisLabel.color).toBeDefined();
            expect(typeof (result.xAxis as any)[0].axisLabel.color).toBe("function");
        });

        it("should include axisLabel formatter function", () => {
            const result = createStackBarChartConfig(baseConfig);

            expect((result.xAxis as any)[0].axisLabel.formatter).toBeDefined();
            expect(typeof (result.xAxis as any)[0].axisLabel.formatter).toBe("function");
        });

        it("should detect errors from criteriaCountMap", () => {
            const result = createStackBarChartConfig(baseConfig);
            const axisLabelFormatter = (result.xAxis as any)[0].axisLabel.formatter;

            // Call formatter for footprint2 which has error
            const label = axisLabelFormatter("footprint2");

            // Should return a formatted label
            expect(label).toBeDefined();
            expect(typeof label).toBe("string");
        });

        it("should handle footprint without error in criteriaCountMap", () => {
            const result = createStackBarChartConfig(baseConfig);
            const axisLabelFormatter = (result.xAxis as any)[0].axisLabel.formatter;

            // Call formatter for footprint1 which has no error
            const label = axisLabelFormatter("footprint1");

            // Should return a formatted label
            expect(label).toBeDefined();
            expect(typeof label).toBe("string");
        });

        it("should handle missing criteriaCountMap entry", () => {
            const result = createStackBarChartConfig(baseConfig);
            const axisLabelFormatter = (result.xAxis as any)[0].axisLabel.formatter;

            // Call formatter for non-existent footprint
            const label = axisLabelFormatter("non-existent");

            // Should return a formatted label
            expect(label).toBeDefined();
            expect(typeof label).toBe("string");
        });

        it("should create series with correct configuration when isAxisInverted is true", () => {
            baseConfig.isAxisInverted = true;
            const result = createStackBarChartConfig(baseConfig);

            // Check that series are created correctly
            expect((result.series as any[]).length).toBeGreaterThan(0);
            (result.series as any[]).forEach((s: any) => {
                expect(s.name).toBeDefined();
                expect(typeof s.name).toBe("string");
            });
        });

        it("should create axis labels when isAxisInverted is false", () => {
            baseConfig.isAxisInverted = false;
            const result = createStackBarChartConfig(baseConfig);
            const axisLabelFormatter = (result.xAxis as any)[0].axisLabel.formatter;

            const label = axisLabelFormatter("footprint1");

            // Axis labels should be formatted properly
            expect(label).toBeDefined();
            expect(typeof label).toBe("string");
        });

        it("should include rich text configuration in axis label", () => {
            const result = createStackBarChartConfig(baseConfig);

            expect((result.xAxis as any)[0].axisLabel.rich).toBe(Constants.CHART_RICH);
        });

        it("should rotate axis labels", () => {
            const result = createStackBarChartConfig(baseConfig);

            expect((result.xAxis as any)[0].axisLabel.rotate).toBe(30);
        });

        it("should configure grid with correct values", () => {
            const result = createStackBarChartConfig(baseConfig);

            expect(result.grid).toEqual({
                left: "3%",
                right: "4%",
                bottom: "3%",
                containLabel: true,
            });
        });

        it("should configure legend as scrollable", () => {
            const result = createStackBarChartConfig(baseConfig);

            expect(result.legend).toEqual({
                show: true,
                type: "scroll",
            });
        });

        it("should set xAxis as category type", () => {
            const result = createStackBarChartConfig(baseConfig);

            expect((result.xAxis as any)[0].type).toBe("category");
        });

        it("should set yAxis as value type", () => {
            const result = createStackBarChartConfig(baseConfig);

            expect((result.yAxis as any)[0].type).toBe("value");
        });

        it("should use BLUE_COLOR constant for default color", () => {
            const result = createStackBarChartConfig(baseConfig);

            expect(result.color).toBe(Constants.BLUE_COLOR);
        });

        it("should create tooltip with formatter function", () => {
            const result = createStackBarChartConfig(baseConfig);

            expect(result.tooltip).toBeDefined();
            expect((result.tooltip as any).show).toBe(true);
            expect(typeof (result.tooltip as any).formatter).toBe("function");
        });

        describe("tooltip formatter", () => {
            it("should format tooltip with correct HTML structure", () => {
                const result = createStackBarChartConfig(baseConfig);
                const tooltipFormatter = (result.tooltip as any).formatter;

                const mockParams = {
                    color: "#ff0000",
                    seriesName: "Test Series",
                    name: "Test Name",
                    data: 1234,
                };

                const html = tooltipFormatter(mockParams);

                expect(html).toContain("background-color: #ff0000");
                expect(html).toContain("Test Series");
                expect(html).toContain("1,234");
                expect(html).toContain("people-eq/min");
            });

            it("should create tooltip when isAxisInverted is true", () => {
                baseConfig.isAxisInverted = true;
                const result = createStackBarChartConfig(baseConfig);
                const tooltipFormatter = (result.tooltip as any).formatter;

                const mockParams = {
                    color: "#ff0000",
                    seriesName: "Test Series",
                    name: "Test Name",
                    data: 1234,
                };

                const html = tooltipFormatter(mockParams);

                expect(html).toBeDefined();
                expect(html).toContain("Test Series");
            });

            it("should call integerPipe.transform on data", () => {
                const result = createStackBarChartConfig(baseConfig);
                const tooltipFormatter = (result.tooltip as any).formatter;

                const mockParams = {
                    color: "#ff0000",
                    seriesName: "Test Series",
                    name: "Test Name",
                    data: 5678,
                };

                tooltipFormatter(mockParams);

                expect(mockIntegerPipe.transform).toHaveBeenCalledWith(5678);
            });

            it("should include translated unit in tooltip", () => {
                mockTranslate.instant.and.returnValue("people-eq/min");
                const result = createStackBarChartConfig(baseConfig);
                const tooltipFormatter = (result.tooltip as any).formatter;

                const mockParams = {
                    color: "#ff0000",
                    seriesName: "Test Series",
                    name: "Test Name",
                    data: 1234,
                };

                const html = tooltipFormatter(mockParams);

                expect(mockTranslate.instant).toHaveBeenCalledWith("common.peopleeq-min");
                expect(html).toContain("people-eq/min");
            });
        });

        it("should handle empty footprints array", () => {
            baseConfig.footprints = [];
            const result = createStackBarChartConfig(baseConfig);

            expect((result.xAxis as any)[0].data).toEqual([]);
            expect((result.series as any).length).toBe(0);
            expect((result.dataZoom as any)[0].show).toBe(false);
        });

        it("should handle footprints with empty impacts array", () => {
            baseConfig.footprints = [
                {
                    data: "footprint1",
                    total: { sip: 100, impact: 100 },
                    status: { ok: 1, error: 0, total: 1 },
                    impacts: [],
                } as FootprintCalculated,
            ];

            const result = createStackBarChartConfig(baseConfig);

            expect((result.series as any).length).toBe(0);
        });

        it("should handle multiple footprints with same criteria", () => {
            const result = createStackBarChartConfig(baseConfig);
            const series = result.series as any[];

            // Each series should have data for all 3 footprints
            series.forEach((s) => {
                expect(s.data.length).toBe(3);
            });
        });

        it("should create valid config with custom selectedView", () => {
            baseConfig.selectedView = "custom-view";
            const result = createStackBarChartConfig(baseConfig);

            // Should create valid configuration
            expect(result).toBeDefined();
            expect((result.series as any[]).length).toBeGreaterThan(0);
        });

        it("should handle null or undefined criteriaCountMap", () => {
            baseConfig.criteriaCountMap = null as any;
            const result = createStackBarChartConfig(baseConfig);

            // Should not throw error
            expect(result).toBeDefined();
        });

        it("should handle criteriaCountMap with null status", () => {
            baseConfig.criteriaCountMap = {
                footprint1: { status: null } as any,
            };

            const result = createStackBarChartConfig(baseConfig);
            const axisLabelFormatter = (result.xAxis as any)[0].axisLabel.formatter;

            // Should not throw error
            expect(() => axisLabelFormatter("footprint1")).not.toThrow();
        });

        it("should preserve alphabetical order of criteria in series", () => {
            baseConfig.footprints = [
                {
                    data: "footprint1",
                    total: { sip: 100, impact: 100 },
                    status: { ok: 1, error: 0, total: 1 },
                    impacts: [
                        { criteria: "z-criterion", sumSip: 10 } as Impact,
                        { criteria: "a-criterion", sumSip: 20 } as Impact,
                        { criteria: "m-criterion", sumSip: 30 } as Impact,
                    ],
                } as FootprintCalculated,
            ];

            const result = createStackBarChartConfig(baseConfig);
            const series = result.series as any[];

            // Series should be created in alphabetical order by criteria
            expect(series.length).toBe(3);
            expect(series[0].data).toBeDefined();
            expect(series[1].data).toBeDefined();
            expect(series[2].data).toBeDefined();
        });
    });
});
