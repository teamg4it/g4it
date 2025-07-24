/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    Component,
    EventEmitter,
    input,
    Input,
    Output,
    SimpleChanges,
} from "@angular/core";
import { EChartsOption } from "echarts";
import {
    DigitalServiceCloudImpact,
    DigitalServiceNetworksImpact,
    DigitalServiceServersImpact,
    DigitalServiceTerminalsImpact,
    ImpactACVStep,
    ImpactNetworkNames,
    ImpactSipValue,
    ServerImpact,
    ServersType,
    StatusCountMap,
} from "src/app/core/interfaces/digital-service.interfaces";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import { AbstractDashboard } from "src/app/layout/inventories-footprint/abstract-dashboard";
import { Constants } from "src/constants";

@Component({
    selector: "app-bar-chart",
    templateUrl: "./bar-chart.component.html",
})
export class BarChartComponent extends AbstractDashboard {
    @Input() barChartChild: boolean = false;
    @Input() selectedParam: string = "";
    @Input() selectedDetailParam: string = "";
    @Input() selectedDetailName: string = "";
    @Input() selectedCriteria: string = "acidification";
    @Input() showDataButton: boolean = false;
    @Input() networkData: DigitalServiceNetworksImpact[] = [];
    @Input() serverData: DigitalServiceServersImpact[] = [];
    @Input() terminalData: DigitalServiceTerminalsImpact[] = [];
    @Input() cloudData: DigitalServiceCloudImpact[] = [];

    @Output() barChartChildChange: EventEmitter<any> = new EventEmitter();
    @Output() selectedDetailParamChange: EventEmitter<any> = new EventEmitter();
    @Output() selectedDetailNameChange: EventEmitter<any> = new EventEmitter();
    showInconsitency = input<boolean>();
    options: EChartsOption = {};

    criteriaMap: StatusCountMap = {};
    xAxisInput: string[] = [];

    terminalsOptions = [
        {
            key: "type",
            value: "type",
            name: this.translate.instant("digital-services.type"),
        },
        {
            key: "country",
            value: "country",
            name: this.translate.instant("digital-services.country"),
        },
    ];
    cloudOptions = [
        {
            key: "instance",
            value: "instance",
            name: this.translate.instant("digital-services-cloud-services.instance"),
        },
        {
            key: "location",
            value: "location",
            name: this.translate.instant("digital-services-cloud-services.location"),
        },
    ];
    terminalsRadioButtonSelected: string = "type";
    cloudRadioButtonSelected: string = "instance";
    serversOptions = [
        {
            key: "lifecycle",
            value: "lifecycle",
            name: this.translate.instant("digital-services.lifecycle"),
        },
        {
            key: "vm",
            value: "vm",
            name: this.translate.instant("digital-services.vm"),
        },
    ];
    serversRadioButtonSelected: string = "lifecycle";

    ngOnChanges(changes: SimpleChanges): void {
        if (changes) {
            if (this.selectedParam === "Network") {
                this.options = this.loadStackBarOptionNetwork(this.networkData);
            } else if (this.selectedParam === "Terminal") {
                this.options = this.loadStackBarOptionTerminal(this.terminalData);
            } else if (this.selectedParam === Constants.CLOUD_SERVICE) {
                this.options = this.loadStackBarOptionCloud(this.cloudData);
            } else if (this.selectedParam === "Server" && !this.barChartChild) {
                this.options = this.loadStackBarOptionServer(this.serverData);
            } else if (this.selectedParam === "Server" && this.barChartChild) {
                this.options = this.loadStackBarOptionServerChild(this.serverData);
            }
        }
    }

    onChartClick(params: any) {
        if (
            (this.selectedParam == "Terminal" ||
                this.selectedParam == "Server" ||
                this.selectedParam === Constants.CLOUD_SERVICE) &&
            !this.barChartChild
        ) {
            this.barChartChildChange.emit(true);
            this.selectedDetailNameChange.emit(params.seriesName);
            this.selectedDetailParamChange.emit(params.name);
        }
    }

