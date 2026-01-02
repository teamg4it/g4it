/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { NGX_ECHARTS_CONFIG, NgxEchartsModule } from "ngx-echarts";
import { ButtonModule } from "primeng/button";
import {
    DigitalServiceCloudImpact,
    DigitalServiceNetworksImpact,
    DigitalServiceServersImpact,
    DigitalServiceTerminalsImpact,
    TerminalsType,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import {
    transformOutPhysicalEquipmentsToTerminalData,
    transformOutVirtualEquipmentsToCloudData,
} from "src/app/core/service/mapper/digital-service";
import {
    getColorFormatter,
    getLabelFormatter,
} from "src/app/core/service/mapper/graphs-mapper";
import { SharedModule } from "src/app/core/shared/shared.module";
import { Constants } from "src/constants";
import { BarChartComponent } from "./bar-chart.component";
declare var require: any;

describe("BarChartComponent", () => {
    let component: BarChartComponent;
    let fixture: ComponentFixture<BarChartComponent>;
    let digitalServicesService: DigitalServiceBusinessService;
    let enableDataInconsistency: boolean;
    let networkMap: any;
    beforeEach(async () => {
        enableDataInconsistency = false;
        networkMap = {
            A: { status: { error: false } },
            B: { status: { error: true } },
        };
        await TestBed.configureTestingModule({
            declarations: [BarChartComponent],
            imports: [
                HttpClientTestingModule,
                ButtonModule,
                SharedModule,
                NgxEchartsModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                DigitalServiceBusinessService,
                {
                    provide: NGX_ECHARTS_CONFIG,
                    useFactory: () => ({ echarts: () => import("echarts") }),
                },
            ],
        }).compileComponents();
    });

    beforeEach(async () => {
        fixture = TestBed.createComponent(BarChartComponent);
        digitalServicesService = TestBed.inject(DigitalServiceBusinessService);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should generate valid EChartsOption for Servers", () => {
        const barChartData: DigitalServiceServersImpact[] = require("test/data/digital-service-data/digital_service_servers_footprint.json");
        component.selectedCriteria = "acidification";
        component.barChartChild = false;

        const echartsOption: EChartsOption =
            component.loadStackBarOptionServer(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
    });

    it("should generate valid EChartsOption for Servers Child (case lifecycle)", () => {
        const barChartData: DigitalServiceServersImpact[] = require("test/data/digital-service-data/digital_service_servers_footprint.json");
        component.selectedCriteria = "acidification";
        component.selectedDetailParam =
            "digital-services-servers.server-type.Shared-Compute";
        component.selectedDetailName = "Server B";
        component.serversRadioButtonSelected = "lifecycle";
        component.barChartChild = true;

        const echartsOption: EChartsOption =
            component.loadStackBarOptionServerChild(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
        expect(echartsOption.series).toEqual([
            {
                name: "servers",
                type: "bar",
                data: [
                    {
                        value: 0.37939472556712334,
                        rawValue: 3211.715221903284,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.0036639668990612407,
                        rawValue: 3211.715221903284,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.23510612851362,
                        rawValue: 3211.715221903284,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.002776134796054795,
                        rawValue: 3211.715221903284,
                        unit: "mol H+ eq",
                    },
                ],
            },
        ]);
    });

    it("should generate valid EChartsOption for Servers Child (case vm)", () => {
        const barChartData: DigitalServiceServersImpact[] = require("test/data/digital-service-data/digital_service_servers_footprint.json");
        component.selectedCriteria = "acidification";
        component.selectedDetailParam =
            "digital-services-servers.server-type.Shared-Compute";
        component.selectedDetailName = "Server B";
        component.serversRadioButtonSelected = "vm";
        component.barChartChild = true;

        const echartsOption: EChartsOption =
            component.loadStackBarOptionServerChild(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
        expect(echartsOption.series).toEqual([
            {
                name: "servers",
                type: "bar",
                data: [
                    {
                        value: 0.6209409557758594,
                        rawValue: 3211.715221903284,
                        unit: "mol H+ eq",
                        quantity: 4,
                    },
                ],
            },
        ]);
    });

    it("should generate valid EChartsOption for Terminals", () => {
        const physicalEquipments: any[] = [
            {
                name: "Terminal 2",
                criterion: "ACIDIFICATION",
                lifecycleStep: "TRANSPORTATION",
                statusIndicator: "OK",
                location: "Egypt",
                equipmentType: "Terminal",
                unit: "mol H+ eq",
                reference: "smartphone-2",
                countValue: 1,
                unitImpact: 0.000022824964931506846,
                peopleEqImpact: 1.8259971945205475e-7,
                electricityConsumption: 0,
                quantity: 0.001141552511415525,
                numberOfUsers: 2,
                lifespan: 0.00684931506849315,
                commonFilters: [""],
                filters: [""],
            },
        ];
        const deviceTypes: TerminalsType[] = [
            {
                code: "smartphone-2",
                value: "Mobile Phone",
                lifespan: 2.5,
            },
        ];
        const barChartData: DigitalServiceTerminalsImpact[] =
            transformOutPhysicalEquipmentsToTerminalData(physicalEquipments, deviceTypes);

        component.selectedCriteria = "acidification";
        component.terminalsRadioButtonSelected = "type";

        const echartsOption: EChartsOption =
            component.loadStackBarOptionTerminal(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
    });

    it("should handle empty data for Terminals", () => {
        const barChartData: any[] = [];
        component.selectedCriteria = "acidification";
        component.terminalsRadioButtonSelected = "type";

        const echartsOption: EChartsOption =
            component.loadStackBarOptionTerminal(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
    });

    it("should generate valid EChartsOption for Cloud Services", () => {
        const barChartData = transformOutVirtualEquipmentsToCloudData(
            [
                {
                    name: "CloudService A",
                    criterion: "ACIDIFICATION",
                    lifecycleStep: "USING",
                    physicalEquipmentName: "",
                    infrastructureType: "CLOUD_SERVICES",
                    instanceType: "a1.medium",
                    provider: "aws",
                    equipmentType: "",
                    location: "EEE",
                    statusIndicator: "ERROR",
                    countValue: 1,
                    quantity: 1,
                    unitImpact: 0,
                    peopleEqImpact: 0,
                    electricityConsumption: 0,
                    unit: "MJ",
                    usageDuration: 8760,
                    workload: 0.5,
                    commonFilters: [""],
                    filters: [""],
                    filtersPhysicalEquipment: [""],
                },
            ] as any[],
            { EEE: "Europe" },
        );
        component.selectedCriteria = "acidification";
        component.cloudRadioButtonSelected = "instance";

        const echartsOption: EChartsOption =
            component.loadStackBarOptionCloud(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
    });

    it("should handle empty data for Cloud Services", () => {
        const barChartData: DigitalServiceCloudImpact[] = [];
        component.selectedCriteria = "acidification";
        component.cloudRadioButtonSelected = "instance";

        const echartsOption: EChartsOption =
            component.loadStackBarOptionCloud(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
    });

    it("should format tooltip correctly for terminal data", () => {
        const isTerminal = true;
        const formatter = component["createTooltipFormatter"](isTerminal);

        const params = {
            value: 1,
            data: [50, 20, 10, 20, 30, "kg"],
            seriesName: "Terminals",
            color: "#0000FF",
        };

        const tooltip = formatter(params);

        expect(tooltip).toContain("30 kg");
        expect(tooltip).toContain("20");
    });

    it("should format tooltip correctly for non-terminal data", () => {
        const isTerminal = false;
        const formatter = component["createTooltipFormatter"](isTerminal);

        const params = {
            value: 200,
            data: [50, 20, 10, 20, 30, "kg"],
            seriesName: "Cloud Services",
            color: "#FF0000",
        };

        const tooltip = formatter(params);

        expect(tooltip).toContain("30 kg");
        expect(tooltip).toContain("20");
    });

    it("should generate valid EChartsOption for Network with data", () => {
        const networkData: DigitalServiceNetworksImpact[] = [
            {
                criteria: "climate-change",
                impacts: [
                    {
                        status: {
                            ok: 80,
                            error: 20,
                            total: 100,
                        },
                        networkType: "Fixed Network",
                        items: [
                            {
                                unit: "GB",
                                networkType: "Fixed Network",
                                sipValue: 50,
                                rawValue: 500,
                                status: "OK",
                                countValue: 10,
                            },
                            {
                                unit: "GB",
                                networkType: "Fixed Network",
                                sipValue: 30,
                                rawValue: 300,
                                status: "Error",
                                countValue: 5,
                            },
                        ],
                    },
                    {
                        status: {
                            ok: 60,
                            error: 40,
                            total: 100,
                        },
                        networkType: "Mobile Network",
                        items: [
                            {
                                name: "Mobile Network A",
                                unit: "GB",
                                networkType: "Mobile Network",
                                sipValue: 70,
                                rawValue: 700,
                                status: "OK",
                                countValue: 15,
                            },
                            {
                                name: "Mobile Network A",
                                unit: "GB",
                                networkType: "Mobile Network",
                                sipValue: 20,
                                rawValue: 200,
                                status: "Error",
                                countValue: 8,
                            },
                        ],
                    },
                ],
            },
        ];

        component.selectedCriteria = "climate-change";

        const echartsOption: EChartsOption =
            component.loadStackBarOptionNetwork(networkData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
    });

    it("should handle empty network data gracefully", () => {
        const networkData: DigitalServiceNetworksImpact[] = [];

        component.selectedCriteria = "acidification";

        const echartsOption: EChartsOption =
            component.loadStackBarOptionNetwork(networkData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
    });

    it("should call formatter and color functions for axisLabel", () => {
        // Mock networkMap and data
        const networkData = [
            {
                networkType: "A",
                status: { ok: 1, error: 0, total: 1 },
                items: [{ name: "item1", sipValue: 0.5, rawValue: 10, unit: "u" }],
            },
            {
                networkType: "B",
                status: { ok: 0, error: 1, total: 1 },
                items: [{ name: "item2", sipValue: 0.7, rawValue: 20, unit: "u" }],
            },
        ];

        // Call the chart option method
        const option = component.loadStackBarOptionNetwork(networkData as any);

        // Access axisLabel formatter and color
        expect(option.xAxis).toBeTruthy();
        const xAxisArray = Array.isArray(option.xAxis) ? option.xAxis : [option.xAxis];
        const axisLabel = xAxisArray[0]!.axisLabel!;
        expect(axisLabel).toBeTruthy();

        // Test for a value with no error
        // Type assertion to ensure axisLabel is of the expected type
        const axisLabelTyped = axisLabel as {
            color?: ((value?: string | number, index?: number) => string) | string;
            formatter?: (value?: string | number, index?: number) => string;
        };

        let colorA: string | undefined;
        if (typeof axisLabelTyped.color === "function") {
            colorA = axisLabelTyped.color("A");
        }
        let formatterA: string | undefined;
        if (typeof axisLabelTyped.formatter === "function") {
            formatterA = axisLabelTyped.formatter("A");
        }
        expect(colorA).toBe(getColorFormatter(false, component.enableDataInconsistency));
        expect(formatterA).toBe(
            getLabelFormatter(false, component.enableDataInconsistency, "A"),
        );

        // Test for a value with error
        let colorB: string | undefined;
        if (typeof axisLabelTyped.color === "function") {
            colorB = axisLabelTyped.color("B");
        }
        let formatterB: string | undefined;
        if (typeof axisLabelTyped.formatter === "function") {
            formatterB = axisLabelTyped.formatter("B");
        }
        expect(colorB).toBe(getColorFormatter(false, component.enableDataInconsistency));
        expect(formatterB).toBe(
            getLabelFormatter(false, component.enableDataInconsistency, "B"),
        );
    });

    it("should call color and formatter functions in axisLabel", () => {
        // Mock okMap and xAxis
        const okMap = {
            A: { status: { error: false } },
            B: { status: { error: true } },
        };
        const xAxis = ["A", "B"];
        const yAxis = [1];

        // Call createChartOption
        const option = (component as any).createChartOption(xAxis, yAxis, okMap, false);

        // Access axisLabel
        const axisLabel = option.xAxis[0].axisLabel;

        // Test color and formatter for 'A' (no error)
        expect(axisLabel.color("A")).toBe(
            getColorFormatter(false, component.enableDataInconsistency),
        );
        expect(axisLabel.formatter("A")).toBe(
            getLabelFormatter(false, component.enableDataInconsistency, "A"),
        );

        // Test color and formatter for 'B' (error)
        expect(axisLabel.color("B")).toBe(
            getColorFormatter(true, component.enableDataInconsistency),
        );
        expect(axisLabel.formatter("B")).toBe(
            getLabelFormatter(true, component.enableDataInconsistency, "B"),
        );
    });

    it("should return correct color/label when enableDataInconsistency is true", () => {
        component.enableDataInconsistency = true;
        const okMap = {
            C: { status: { error: false } },
            D: { status: { error: true } },
        };
        const xAxis = ["C", "D"];
        const yAxis = [1];

        const option = (component as any).createChartOption(xAxis, yAxis, okMap, false);
        const axisLabel = option.xAxis[0].axisLabel;

        expect(axisLabel.color("C")).toBe(getColorFormatter(false, true));
        expect(axisLabel.formatter("C")).toBe(getLabelFormatter(false, true, "C"));
        expect(axisLabel.color("D")).toBe(getColorFormatter(true, true));
        expect(axisLabel.formatter("D")).toBe(getLabelFormatter(true, true, "D"));
    });

    it("should call formatter and color functions in axisLabel", () => {
        // Mock barChartData for one server type with and without error

        const barChartData = [
            {
                criteria: "acidification",
                impactsServer: [
                    {
                        mutualizationType: "Dedicated",
                        serverType: "Compute",
                        servers: [
                            {
                                name: "Server1",
                                totalSipValue: 0.5,
                                impactStep: [
                                    {
                                        unit: "u",
                                    },
                                ],
                                impactVmDisk: [
                                    {
                                        status: Constants.DATA_QUALITY_STATUS.ok,
                                        countValue: 2,
                                        rawValue: 10,
                                        unit: "u",
                                        name: "VM1",
                                        quantity: 1,
                                    },
                                    {
                                        status: Constants.DATA_QUALITY_STATUS.error,
                                        countValue: 1,
                                        rawValue: 5,
                                        unit: "u",
                                        name: "VM2",
                                        quantity: 2,
                                    },
                                ],
                                hostingEfficiency: "high",
                            },
                        ],
                    },
                    {
                        mutualizationType: "Shared",
                        serverType: "Compute",
                        servers: [
                            {
                                name: "Server2",
                                totalSipValue: 2,
                                impactStep: [
                                    {
                                        unit: "u",
                                    },
                                ],
                                impactVmDisk: [
                                    {
                                        status: Constants.DATA_QUALITY_STATUS.error,
                                        countValue: 3,
                                        rawValue: 15,
                                        unit: "u",
                                        name: "VM3",
                                        quantity: 3,
                                    },
                                ],
                                hostingEfficiency: "low",
                            },
                        ],
                    },
                ],
            },
        ];

        // Call the method
        const option = component.loadStackBarOptionServer(barChartData);

        // Get server types as keys
        const serverTypes = [
            "digital-services-servers.server-type.Dedicated-Compute",
            "digital-services-servers.server-type.Shared-Compute",
        ];

        // Access axisLabel
        let axisLabel;
        if (option.xAxis) {
            if (Array.isArray(option.xAxis)) {
                axisLabel = option.xAxis[0]?.axisLabel;
            } else {
                axisLabel = (option.xAxis as any).axisLabel;
            }
        }

        // For first serverType (has ok and error)
        const hasErrorA = true; // because there is at least one error in impactVmDisk
        expect(axisLabel.color(serverTypes[0])).toBe(
            getColorFormatter(hasErrorA, component.enableDataInconsistency),
        );
        expect(axisLabel.formatter(serverTypes[0])).toBe(
            getLabelFormatter(
                hasErrorA,
                component.enableDataInconsistency,
                serverTypes[0],
            ),
        );

        // For second serverType (only error)
        const hasErrorB = true;
        expect(axisLabel.color(serverTypes[1])).toBe(
            getColorFormatter(hasErrorB, component.enableDataInconsistency),
        );
        expect(axisLabel.formatter(serverTypes[1])).toBe(
            getLabelFormatter(
                hasErrorB,
                component.enableDataInconsistency,
                serverTypes[1],
            ),
        );
    });

    it("should return correct color/label when enableDataInconsistency is true", () => {
        component.enableDataInconsistency = true;
        const barChartData: any[] = [
            {
                criteria: "acidification",
                impactsServer: [
                    {
                        mutualizationType: "Shared",
                        serverType: "Storage",
                        servers: [
                            {
                                name: "Server1",
                                totalSipValue: 0.5,
                                impactStep: [
                                    {
                                        unit: "u",
                                    },
                                ],
                                impactVmDisk: [
                                    {
                                        status: Constants.DATA_QUALITY_STATUS.ok,
                                        countValue: 2,
                                        rawValue: 10,
                                        unit: "u",
                                        name: "VM1",
                                        quantity: 1,
                                    },
                                ],
                                hostingEfficiency: "high",
                            },
                        ],
                    },
                ],
            },
        ];
        const option = component.loadStackBarOptionServer(barChartData);
        const serverType = "digital-services-servers.server-type.Shared-Storage";
        let axisLabel;
        if (option.xAxis) {
            if (Array.isArray(option.xAxis)) {
                axisLabel = option.xAxis[0]?.axisLabel;
            } else {
                axisLabel = (option.xAxis as any).axisLabel;
            }
        }

        const hasError = false;
        expect(axisLabel.color(serverType)).toBe(getColorFormatter(hasError, true));
        expect(axisLabel.formatter(serverType)).toBe(
            getLabelFormatter(hasError, true, serverType),
        );
    });
});
