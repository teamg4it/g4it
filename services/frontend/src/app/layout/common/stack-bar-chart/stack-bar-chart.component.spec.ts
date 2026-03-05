import { ComponentRef } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { StatusCountMap } from "src/app/core/interfaces/digital-service.interfaces";
import { SharedModule } from "src/app/core/shared/shared.module";
import { Constants } from "src/constants";
import { StackBarChartComponent } from "./stack-bar-chart.component";

describe("StackBarChartComponent", () => {
    let component: StackBarChartComponent;
    let fixture: ComponentFixture<StackBarChartComponent>;
    let componentRef: ComponentRef<StackBarChartComponent>;
    let translateService: TranslateService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [StackBarChartComponent],
            imports: [TranslateModule.forRoot(), SharedModule],
        }).compileComponents();

        fixture = TestBed.createComponent(StackBarChartComponent);
        component = fixture.componentInstance;
        componentRef = fixture.componentRef;
        translateService = TestBed.inject(TranslateService);
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    describe("Input signals", () => {
        it("should initialize xAxisInput with empty array by default", () => {
            expect(component.xAxisInput()).toEqual([]);
        });

        it("should initialize statusCountMap with empty object by default", () => {
            expect(component.statusCountMap()).toEqual({});
        });

        it("should initialize cursor with 'pointer' by default", () => {
            expect(component.cursor()).toBe("pointer");
        });

        it("should set xAxisInput", () => {
            const xAxisData = ["Item1", "Item2", "Item3"];
            componentRef.setInput("xAxisInput", xAxisData);
            fixture.detectChanges();

            expect(component.xAxisInput()).toEqual(xAxisData);
        });

        it("should set statusCountMap", () => {
            const statusMap: StatusCountMap = {
                Item1: { status: { ok: 10, error: 2, total: 12 } },
                Item2: { status: { ok: 8, error: 4, total: 12 } },
            };
            componentRef.setInput("statusCountMap", statusMap);
            fixture.detectChanges();

            expect(component.statusCountMap()).toEqual(statusMap);
        });

        it("should set cursor", () => {
            componentRef.setInput("cursor", "default");
            fixture.detectChanges();

            expect(component.cursor()).toBe("default");
        });
    });

    describe("Output events", () => {
        it("should have selectedStackBarClick EventEmitter", () => {
            expect(component.selectedStackBarClick).toBeDefined();
        });

        it("should emit selectedStackBarClick when onChartClick is called", () => {
            const emitSpy = spyOn(component.selectedStackBarClick, "emit");
            const params = { name: "Item1" };

            component.onChartClick(params);

            expect(emitSpy).toHaveBeenCalledWith("Item1");
        });

        it("should emit correct item name from chart click", () => {
            const emitSpy = spyOn(component.selectedStackBarClick, "emit");
            const testName = "TestDigitalService";

            component.onChartClick({ name: testName, value: 100 });

            expect(emitSpy).toHaveBeenCalledWith(testName);
        });

        it("should emit undefined when params.name is undefined", () => {
            const emitSpy = spyOn(component.selectedStackBarClick, "emit");

            component.onChartClick({});

            expect(emitSpy).toHaveBeenCalledWith(undefined);
        });
    });

    describe("renderStackChart", () => {
        it("should return EChartsOption object", () => {
            const option = component.renderStackChart();

            expect(option).toBeDefined();
            expect(option.xAxis).toBeDefined();
            expect(option.yAxis).toBeDefined();
            expect(option.series).toBeDefined();
        });

        it("should set xAxis data from xAxisInput", () => {
            const xAxisData = ["DS1", "DS2", "DS3"];
            componentRef.setInput("xAxisInput", xAxisData);
            fixture.detectChanges();

            const option = component.renderStackChart();

            expect((option.xAxis as any).data).toEqual(xAxisData);
        });

        it("should configure xAxis with category type", () => {
            const option = component.renderStackChart();

            expect((option.xAxis as any).type).toBe("category");
        });

        it("should configure xAxis labels to show all with rotation", () => {
            const option = component.renderStackChart();

            expect((option.xAxis as any).axisLabel.interval).toBe(0);
            expect((option.xAxis as any).axisLabel.rotate).toBe(30);
        });

        it("should configure yAxis with value type", () => {
            const option = component.renderStackChart();

            expect((option.yAxis as any).type).toBe("value");
        });

        it("should use cursor from input signal", () => {
            componentRef.setInput("cursor", "crosshair");
            fixture.detectChanges();

            const option = component.renderStackChart();

            expect((option as any).cursor).toBe("crosshair");
        });

        it("should set legend with selectedMode false", () => {
            const option = component.renderStackChart();

            expect((option.legend as any).selectedMode).toBe(false);
        });

        it("should set grid with correct positioning", () => {
            const option = component.renderStackChart();

            expect((option.grid as any).left).toBe("3%");
            expect((option.grid as any).right).toBe("4%");
            expect((option.grid as any).bottom).toBe("3%");
            expect((option.grid as any).containLabel).toBe(true);
        });

        it("should use Constants colors for chart", () => {
            const option = component.renderStackChart();

            expect((option as any).color).toEqual([
                Constants.GRAPH_BLUE,
                Constants.GRAPH_RED,
            ]);
        });
    });

    describe("dataZoom configuration", () => {
        it("should not show dataZoom when items <= TOTAL_VISIBLE_GRAPH_ITEMS", () => {
            const xAxisData = ["DS1", "DS2", "DS3"];
            componentRef.setInput("xAxisInput", xAxisData);
            fixture.detectChanges();

            const option = component.renderStackChart();

            expect((option as any).dataZoom).toEqual([]);
        });

        it("should show dataZoom when items > TOTAL_VISIBLE_GRAPH_ITEMS", () => {
            const xAxisData = Array.from({ length: 15 }, (_, i) => `DS${i + 1}`);
            componentRef.setInput("xAxisInput", xAxisData);
            fixture.detectChanges();

            const option = component.renderStackChart();

            expect((option as any).dataZoom).toBeDefined();
            expect((option as any).dataZoom.length).toBe(2);
        });

        it("should configure slider dataZoom correctly", () => {
            const xAxisData = Array.from({ length: 20 }, (_, i) => `DS${i + 1}`);
            componentRef.setInput("xAxisInput", xAxisData);
            fixture.detectChanges();

            const option = component.renderStackChart();
            const sliderZoom = (option as any).dataZoom[0];

            expect(sliderZoom.type).toBe("slider");
            expect(sliderZoom.show).toBe(true);
            expect(sliderZoom.xAxisIndex).toBe(0);
            expect(sliderZoom.start).toBe(0);
        });

        it("should configure inside dataZoom correctly", () => {
            const xAxisData = Array.from({ length: 20 }, (_, i) => `DS${i + 1}`);
            componentRef.setInput("xAxisInput", xAxisData);
            fixture.detectChanges();

            const option = component.renderStackChart();
            const insideZoom = (option as any).dataZoom[1];

            expect(insideZoom.type).toBe("inside");
            expect(insideZoom.xAxisIndex).toBe(0);
            expect(insideZoom.start).toBe(0);
        });

        it("should calculate correct end percentage for dataZoom", () => {
            const totalItems = 20;
            const visibleItems = Constants.TOTAL_VISIBLE_GRAPH_ITEMS;
            const expectedEnd = (visibleItems / totalItems) * 100;
            const xAxisData = Array.from({ length: totalItems }, (_, i) => `DS${i + 1}`);
            componentRef.setInput("xAxisInput", xAxisData);
            fixture.detectChanges();

            const option = component.renderStackChart();
            const sliderZoom = (option as any).dataZoom[0];

            expect(sliderZoom.end).toBe(expectedEnd);
        });

        it("should handle exactly TOTAL_VISIBLE_GRAPH_ITEMS items", () => {
            const xAxisData = Array.from(
                { length: Constants.TOTAL_VISIBLE_GRAPH_ITEMS },
                (_, i) => `DS${i + 1}`,
            );
            componentRef.setInput("xAxisInput", xAxisData);
            fixture.detectChanges();

            const option = component.renderStackChart();

            expect((option as any).dataZoom).toEqual([]);
        });
    });

    describe("series configuration", () => {
        it("should create two series", () => {
            const option = component.renderStackChart();

            expect((option.series as any[]).length).toBe(2);
        });

        it("should configure series with bar type", () => {
            const option = component.renderStackChart();
            const series = option.series as any[];

            series.forEach((s) => {
                expect(s.type).toBe("bar");
            });
        });

        it("should configure series with stack total", () => {
            const option = component.renderStackChart();
            const series = option.series as any[];

            series.forEach((s) => {
                expect(s.stack).toBe("total");
            });
        });

        it("should set barWidth to 50%", () => {
            const option = component.renderStackChart();
            const series = option.series as any[];

            series.forEach((s) => {
                expect(s.barWidth).toBe("50%");
            });
        });

        it("should use translated names for series", () => {
            spyOn(translateService, "instant").and.callFake((key: string) => {
                if (key === "error-graph.impact-calculated") return "Impact Calculated";
                if (key === "error-graph.unable-calculate") return "Unable to Calculate";
                return key;
            });

            const option = component.renderStackChart();
            const series = option.series as any[];

            expect(series[0].name).toBe("Impact Calculated");
            expect(series[1].name).toBe("Unable to Calculate");
        });

        it("should calculate data from statusCountMap for ok status", () => {
            const statusMap: StatusCountMap = {
                DS1: { status: { ok: 10, error: 2, total: 12 } },
                DS2: { status: { ok: 8, error: 4, total: 12 } },
            };
            componentRef.setInput("statusCountMap", statusMap);
            fixture.detectChanges();

            const option = component.renderStackChart();
            const series = option.series as any[];
            const okData = series[0].data;

            expect(okData[0]).toBe(10 / 12);
            expect(okData[1]).toBe(8 / 12);
        });

        it("should calculate data from statusCountMap for error status", () => {
            const statusMap: StatusCountMap = {
                DS1: { status: { ok: 10, error: 2, total: 12 } },
                DS2: { status: { ok: 8, error: 4, total: 12 } },
            };
            componentRef.setInput("statusCountMap", statusMap);
            fixture.detectChanges();

            const option = component.renderStackChart();
            const series = option.series as any[];
            const errorData = series[1].data;

            expect(errorData[0]).toBe(2 / 12);
            expect(errorData[1]).toBe(4 / 12);
        });

        it("should handle empty statusCountMap", () => {
            componentRef.setInput("statusCountMap", {});
            fixture.detectChanges();

            const option = component.renderStackChart();
            const series = option.series as any[];

            expect(series[0].data).toEqual([]);
            expect(series[1].data).toEqual([]);
        });

        it("should configure label formatter to show percentage", () => {
            const option = component.renderStackChart();
            const series = option.series as any[];

            series.forEach((s) => {
                expect(s.label.show).toBe(true);
                expect(s.label.formatter).toBeDefined();
            });
        });

        it("should format label as percentage with one decimal", () => {
            const option = component.renderStackChart();
            const series = option.series as any[];
            const formatter = series[0].label.formatter;

            expect(formatter({ value: 0.5 })).toBe("50%");
            expect(formatter({ value: 0.333 })).toBe("33.3%");
            expect(formatter({ value: 0.667 })).toBe("66.7%");
        });

        it("should round percentage correctly", () => {
            const option = component.renderStackChart();
            const series = option.series as any[];
            const formatter = series[0].label.formatter;

            expect(formatter({ value: 0.125 })).toBe("12.5%");
            expect(formatter({ value: 0.8333 })).toBe("83.3%");
        });
    });

    describe("optionStackBar computed signal", () => {
        it("should compute chart options", () => {
            expect(component.optionStackBar()).toBeDefined();
        });

        it("should update when xAxisInput changes", () => {
            const initialOption = component.optionStackBar();

            componentRef.setInput("xAxisInput", ["New1", "New2"]);
            fixture.detectChanges();

            const updatedOption = component.optionStackBar();
            expect((updatedOption.xAxis as any).data).toEqual(["New1", "New2"]);
            expect(updatedOption).not.toBe(initialOption);
        });

        it("should update when statusCountMap changes", () => {
            const statusMap1: StatusCountMap = {
                DS1: { status: { ok: 5, error: 5, total: 10 } },
            };
            componentRef.setInput("statusCountMap", statusMap1);
            fixture.detectChanges();

            const option1 = component.optionStackBar();

            const statusMap2: StatusCountMap = {
                DS1: { status: { ok: 8, error: 2, total: 10 } },
            };
            componentRef.setInput("statusCountMap", statusMap2);
            fixture.detectChanges();

            const option2 = component.optionStackBar();
            expect(option2).not.toBe(option1);
        });

        it("should update when cursor changes", () => {
            componentRef.setInput("cursor", "pointer");
            fixture.detectChanges();
            const option1 = component.optionStackBar();

            componentRef.setInput("cursor", "default");
            fixture.detectChanges();
            const option2 = component.optionStackBar();

            expect((option2 as any).cursor).toBe("default");
            expect((option1 as any).cursor).toBe("pointer");
        });
    });

    describe("Edge cases", () => {
        it("should handle single item in xAxis", () => {
            componentRef.setInput("xAxisInput", ["SingleItem"]);
            fixture.detectChanges();

            const option = component.renderStackChart();

            expect((option.xAxis as any).data).toEqual(["SingleItem"]);
            expect((option as any).dataZoom).toEqual([]);
        });

        it("should handle zero total in status count", () => {
            const statusMap: StatusCountMap = {
                DS1: { status: { ok: 0, error: 0, total: 0 } },
            };
            componentRef.setInput("statusCountMap", statusMap);
            fixture.detectChanges();

            const option = component.renderStackChart();
            const series = option.series as any[];

            expect(series[0].data[0]).toBeNaN();
        });

        it("should handle very large numbers in status count", () => {
            const statusMap: StatusCountMap = {
                DS1: { status: { ok: 1000000, error: 500000, total: 1500000 } },
            };
            componentRef.setInput("statusCountMap", statusMap);
            fixture.detectChanges();

            const option = component.renderStackChart();
            const series = option.series as any[];

            expect(series[0].data[0]).toBeCloseTo(1000000 / 1500000);
            expect(series[1].data[0]).toBeCloseTo(500000 / 1500000);
        });

        it("should handle all ok status (no errors)", () => {
            const statusMap: StatusCountMap = {
                DS1: { status: { ok: 10, error: 0, total: 10 } },
            };
            componentRef.setInput("statusCountMap", statusMap);
            fixture.detectChanges();

            const option = component.renderStackChart();
            const series = option.series as any[];

            expect(series[0].data[0]).toBe(1);
            expect(series[1].data[0]).toBe(0);
        });

        it("should handle all error status (no ok)", () => {
            const statusMap: StatusCountMap = {
                DS1: { status: { ok: 0, error: 10, total: 10 } },
            };
            componentRef.setInput("statusCountMap", statusMap);
            fixture.detectChanges();

            const option = component.renderStackChart();
            const series = option.series as any[];

            expect(series[0].data[0]).toBe(0);
            expect(series[1].data[0]).toBe(1);
        });

        it("should handle very long xAxis labels", () => {
            const longNames = ["Very_Long_Digital_Service_Name_That_Might_Overlap"];
            componentRef.setInput("xAxisInput", longNames);
            fixture.detectChanges();

            const option = component.renderStackChart();

            expect((option.xAxis as any).axisLabel.rotate).toBe(30);
        });

        it("should handle special characters in xAxis labels", () => {
            const specialNames = ["DS-1", "DS_2", "DS (3)", "DS.4"];
            componentRef.setInput("xAxisInput", specialNames);
            fixture.detectChanges();

            const option = component.renderStackChart();

            expect((option.xAxis as any).data).toEqual(specialNames);
        });
    });

    describe("Integration tests", () => {
        it("should create complete chart with all inputs", () => {
            const xAxisData = ["DS1", "DS2", "DS3"];
            const statusMap: StatusCountMap = {
                DS1: { status: { ok: 10, error: 2, total: 12 } },
                DS2: { status: { ok: 8, error: 4, total: 12 } },
                DS3: { status: { ok: 12, error: 0, total: 12 } },
            };

            componentRef.setInput("xAxisInput", xAxisData);
            componentRef.setInput("statusCountMap", statusMap);
            componentRef.setInput("cursor", "pointer");
            fixture.detectChanges();

            const option = component.optionStackBar();

            expect((option.xAxis as any).data).toEqual(xAxisData);
            expect((option as any).cursor).toBe("pointer");
            expect((option.series as any[]).length).toBe(2);
            expect((option.series as any[])[0].data.length).toBe(3);
        });

        it("should handle dynamic updates to all inputs", () => {
            componentRef.setInput("xAxisInput", ["Initial"]);
            componentRef.setInput("statusCountMap", {
                Initial: { status: { ok: 5, error: 5, total: 10 } },
            });
            fixture.detectChanges();

            const initialOption = component.optionStackBar();

            componentRef.setInput("xAxisInput", ["Updated1", "Updated2"]);
            componentRef.setInput("statusCountMap", {
                Updated1: { status: { ok: 7, error: 3, total: 10 } },
                Updated2: { status: { ok: 6, error: 4, total: 10 } },
            });
            componentRef.setInput("cursor", "crosshair");
            fixture.detectChanges();

            const updatedOption = component.optionStackBar();

            expect((updatedOption.xAxis as any).data).toEqual(["Updated1", "Updated2"]);
            expect((updatedOption as any).cursor).toBe("crosshair");
            expect(updatedOption).not.toBe(initialOption);
        });

        it("should emit event and render chart simultaneously", () => {
            const emitSpy = spyOn(component.selectedStackBarClick, "emit");
            componentRef.setInput("xAxisInput", ["DS1"]);
            componentRef.setInput("statusCountMap", {
                DS1: { status: { ok: 10, error: 2, total: 12 } },
            });
            fixture.detectChanges();

            const option = component.optionStackBar();
            component.onChartClick({ name: "DS1" });

            expect(option).toBeDefined();
            expect(emitSpy).toHaveBeenCalledWith("DS1");
        });
    });
});
