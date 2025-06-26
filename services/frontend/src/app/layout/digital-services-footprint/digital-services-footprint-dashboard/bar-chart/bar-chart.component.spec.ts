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
import { SharedModule } from "src/app/core/shared/shared.module";
import { BarChartComponent } from "./bar-chart.component";
declare var require: any;

describe("BarChartComponent", () => {
    let component: BarChartComponent;
    let fixture: ComponentFixture<BarChartComponent>;
    let digitalServicesService: DigitalServiceBusinessService;

    beforeEach(async () => {
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

    it("should generate valid EChartsOption for Networks", () => {
        const barChartData: DigitalServiceNetworksImpact[] = require("test/data/digital-service-data/digital_service_networks_footprint.json");
        component.selectedCriteria = "acidification";
        const echartsOption: EChartsOption =
            component.loadStackBarOptionNetwork(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
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
});