    loadStackBarOptionNetwork(
        barChartData: DigitalServiceNetworksImpact[],
    ): EChartsOption {
        const networkMap: { [key: string]: any } = {};
        const seriesData = this.getSelectedCriteriaData(
            barChartData,
            "impacts",
            this.selectedCriteria,
        );
        let xAxis: any[] = [];
        const yAxis: any[] = [];
        seriesData.forEach((impact: ImpactNetworkNames) => {
            networkMap[impact.networkType] = {
                ...impact,
                status: {
                    ok: impact.status.ok,
                    error: impact.status.error,
                    total: impact.status.total,
                },
            };
            xAxis.push(impact.networkType);
            impact.items.forEach((item: any, index: number) => {
                const data = [
                    impact.networkType,
                    item.sipValue < 1 ? item.sipValue : item.sipValue.toFixed(0),
                    item.rawValue,
                    item.unit,
                ];

                yAxis.push({
                    name: item.name,
                    data: [data],
                    type: "bar",
                    stack: "Ad",
                    emphasis: {
                        focus: "series",
                    },
                    itemStyle: {
                        color: this.createStackBarGradientColor(
                            index,
                            impact.items.length,
                        ),
                    },
                });
            });
        });
        this.xAxisInput = xAxis;
        this.criteriaMap = networkMap;
        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    return `
                    <div style="display: flex; align-items: center; height: 30px;">
                            <span style="display: inline-block; width: 10px; height: 10px; background-color: ${params.color}; border-radius: 50%; margin-right: 5px;"></span>
                            <span style="font-weight: bold; margin-right: 15px;">${params.seriesName}</span>
                        </div>
                        <div>
                            ${this.integerPipe.transform(params.data[1])}
                            ${this.translate.instant("common.peopleeq-min")}<br>
                            ${this.decimalsPipe.transform(params.data[2])} ${params.data[3]}
                        </div>
                    `;
                },
            },
            grid: {
                left: "3%",
                right: "4%",
                bottom: "3%",
                containLabel: true,
            },
            xAxis: [
                {
                    type: "category",
                    data: xAxis,
                    axisLabel: {
                        rotate: 30, // Rotate labels if they overlap
                        color: (value: any) => {
                            return !networkMap[value].status.error || !this.showDataButton
                                ? Constants.GRAPH_GREY
                                : Constants.GRAPH_RED;
                        },
                        formatter: function (value: any) {
                            return !networkMap[value].status.error
                                ? `{grey| ${value}}`
                                : `{redBold| \u24d8} {red|${value}}`;
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
            series: yAxis,
            color: Constants.BLUE_COLOR,
        };
    }

    private loadStackBarOption(
        barChartData: any[],
        radioButtonSelected: string,
        impactType: string,
        impactLocation: string,
        type: string,
    ): EChartsOption {
        const isTerminal = type === Constants.TERMINAL;
        const xAxis: any[] = [];
        const yAxis: any[] = [];
        const seriesData = this.extractSelectedData(
            barChartData,
            radioButtonSelected,
            impactType,
            impactLocation,
            isTerminal,
        );
        let okMap = this.processParentOrChildData(seriesData, xAxis, yAxis, type);

        this.xAxisInput = xAxis;
        this.criteriaMap = okMap;

        return this.createChartOption(xAxis, yAxis, okMap, isTerminal);
    }

    private createChartOption(
        xAxis: any[],
        yAxis: any[],
        okMap: StatusCountMap,
        isTerminal: boolean,
    ): EChartsOption {
        return {
            tooltip: {
                show: true,
                formatter: this.createTooltipFormatter(isTerminal),
            },
            grid: {
                left: "3%",
                right: "4%",
                bottom: "3%",
                containLabel: true,
            },
            xAxis: [
                {
                    type: "category",
                    data: xAxis,
                    axisLabel: {
                        rotate: 30, // Rotate labels if they overlap
                        color: (value: any) => {
                            return !okMap[value].status.error || !this.showDataButton
                                ? Constants.GRAPH_GREY
                                : Constants.GRAPH_RED;
                        },
                        formatter: (value: any) => {
                            return !okMap[value].status.error || !this.showDataButton
                                ? `{grey| ${value}}`
                                : `{redBold| \u24d8} {red|${value}}`;
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
            series: this.barChartChild
                ? [
                      {
                          name: isTerminal ? "terminals" : "Cloud Services",
                          type: "bar",
                          data: yAxis,
                      },
                  ]
                : yAxis,
            color: Constants.BLUE_COLOR,
        } as any;
    }

    private createTooltipFormatter(isTerminal: boolean): (params: any) => string {
        return (params: any) => {
            if (
                this.barChartChild
                    ? params.value === undefined
                    : params.data[1] === undefined
            ) {
                return "";
            }
            const seriesName = !this.barChartChild
                ? `<div style="display: flex; align-items: center; height: 30px;">
                            <span style="display: inline-block; width: 10px; height: 10px; background-color: ${params.color}; border-radius: 50%; margin-right: 5px;"></span>
                            <span style="font-weight: bold; margin-right: 15px;">${params.seriesName}</span>
                        </div>`
                : "";
            const showedHtml = `
                <div>
                    ${this.integerPipe.transform(this.barChartChild ? params.value : params.data[1])}
                    ${this.translate.instant("common.peopleeq-min")} <br>
                    ${this.decimalsPipe.transform(params.data.rawValue ?? params.data[4])} ${params.data.unit ?? params.data[5]}
                </div>
            `;

            let otherHtml = "";

            if (!this.barChartChild) {
                otherHtml = `
                    <div>
                        ${this.translate.instant(
                            isTerminal
                                ? "digital-services-terminals.nb-user"
                                : "digital-services-cloud-services.tooltip_average_workload",
                        )}: ${this.decimalsPipe.transform(params.data[2])}${isTerminal ? "" : "%"}
                    </div>
                    <div>
                        ${this.translate.instant(
                            isTerminal
                                ? "digital-services-terminals.yearly-usage"
                                : "digital-services-cloud-services.tooltip_annual_usage",
                        )}: ${this.decimalsPipe.transform(params.data[3])}
                        ${this.translate.instant("digital-services-terminals.hours")}
                    </div>
                `;
            }

            return seriesName + showedHtml + otherHtml;
        };
    }

    private extractSelectedData(
        barChartData: any[],
        radioButtonSelected: string,
        impactType: string,
        impactLocation: string,
        isTerminal: boolean,
    ): any[] {
        if (
            radioButtonSelected ===
            (isTerminal ? this.terminalsOptions[0].value : this.cloudOptions[0].value)
        ) {
            return this.getSelectedCriteriaData(
                barChartData,
                impactType,
                this.selectedCriteria,
            );
        } else if (
            radioButtonSelected ===
            (isTerminal ? this.terminalsOptions[1].value : this.cloudOptions[1].value)
        ) {
            return this.getSelectedCriteriaData(
                barChartData,
                impactLocation,
                this.selectedCriteria,
            );
        }
        return [];
    }

    loadStackBarOptionTerminal(barChartData: any): EChartsOption {
        return this.loadStackBarOption(
            barChartData,
            this.terminalsRadioButtonSelected,
            "impactType",
            "impactCountry",
            Constants.TERMINAL,
        );
    }

    loadStackBarOptionCloud(barChartData: DigitalServiceCloudImpact[]): EChartsOption {
        return this.loadStackBarOption(
            barChartData,
            this.cloudRadioButtonSelected,
            "impactInstance",
            "impactLocation",
            Constants.CLOUD_SERVICE,
        );
    }

    processParentOrChildData(
        seriesData: any[],
        xAxis: string[],
        yAxis: any[],
        type: string,
    ): StatusCountMap {
        let okMap = {};
        const isTerminals = type === Constants.TERMINAL;

        if (!this.barChartChild) {
            return this.processParentData(seriesData, xAxis, yAxis, okMap, isTerminals);
        } else {
            return this.processChildData(seriesData, xAxis, yAxis, okMap, isTerminals);
        }
    }

    private processParentData(
        seriesData: any[],
        xAxis: string[],
        yAxis: any[],
        okMap: StatusCountMap,
        isTerminals: boolean,
    ): StatusCountMap {
        seriesData.forEach((impact: any) => {
            okMap[impact.name] = {
                status: {
                    ok: impact.status.ok ?? 0,
                    error: impact.status.error ?? 0,
                    total: impact.status.total ?? 0,
                },
            };
            xAxis.push(impact.name);

            const items = isTerminals ? impact.terminals : impact.clouds;
            items.forEach((item: any, index: number) => {
                const data = [
                    impact.name,
                    item.totalSipValue < 1
                        ? item.totalSipValue
                        : item.totalSipValue.toFixed(0),
                    ...(isTerminals
                        ? [item.totalNbUsers, item.avgUsageTime]
                        : [item.totalAvgWorkLoad, item.totalAvgUsage]),
                    item.rawValue,
                    item.unit,
                ];

                yAxis.push({
                    name: item.name,
                    data: [data],
                    type: "bar",
                    stack: "Ad",
                    emphasis: {
                        focus: "series",
                    },
                    itemStyle: {
                        color: this.createStackBarGradientColor(index, items.length),
                    },
                });
            });
        });
        return okMap;
    }

    private processChildData(
        seriesData: any[],
        xAxis: string[],
        yAxis: any[],
        okMap: StatusCountMap,
        isTerminals: boolean,
    ): StatusCountMap {
        let childData;
        if (!isTerminals) {
            childData = seriesData
                .find((item: any) => item.name === this.selectedDetailParam)
                .clouds.find((c: any) => c.name === this.selectedDetailName);
        } else {
            childData = seriesData
                .find((item: any) => item.name === this.selectedDetailParam)
                .terminals.find((term: any) => term.name === this.selectedDetailName);
        }

        const stepKey = "acvStep";

        if (childData) {
            const order = LifeCycleUtils.getLifeCycleList();

            childData.impact.forEach((impact: any) => {
                impact[stepKey] =
                    LifeCycleUtils.getLifeCycleMap().get(impact[stepKey]) ||
                    impact[stepKey];
            });

            childData.impact.sort((a: any, b: any) => {
                return order.indexOf(a[stepKey]) - order.indexOf(b[stepKey]);
            });

            childData.impact.forEach((impact: any) => {
                okMap[this.existingTranslation(impact[stepKey], "acvStep")] = {
                    status: {
                        ok: impact.statusCount?.ok ?? 0,
                        error: impact.statusCount?.error ?? 0,
                        total: impact.statusCount?.total ?? 0,
                    },
                };
                xAxis.push(this.existingTranslation(impact[stepKey], "acvStep"));
                yAxis.push({
                    value: impact.sipValue,
                    name: impact[stepKey],
                    ...(isTerminals
                        ? {
                              nbUsers: impact.totalNbUsers,
                              usageTime: impact.avgUsageTime,
                          }
                        : {
                              averageUsage: impact.totalAvgUsage,
                              averageWorkLoad: impact.totalAvgWorkLoad,
                          }),
                    rawValue: impact.rawValue,
                    unit: impact.unit,
                    statusCount: impact.statusCount,
                });
            });
        }
        return okMap;
    }

    loadStackBarOptionServer(barChartData: any): EChartsOption {
        const xAxisData: string[] = [];
        const seriesData: any[] = [];
        let detailServers: any[] = [];

        const data4Criteria = this.getSelectedCriteriaData(
            barChartData,
            "impactsServer",
            this.selectedCriteria,
        );
        let serverOkmap: StatusCountMap = {};
        data4Criteria.forEach((impact: ServersType) => {
            let serverType = this.translate.instant(
                `digital-services-servers.server-type.${impact.mutualizationType}-${impact.serverType}`,
            );
            xAxisData.push(serverType);
            const impactVmDisk = impact.servers.flatMap((server) => server.impactVmDisk);
            serverOkmap[serverType] = {
                status: {
                    ok: impactVmDisk.reduce(
                        (acc, i) =>
                            acc +
                            (i.status === Constants.DATA_QUALITY_STATUS.ok
                                ? i.countValue
                                : 0),
                        0,
                    ),
                    error: impactVmDisk.reduce(
                        (acc, i) =>
                            acc +
                            (i.status !== Constants.DATA_QUALITY_STATUS.ok
                                ? i.countValue
                                : 0),
                        0,
                    ),
                    total: impactVmDisk.reduce((acc, i) => acc + i.countValue, 0),
                },
            };
            impact.servers.forEach((server: ServerImpact, index: any) => {
                detailServers.push({
                    name: server.name,
                    vmList: server.impactVmDisk,
                    hostingEfficiency: server.hostingEfficiency,
                });
                seriesData.push({
                    name: server.name,
                    data: [
                        [
                            serverType,
                            server.totalSipValue < 1
                                ? server.totalSipValue
                                : server.totalSipValue.toFixed(0),
                            server.impactVmDisk.find(
                                (impact) =>
                                    impact.status === Constants.DATA_QUALITY_STATUS.ok,
                            )?.rawValue ?? 0,
                            server.impactVmDisk[0].unit,
                        ],
                    ],
                    type: "bar",
                    stack: "Ad",
                    emphasis: {
                        focus: "series",
                    },
                    itemStyle: {
                        color: this.createStackBarGradientColor(
                            index,
                            impact.servers.length,
                        ),
                    },
                });
            });
        });

        this.xAxisInput = xAxisData;
        this.criteriaMap = serverOkmap;
        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    let hostingEfficiency =
                        detailServers.find(
                            (server: any) => server.name === params.seriesName,
                        ).hostingEfficiency || "N/A";

                    const vmNameNbList = detailServers
                        .find((server: any) => server.name === params.seriesName)
                        .vmList.map((vm: any, index: any) => {
                            const str = `${vm.name} (${vm.quantity}) ; `;
                            return (index + 1) % 3 === 0 ? str + "<br/>" : str;
                        })
                        .join("");
                    const impactVm = detailServers
                        .find((server: any) => server.name === params.seriesName)
                        .vmList.reduce((acc: number, i: any) => acc + i.rawValue, 0);
                    return `
                        <div style="display: flex; align-items: center; height: 30px;">
                            <span style="display: inline-block; width: 10px; height: 10px; background-color: ${params.color}; border-radius: 50%; margin-right: 5px;"></span>
                            <span style="font-weight: bold; margin-right: 15px;">${params.seriesName}</span>
                        </div>
                        <div>
                            ${vmNameNbList}
                        </div>
                        <div>Impact: ${this.integerPipe.transform(params.data[1])}
                            ${this.translate.instant("common.peopleeq-min")} <br>
                            ${this.decimalsPipe.transform(impactVm)} ${params.data[3]}
                        </div>
                        <div>
                            ${this.translate.instant(
                                "digital-services-servers.hosting-efficiency",
                            )}: ${this.translate.instant(
                                "digital-services-servers." + hostingEfficiency,
                            )}
                        </div>`;
                },
            },
            grid: {
                left: "3%",
                right: "4%",
                bottom: "3%",
                containLabel: true,
            },
            xAxis: {
                type: "category",
                data: xAxisData,
                axisLabel: {
                    rotate: 30, // Rotate labels if they overlap
                    formatter: (value) =>
                        !serverOkmap[value].status.error
                            ? `{grey| ${this.translate.instant(value) || value}}`
                            : `{redBold| \u24d8} {red| ${this.translate.instant(value) || value}}`,
                    interval: 0, // Display all labels
                    color: (value: any) => {
                        return !serverOkmap[value].status.error || !this.showDataButton
                            ? Constants.GRAPH_GREY
                            : Constants.GRAPH_RED;
                    },
                    rich: Constants.CHART_RICH as any,
                },
            },
            yAxis: {
                type: "value",
            },
            series: seriesData,
        };
    }

    loadStackBarOptionServerChild(barChartData: any): EChartsOption {
        const xAxis: any[] = [];
        const seriesData: any[] = [];
        let serverChildOkmap: StatusCountMap = {};
        const selectedServer: ServerImpact =
            this.getSelectedCriteriaData(
                barChartData,
                "impactsServer",
                this.selectedCriteria,
            )
                .find(
                    (data: ServersType) =>
                        this.translate.instant(
                            `digital-services-servers.server-type.${data.mutualizationType}-${data.serverType}`,
                        ) === this.selectedDetailParam,
                )
                .servers.find(
                    (data: ServerImpact) => data.name === this.selectedDetailName,
                ) || null;

        if (this.serversRadioButtonSelected === "lifecycle") {
            const order = LifeCycleUtils.getLifeCycleList();
            const lifecycleMap = LifeCycleUtils.getLifeCycleMap();
            selectedServer.impactStep.forEach(
                (impact) =>
                    (impact.acvStep = lifecycleMap.get(impact.acvStep) || impact.acvStep),
            );

            selectedServer.impactStep.sort((a, b) => {
                return order.indexOf(a.acvStep) - order.indexOf(b.acvStep);
            });
            serverChildOkmap = {};

            selectedServer.impactStep.forEach((lifecycle: ImpactACVStep) => {
                const acvStep = this.existingTranslation(lifecycle.acvStep, "acvStep");
                xAxis.push(acvStep);
                serverChildOkmap[acvStep] = {
                    status: {
                        ok: selectedServer.impactStep.filter(
                            (i) =>
                                i.acvStep === lifecycle.acvStep &&
                                i.status === Constants.DATA_QUALITY_STATUS.ok,
                        ).length,
                        error: selectedServer.impactStep.filter(
                            (i) =>
                                i.acvStep === lifecycle.acvStep &&
                                i.status !== Constants.DATA_QUALITY_STATUS.ok,
                        ).length,
                        total: selectedServer.impactStep.filter(
                            (i) => i.acvStep === lifecycle.acvStep,
                        ).length,
                    },
                };

                seriesData.push({
                    value: lifecycle.sipValue,
                    rawValue: lifecycle.rawValue,
                    unit: lifecycle.unit,
                });
            });
        } else if (this.serversRadioButtonSelected === "vm") {
            selectedServer.impactVmDisk.sort((a, b) => a.name.localeCompare(b.name));
            serverChildOkmap = {};
            selectedServer.impactVmDisk.forEach((vm: ImpactSipValue) => {
                xAxis.push(vm.name);
                serverChildOkmap[vm.name] = {
                    status: {
                        ok: selectedServer.impactVmDisk.filter(
                            (i) => i.status === Constants.DATA_QUALITY_STATUS.ok,
                        ).length,
                        error: selectedServer.impactVmDisk.filter(
                            (i) => i.status !== Constants.DATA_QUALITY_STATUS.ok,
                        ).length,
                        total: selectedServer.impactVmDisk.length,
                    },
                };

                selectedServer.impactVmDisk.every(
                    (impact: any) => impact.status === Constants.DATA_QUALITY_STATUS.ok,
                );

                seriesData.push({
                    value: vm.sipValue < 1 ? vm.sipValue : vm.sipValue.toFixed(0),
                    rawValue: vm.rawValue,
                    unit: vm.unit,
                    quantity: vm.quantity,
                });
            });
        }
        this.xAxisInput = xAxis;
        this.criteriaMap = serverChildOkmap;
        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    const showedHtml = `
                        <div>
                            ${this.integerPipe.transform(params.value)}
                            ${this.translate.instant("common.peopleeq-min")}<br>
                            ${this.decimalsPipe.transform(params.data.rawValue)} ${params.data.unit}
                        </div>
                    `;

                    let showedVmHtml = "";

                    if (this.serversRadioButtonSelected === "vm") {
                        showedVmHtml = `
                            <div>
                                ${this.translate.instant(
                                    "digital-services-servers.quantity",
                                )}: ${params.data.quantity} ${this.translate.instant(
                                    "digital-services-servers.vms",
                                )}
                            </div>
                        `;
                    }

                    return showedVmHtml + showedHtml;
                },
            },
            grid: {
                left: "3%",
                right: "4%",
                bottom: "3%",
                containLabel: true,
            },
            xAxis: [
                {
                    type: "category",
                    data: xAxis,
                    axisLabel: {
                        rotate: 30, // Rotate labels if they overlap
                        formatter: (value) =>
                            !serverChildOkmap[value].status.error
                                ? this.noError(value)
                                : this.errorDetected(value),
                        interval: 0, // Display all labels
                        color: function (value: any) {
                            return !serverChildOkmap[value].status.error
                                ? Constants.GRAPH_GREY
                                : Constants.GRAPH_RED;
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
            series: [
                {
                    name: "servers",
                    type: "bar",
                    data: seriesData,
                },
            ],
            color: Constants.BLUE_COLOR,
        };
    }

    noError(value: string) {
        return `{grey| ${this.translate.instant(value) || value}}`;
    }

    errorDetected(value: string) {
        return `{redBold| \u24d8} {red| ${this.translate.instant(value) || value}}`;
    }

    changeTerminalsRadioButtonSelected() {
        this.options = this.loadStackBarOptionTerminal(this.terminalData);
    }

    changeCloudsRadioButtonSelected() {
        this.options = this.loadStackBarOptionCloud(this.cloudData);
    }

    changeServersRadioButtonSelected() {
        this.options = this.loadStackBarOptionServerChild(this.serverData);
    }

    createStackBarGradientColor(index: number, totalCount: number): any {
        if (totalCount == 1) {
            return Constants.BLUE_COLOR;
        }
        const startColor = Constants.BLUE_COLOR;
        const endColor = Constants.YELLOW_COLOR;
        const t = index / (totalCount - 1);
        const startR = parseInt(startColor.slice(1, 3), 16);
        const startG = parseInt(startColor.slice(3, 5), 16);
        const startB = parseInt(startColor.slice(5, 7), 16);
        const endR = parseInt(endColor.slice(1, 3), 16);
        const endG = parseInt(endColor.slice(3, 5), 16);
        const endB = parseInt(endColor.slice(5, 7), 16);
        const r = Math.round((1 - t) * startR + t * endR);
        const g = Math.round((1 - t) * startG + t * endG);
        const b = Math.round((1 - t) * startB + t * endB);
        return `rgb(${r},${g},${b})`;
    }
}
