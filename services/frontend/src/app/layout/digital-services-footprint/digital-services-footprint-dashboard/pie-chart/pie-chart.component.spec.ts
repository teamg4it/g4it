/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { NGX_ECHARTS_CONFIG, NgxEchartsModule } from "ngx-echarts";
import { ButtonModule } from "primeng/button";
import {
    getColorFormatter,
    getLabelFormatter,
} from "src/app/core/service/mapper/graphs-mapper";
import { SharedModule } from "src/app/core/shared/shared.module";
import { Constants } from "src/constants";
import { PieChartComponent } from "./pie-chart.component";
declare var require: any;

describe("PieChartComponent", () => {
    let component: PieChartComponent;
    let fixture: ComponentFixture<PieChartComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [PieChartComponent],
            imports: [
                ButtonModule,
                SharedModule,
                NgxEchartsModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                {
                    provide: NGX_ECHARTS_CONFIG,
                    useFactory: () => ({ echarts: () => import("echarts") }),
                },
            ],
        }).compileComponents();
    });

    beforeEach(async () => {
        fixture = TestBed.createComponent(PieChartComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should generate valid EChartsOption", () => {
        const chartData: any[] = require("test/data/digital-service-data/digital_service_indicators_footprint.json");
        component.selectedCriteria = "acidification";
        const echartsOption: EChartsOption = component.loadPieChartOption(chartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
    });

    it("should return correct label/color when enableDataInconsistency is true", () => {
        component.enableDataInconsistency = true;
        const chartData = [
            {
                tier: "Tier1",
                impacts: [
                    {
                        criteria: "acidification",
                        status: Constants.DATA_QUALITY_STATUS.ok,
                        countValue: 2,
                        sipValue: 1.5,
                        unitValue: 10,
                        unit: "u",
                    },
                ],
            },
        ];
        const option = component.loadPieChartOption(chartData as any);
        const dsTierOkmap = component.criteriaMap;
        const labelFormatter = (option["label"] as { formatter: (params: any) => string })
            .formatter;
        expect(labelFormatter({ name: "Tier1" })).toBe(
            getLabelFormatter(!!dsTierOkmap["Tier1"]?.status?.error, true, "Tier1"),
        );
        const series = option.series;
        expect(Array.isArray(series)).toBeTrue();
        const seriesData = Array.isArray(series) ? (series[0].data as any[]) : undefined;
        expect(seriesData?.[0]?.label?.color).toEqual(
            getColorFormatter(!!dsTierOkmap["Tier1"]?.status?.error, true),
        );
    });
});
