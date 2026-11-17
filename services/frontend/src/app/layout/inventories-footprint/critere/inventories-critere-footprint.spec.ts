import { TestBed } from "@angular/core/testing";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { Constants } from "src/constants";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { InventoriesFootprintComponent } from "../inventories-footprint.component";
import { InventoriesCritereFootprintComponent } from "./inventories-critere-footprint.component";

describe("InventoriesCritereFootprintComponent", () => {
    let component: InventoriesCritereFootprintComponent;
    let footprintStore: any;
    let footprintService: any;
    let router: jasmine.SpyObj<Router>;
    let route: ActivatedRoute;
    let translateMock: any;

    beforeEach(async () => {
        footprintStore = {
            criteria: () => "Climate change",
            filters: () => ({}),
            dimension: () => "acvStep",
            unit: () => "kg",
            setUnit: jasmine.createSpy("setUnit"),
        };
        footprintService = {
            calculate: jasmine.createSpy("calculate").and.returnValue({
                footprintCalculated: [],
            }),
            valueImpact: jasmine
                .createSpy("valueImpact")
                .and.callFake((impact: any, field: string) => impact[field]),
        };
        router = jasmine.createSpyObj<Router>("Router", ["navigate"]);
        route = {} as ActivatedRoute;
        translateMock = {
            instant: jasmine
                .createSpy("instant")
                .and.callFake((key: string) =>
                    key === "acvStep"
                        ? { MANUFACTURING: "Manufacturing translated" }
                        : key,
                ),
            currentLang: "en",
        };

        await TestBed.configureTestingModule({
            imports: [InventoriesCritereFootprintComponent],
            providers: [
                DecimalsPipe,
                IntegerPipe,
                { provide: TranslateService, useValue: translateMock },
                { provide: GlobalStoreService, useValue: { criteriaList: () => ({}) } },
                { provide: FootprintStoreService, useValue: footprintStore },
                { provide: FootprintService, useValue: footprintService },
                {
                    provide: InventoriesFootprintComponent,
                    useValue: { allUnmodifiedFootprint: () => ({ A: {} }) },
                },
                { provide: Router, useValue: router },
                { provide: ActivatedRoute, useValue: route },
            ],
        }).compileComponents();

        component = TestBed.createComponent(
            InventoriesCritereFootprintComponent,
        ).componentInstance;
    });

    it("creates and derives calculated criteria totals", () => {
        footprintService.calculate.and.returnValue({
            footprintCalculated: [
                {
                    data: "",
                    impacts: [],
                    status: { ok: 0, error: 1, total: 1 },
                    total: { impact: 4, sip: 2 },
                },
                {
                    data: "",
                    impacts: [],
                    status: { ok: 1, error: 0, total: 1 },
                    total: { impact: 6, sip: 3 },
                },
            ],
        });

        expect(component).toBeTruthy();
        expect(component.criteriaCalculated()).toEqual({
            footprints: [
                {
                    data: "",
                    impacts: [],
                    status: { ok: 0, error: 1, total: 1 },
                    total: { impact: 4, sip: 2 },
                },
                {
                    data: "",
                    impacts: [],
                    status: { ok: 1, error: 0, total: 1 },
                    total: { impact: 6, sip: 3 },
                },
            ],
            hasError: true,
            total: { impact: 10, sip: 5 },
        });
        expect(component.allFootprintsCriteriaKeys()).toEqual(["A"]);
    });

    it("updates the selected unit and reports the appropriate total", () => {
        component.changeUnitInStore("Peopleeq");

        expect(footprintStore.setUnit).toHaveBeenCalledWith("Peopleeq");
        expect(component.unitOfCriteria()).toBe("criteria.Climate change.unite");
        expect(component.infocard()).toEqual({
            title: "criteria.Climate change.inventory-title",
            text: "criteria.Climate change.inventory-text",
        });
        const calculated = {
            footprints: [],
            hasError: false,
            total: { impact: 12, sip: 7 },
        };
        expect(component.getTotal(Constants.PEOPLEEQ, calculated)).toBe(7);
        expect(component.getTotal("kg", calculated)).toBe(12);
    });

    it("clears chart state when changes arrive and records data status", () => {
        component.showInconsitencyGraph = true;
        component.ngOnChanges({ footprint: {} as any });
        expect(component.showInconsitencyGraph).toBeFalse();

        component.checkDataStatus({
            name: "stage",
            status: { ok: 1, error: 2, total: 3 },
        } as any);
        component.checkDataStatus({ name: "without-status" } as any);
        expect(component.criteriaMap).toEqual({
            stage: { status: { ok: 1, error: 2, total: 3 } },
        });
    });

    it("returns no chart data when there are no calculated footprints", () => {
        component.xAxisInput = ["stale"];
        component.criteriaMap = { stale: { status: {} as any } };

        expect(
            component.renderChart(
                { footprints: [], hasError: false, total: { impact: 0, sip: 0 } },
                "acvStep",
                "kg",
                {} as any,
            ),
        ).toEqual({});
        expect(component.xAxisInput).toEqual([]);
        expect(component.criteriaMap).toEqual({});
    });

    it("groups equipment impacts into sunburst categories and sums their values", () => {
        footprintStore.filters = () => ({ country: [Constants.ALL] });
        component.filterFields = ["country"];
        component.criteriaFootprint = {
            impacts: [
                {
                    itemCategory: "DEVICES",
                    equipment: "Laptop",
                    itemType: "Laptop",
                    itemModel: "ThinkPad",
                    country: "France",
                    impact: 2,
                    sip: 3,
                },
                {
                    itemCategory: "EXTERNAL",
                    equipment: "Cloud",
                    sunburstServiceType: "cloud",
                    sunburstProvider: " AWS ",
                    country: "France",
                    impact: 4,
                    sip: 6,
                },
                {
                    itemCategory: "EXTERNAL",
                    equipment: "Cloud",
                    sunburstServiceType: "cloud",
                    sunburstProvider: "aws",
                    country: "France",
                    impact: 1,
                    sip: 2,
                },
            ],
        } as any;

        const chart: any = component.renderChart(
            { footprints: [{} as any], hasError: false, total: { impact: 7, sip: 11 } },
            "equipment",
            "kg",
            {} as any,
        );
        const series = chart.series[0];

        expect(series.type).toBe("sunburst");
        expect(series.data[0].name).toBe(
            "inventories-footprint.sunburst-category.devices",
        );
        expect(series.data[0].children).toEqual([
            jasmine.objectContaining({
                name: "Laptop",
                children: [jasmine.objectContaining({ name: "ThinkPad", value: 2 })],
            }),
        ]);
        expect(series.data[1].children[0].children).toEqual([
            jasmine.objectContaining({ name: "AWS", value: 5 }),
        ]);
    });

    it("combines sub-one-percent impacts and exposes inconsistent categories", () => {
        const status = (ok: number, error: number) => ({
            ok,
            error,
            total: ok + error,
        });
        const chart: any = component.renderChart(
            {
                footprints: [
                    {
                        data: "dominant",
                        total: { impact: 99, sip: 99 },
                        status: status(1, 0),
                    },
                    {
                        data: "small-a",
                        total: { impact: 0.5, sip: 0.5 },
                        status: status(0, 1),
                    },
                    {
                        data: "small-b",
                        total: { impact: 0.5, sip: 0.5 },
                        status: status(0, 2),
                    },
                ],
                hasError: true,
                total: { impact: 100, sip: 100 },
            } as any,
            "country",
            "kg",
            { enableDataInconsistency: true } as any,
        );
        const series: any = chart.series[0];
        const other = series.data.find((item: any) => item.name === "common.other");

        expect(other.value).toBe(1);
        expect(other.status).toEqual({ ok: 0, error: 3, total: 3 });
        expect(component.criteriaMap["common.other"].status.error).toBe(3);
        expect(component.xAxisInput).toEqual(["common.other"]);
        expect(component.allErrorData).toBeFalse();
        expect(
            series.label.formatter({
                name: "small-a",
                data: { status: status(0, 1) },
                percent: 0.5,
            }),
        ).toContain("{red|");
        expect(
            series.label.formatter({
                name: "dominant",
                data: { status: status(1, 0) },
                percent: 99,
            }),
        ).toContain("{grey|");
        expect(chart.tooltip.formatter({ dataIndex: 0, color: "red" })).toContain(
            "common.other:",
        );
        expect(chart.legend.formatter("dominant")).toBe("dominant");
    });

    it("filters equipment impacts and totals Peopleeq values for unknown categories", () => {
        footprintStore.filters = () => ({ country: ["France"] });
        component.filterFields = ["country"];
        component.criteriaFootprint = {
            impacts: [
                {
                    itemCategory: "CUSTOM_CATEGORY",
                    equipment: "Server",
                    country: "France",
                    impact: 20,
                    sip: 8,
                },
                {
                    itemCategory: "CUSTOM_CATEGORY",
                    equipment: "Server",
                    country: "France",
                    impact: 10,
                    sip: 4,
                },
                {
                    itemCategory: "DEVICES",
                    equipment: "Excluded",
                    country: "Canada",
                    impact: 100,
                    sip: 100,
                },
            ],
        } as any;

        const chart: any = component.renderChart(
            { footprints: [{} as any], hasError: false, total: { impact: 30, sip: 12 } },
            "equipment",
            Constants.PEOPLEEQ,
            {} as any,
        );

        expect(footprintService.valueImpact).toHaveBeenCalled();
        expect(chart.series[0].data).toEqual([
            jasmine.objectContaining({
                name: "CUSTOM_CATEGORY",
                children: [jasmine.objectContaining({ name: "Server", value: 12 })],
            }),
        ]);
    });

    it("sorts lifecycle pie data, translates error labels, and marks all-error data", () => {
        const chart: any = component.renderChart(
            {
                footprints: [
                    {
                        data: "FIN_DE_VIE",
                        total: { impact: 1, sip: 1 },
                        status: { ok: 0, error: 1, total: 1 },
                    },
                    {
                        data: "FABRICATION",
                        total: { impact: 1, sip: 1 },
                        status: { ok: 0, error: 2, total: 2 },
                    },
                    {
                        data: Constants.EMPTY,
                        total: { impact: 1, sip: 1 },
                        status: { ok: 0, error: 1, total: 1 },
                    },
                ],
                hasError: true,
                total: { impact: 3, sip: 3 },
            } as any,
            Constants.ACV_STEP,
            "kg",
            { enableDataInconsistency: false } as any,
        );

        expect(chart.series[0].data.map((item: any) => item.name)).toEqual([
            Constants.EMPTY,
            "MANUFACTURING",
            "END_OF_LIFE",
        ]);
        expect(component.xAxisInput).toEqual([
            "common.empty",
            "Manufacturing translated",
            "END_OF_LIFE",
        ]);
        expect(component.allErrorData).toBeTrue();
        expect(
            chart.series[0].label.formatter({
                name: "MANUFACTURING",
                data: { status: { error: 1 } },
                percent: 33.333,
            }),
        ).toContain("{grey|");
    });

    it("builds text and graph descriptions for lifecycle and top-three impacts", () => {
        component.criteriaFootprint = { unit: "kg" } as any;
        const calculated = {
            footprints: [
                {
                    data: "MANUFACTURING",
                    impacts: [],
                    total: { impact: 50, sip: 5 },
                    status: { ok: 1, error: 0, total: 1 },
                },
                {
                    data: Constants.EMPTY,
                    impacts: [],
                    total: { impact: 30, sip: 3 },
                    status: { ok: 1, error: 0, total: 1 },
                },
                {
                    data: "USING",
                    impacts: [],
                    total: { impact: 20, sip: 2 },
                    status: { ok: 1, error: 0, total: 1 },
                },
                {
                    data: "Ignored",
                    impacts: [],
                    total: { impact: 1, sip: 1 },
                    status: { ok: 1, error: 0, total: 1 },
                },
            ],
            hasError: false,
            total: { impact: 101, sip: 11 },
        };

        expect(
            component.getTextDescription("description.", calculated as any, "kg"),
        ).toBe("description.text-description");
        expect(component.textDescriptionImpacts).toHaveSize(3);
        expect(
            component.textDescriptionImpacts.map((impact) => impact.impactNameVisible),
        ).toEqual(["acvStep.MANUFACTURING", "Empty", "acvStep.USING"]);
        expect(
            component.getTextDescription(
                "description.",
                {
                    ...calculated,
                    footprints: [],
                } as any,
                Constants.PEOPLEEQ,
            ),
        ).toBe("");
        expect(component.textDescriptionImpacts).toEqual([]);

        const content = component.getContentText();
        expect(content.scale).toBe("criteria.climate-change.scale");
        expect(content.analysis).toBe("ds-graph-description.global-vision.analysis");
    });

    it("creates tooltip markup and navigates to multi-criteria", () => {
        expect(component.getCircleColorHtml()).toBe("");
        expect(component.getCircleColorHtml("#123456")).toContain("#123456");
        expect(component.getTooltipItemHtml("Stage", "2 kg")).toContain("Stage:");
        expect(
            component.getToolTipHtml(
                { name: "Stage", value: 2 },
                "kg",
                "#123456",
                "country",
            ),
        ).toContain("2 kg");
        expect(
            component.computeToolTipHtml(
                [{ name: "Stage", value: 2 } as any],
                { dataIndex: 0, color: "#123456" },
                Constants.PEOPLEEQ,
                "country",
            ),
        ).toContain("2");
        expect(
            component.computeToolTipHtml(
                [{ name: "Stage", value: 2 } as any],
                { dataIndex: 0, color: "#123456" },
                "kg",
                "country",
            ),
        ).toContain("2 criteria.Climate change.unite");
        const otherTooltip = component.getToolTipHtml(
            {
                name: "other",
                otherData: [
                    { name: "Small A", value: 1, percent: 0.5 },
                    { name: "Small B", value: 2, percent: 0.7 },
                ],
            },
            Constants.PEOPLEEQ,
            "#123456",
            "country",
        );
        expect(otherTooltip).toContain("common.otherLegend");
        expect(otherTooltip).toContain("Small A:");

        component.criteriaFootprint = { unit: "kg" } as any;
        const description = component.getTextDescription(
            "description.",
            {
                footprints: [
                    {
                        data: "Custom stage",
                        impacts: [],
                        total: { impact: 4, sip: 2 },
                        status: { ok: 1, error: 0, total: 1 },
                    },
                ],
                hasError: false,
                total: { impact: 4, sip: 2 },
            },
            "kg",
        );
        expect(description).toBe("description.text-description");
        expect(component.textDescriptionImpacts).toHaveSize(1);
        expect(component.textDescriptionImpacts[0].impactNameVisible).toBe(
            "Custom stage",
        );

        component.moveToMultiCriteria();
        expect(router.navigate).toHaveBeenCalledWith(["../", "multi-criteria"], {
            relativeTo: route,
        });
    });
});
