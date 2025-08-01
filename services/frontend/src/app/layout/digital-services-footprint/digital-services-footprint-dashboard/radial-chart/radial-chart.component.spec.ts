/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { NGX_ECHARTS_CONFIG, NgxEchartsModule } from "ngx-echarts";
import { ButtonModule } from "primeng/button";
import {
    getColorFormatter,
    getLabelFormatter,
} from "src/app/core/service/mapper/graphs-mapper";
import { SharedModule } from "src/app/core/shared/shared.module";
import { Constants } from "src/constants";
import { RadialChartComponent } from "./radial-chart.component";

declare var require: any;

describe("RadialChartComponent", () => {
    let component: RadialChartComponent;
    let fixture: ComponentFixture<RadialChartComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [RadialChartComponent],
            imports: [
                ButtonModule,
                SharedModule,
                NgxEchartsModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                {
                    provide: TranslateService,
                    useValue: {
                        currentLang: "en",
                        translations: {
                            en: {
                                criteria: {
                                    "criteria.climate-change.title": "Climate Change",
                                    "criteria.resource-use.title": "Resource Use",
                                    "criteria.ionising-radiation.title":
                                        "Ionising Radiation",
                                    "criteria.acidification.title": "Acidification",
                                    "criteria.particulate-matter.title":
                                        "Particulate Matter",
                                },
                            },
                        },
                        instant: (key: string) => key,
                    },
                },
                {
                    provide: NGX_ECHARTS_CONFIG,
                    useFactory: () => ({ echarts: () => import("echarts") }),
                },
            ],
        }).compileComponents();
    });

    beforeEach(async () => {
        fixture = TestBed.createComponent(RadialChartComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should return correct color/label when enableDataInconsistency is true", () => {
        component.enableDataInconsistency = true;
        const radialChartData = [
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
        const option = component.loadRadialChartOption(radialChartData as any);
        const criteriaMap = component.criteriaMap;
        let angleAxisData: any[] = [];
        if (option.angleAxis) {
            if (Array.isArray(option.angleAxis)) {
                angleAxisData = option.angleAxis.flatMap((axis: any) => axis?.data ?? []);
            } else if ("data" in option.angleAxis) {
                angleAxisData = (option.angleAxis as any).data ?? [];
            } else {
                angleAxisData = [];
            }
        }
        angleAxisData.forEach((dataObj: any) => {
            const value = dataObj.value;
            expect(dataObj.textStyle.color).toBe(
                getColorFormatter(!!criteriaMap[value]?.status?.error, true),
            );
        });
        let axisLabelFormatter: ((criteria: string) => string) | undefined;
        if (option.angleAxis) {
            if (Array.isArray(option.angleAxis)) {
                // If it's an array, take the first element with axisLabel.formatter
                const axis = option.angleAxis.find((a: any) => a?.axisLabel?.formatter);
                axisLabelFormatter = (
                    axis?.axisLabel as { formatter?: (criteria: string) => string }
                )?.formatter;
            } else if ((option.angleAxis as any).axisLabel?.formatter) {
                axisLabelFormatter = (option.angleAxis as any).axisLabel.formatter;
            }
        }
        expect(axisLabelFormatter).toBeDefined();
        Object.keys(criteriaMap).forEach((criteria) => {
            expect(axisLabelFormatter!(criteria)).toBe(
                getLabelFormatter(!!criteriaMap[criteria]?.status?.error, true, criteria),
            );
        });
    });
});
