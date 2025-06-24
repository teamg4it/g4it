import { Constants } from "src/constants";
import {
    CloudsImpact,
    DigitalServiceCloudImpact,
    DigitalServiceFootprint,
    DigitalServiceFootprintImpact,
    DigitalServiceNetworksImpact,
    DigitalServiceServersImpact,
    DigitalServiceTerminalsImpact,
    Host,
    ImpactACVStep,
    ImpactSipValue,
    NetworkType,
    ServerImpact,
    ServersType,
    TerminalsImpact,
    TerminalsImpactTypeLocation,
    TerminalsType,
} from "../../interfaces/digital-service.interfaces";
import { Impact } from "../../interfaces/footprint.interface";
import { MapString } from "../../interfaces/generic.interfaces";
import {
    OutPhysicalEquipmentRest,
    OutVirtualEquipmentRest,
} from "../../interfaces/output.interface";
import { getLifeCycleMapReverse } from "../../utils/lifecycle";
import {
    groupByCriterion,
    groupByField,
    sumByProperty,
    transformCriterion,
} from "./array";

export const convertToGlobalVision = (
    physicalEquipments: OutPhysicalEquipmentRest[],
    virtualEquipments: OutVirtualEquipmentRest[],
): DigitalServiceFootprint[] => {
    const globalVision: DigitalServiceFootprint[] = [];

    ["Terminal", "Network"].forEach((tier) => {
        const data = physicalEquipments.filter((pe) => pe.equipmentType === tier);
        if (data.length > 0) {
            globalVision.push({
                tier: tier,
                impacts: aggregateAndMap(data),
            });
        }
    });
    const data = physicalEquipments.filter((pe) =>
        ["Dedicated Server"].includes(pe.equipmentType),
    );
    const vms = virtualEquipments.filter((pe) =>
        ["Shared Server"].includes(pe.equipmentType),
    );
    const physicalEqpsAndVms = [...data, ...vms];
    if (physicalEqpsAndVms.length > 0) {
        globalVision.push({
            tier: "Server",
            impacts: aggregateAndMap(physicalEqpsAndVms),
        });
    }

    const cloudData = virtualEquipments.filter(
        (ve) => ve.infrastructureType === "CLOUD_SERVICES",
    );

    if (cloudData.length > 0) {
        globalVision.push({
            tier: "CloudService",
            impacts: aggregateAndMap(cloudData),
        });
    }

    return globalVision;
};

const aggregateAndMap = (data: any[]): DigitalServiceFootprintImpact[] => {
    return aggregateImpacts(data, ["criterion", "unit", "statusIndicator"]).map(
        (item) => {
            return {
                criteria: transformCriterion(item.criterion),
                countValue: item.countValue,
                sipValue: item.peopleEqImpact,
                unitValue: item.unitImpact,
                status: item.statusIndicator,
                unit: item.unit,
            };
        },
    );
};

export const transformOutPhysicalEquipmentsToTerminalData = (
    physicalEquipments: OutPhysicalEquipmentRest[],
    deviceTypes: TerminalsType[],
): DigitalServiceTerminalsImpact[] => {
    const terminal = physicalEquipments
        .filter((pe) => pe.equipmentType === "Terminal")
        .map((t) => ({
            ...t,
            deviceType: deviceTypes.find((type) => type.code === t.reference)?.value!,
        }));

    const terminalByCriterion = groupByCriterion(terminal);

    const results: DigitalServiceTerminalsImpact[] = [];
    for (const criterion in terminalByCriterion) {
        const impacts = terminalByCriterion[criterion];
        results.push({
            criteria: criterion,
            impactCountry: getImpactsForTerminal(impacts, "location"),
            impactType: getImpactsForTerminal(impacts, "deviceType"),
        });
    }
    return results;
};

export const transformOutPhysicalEquipmentsToNetworkData = (
    physicalEquipments: OutPhysicalEquipmentRest[],
    networkTypes: NetworkType[],
): DigitalServiceNetworksImpact[] => {
    const network = physicalEquipments.filter((pe) => pe.equipmentType === "Network");

    const networkByCriterion = groupByCriterion(network);

    const results: DigitalServiceNetworksImpact[] = [];
    for (const criterion in networkByCriterion) {
        results.push({
            criteria: criterion,
            impacts: aggregateImpacts(networkByCriterion[criterion], [
                "reference",
                "unit",
                "statusIndicator",
            ])
                .map((item) => {
                    return {
                        countValue: item.countValue,
                        networkType: networkTypes.find((n) => n.code === item.reference)
                            ?.value!,
                        rawValue: item.unitImpact,
                        sipValue: item.peopleEqImpact,
                        status:
                            item.statusCount.total === item.statusCount.ok
                                ? "OK"
                                : "ERROR",
                        unit: item.unit,
                    };
                })
                .sort((a, b) => a?.networkType?.localeCompare(b?.networkType)),
        });
    }

    return results;
};

export const transformOutPhysicalEquipmentstoServerData = (
    physicalEquipments: OutPhysicalEquipmentRest[],
    virtualEquipments: OutVirtualEquipmentRest[],
    serverTypes: Host[],
): DigitalServiceServersImpact[] => {
    const server = physicalEquipments
        .filter((pe) => pe.equipmentType.endsWith("Server"))
        .map((s) => {
            const serverType = serverTypes.find((ref) => ref.reference === s.reference);
            return {
                ...s,
                equipmentServerType: `${s.equipmentType?.replace(" Server", "")} ${serverType?.type}`,
            };
        });

    const vms = virtualEquipments.filter(
        (ve) => ve.infrastructureType !== "CLOUD_SERVICES",
    );

    const serverByCriterion = groupByCriterion(server);
    const results: DigitalServiceServersImpact[] = [];
    for (const criterion in serverByCriterion) {
        results.push({
            criteria: criterion,
            impactsServer: getImpactsForServer(
                serverByCriterion[criterion],
                vms.filter((vm) => transformCriterion(vm.criterion) === criterion),
                serverTypes,
            )?.sort((a, b) => {
                return getServerMutualizationServerType(b)?.localeCompare(
                    getServerMutualizationServerType(a),
                );
            }),
        });
    }
    return results;
};

const getServerMutualizationServerType = (server: ServersType): string => {
    return `${server?.mutualizationType} ${server?.serverType}`;
};

export const transformOutVirtualEquipmentsToCloudData = (
    virtualEquipments: OutVirtualEquipmentRest[],
    countryMap: MapString,
): DigitalServiceCloudImpact[] => {
    const cloud = virtualEquipments.filter(
        (pe) => pe.infrastructureType === "CLOUD_SERVICES",
    );

    const cloudByCriterion = groupByCriterion(cloud);

    const results: DigitalServiceCloudImpact[] = [];
    for (const criterion in cloudByCriterion) {
        const impacts = cloudByCriterion[criterion];
        results.push({
            criteria: criterion,
            impactLocation: getImpactsForCloud(impacts, "location", countryMap),
            impactInstance: getImpactsForCloud(impacts, "instanceType"),
        });
    }

    return results;
};

const aggregateImpacts = (arr: any[], keyFields: string[]) => {
    let unit = undefined;
    const temp = arr.reduce((acc: any, obj) => {
        const key = keyFields.map((k) => obj[k]).join("|");
        if (!acc[key]) {
            acc[key] = {
                quantity: 0,
                unitImpact: 0,
                peopleEqImpact: 0,
                countValue: 0,
                statusCount: {
                    ok: 0,
                    error: 0,
                    total: 0,
                },
            };
        }

        acc[key] = {
            quantity: obj.quantity,
            unitImpact: obj.unitImpact + acc[key].unitImpact,
            peopleEqImpact: obj.peopleEqImpact + acc[key].peopleEqImpact,
            countValue: obj.countValue + acc[key].countValue,
            statusCount: {
                ok:
                    (obj.statusIndicator === "OK" ? obj.countValue : 0) +
                    acc[key].statusCount.ok,
                error:
                    (obj.statusIndicator !== "OK" ? obj.countValue : 0) +
                    acc[key].statusCount.error,
                total: obj.countValue + acc[key].statusCount.total,
            },
        };

        if (obj.unit && obj.unit !== "null") {
            unit = obj.unit;
        }
        return acc;
    }, {});

    const results: any[] = [];
    for (const key in temp) {
        const keySplit = key.split("|");
        const element = { ...temp[key], unit };

        for (let i = 0; i < keySplit.length; i++) {
            element[keyFields[i]] = keySplit[i];
        }

        results.push(element);
    }
    return results;
};

const getImpactsForTerminal = (data: OutPhysicalEquipmentRest[], projection: string) => {
    const impactCountryTmp = data.reduce((acc: any, obj: any) => {
        const key = obj[projection];
        const nameKey = obj.name;
        acc[key] = acc[key] ?? {};
        acc[key][nameKey] = acc[key][nameKey] ?? [];

        /*   Logic for averageUsageTime
        avgUsageTime = (obj.quantity * (365 * 24) / obj.numberOfUsers ) * obj.numberOfUsers
        */
        const existingACVStep = acc[key][nameKey].find(
            (impact: Impact) =>
                impact.acvStep === getLifeCycleMapReverse().get(obj.lifecycleStep),
        );

        if (existingACVStep) {
            existingACVStep.sipValue += obj.peopleEqImpact;
            existingACVStep.rawValue += obj.unitImpact;
            existingACVStep.unit = obj.unit;
            existingACVStep.totalNbUsers += obj.numberOfUsers;
            existingACVStep.avgUsageTime += obj.quantity * (365 * 24);
            existingACVStep.statusCount = existingACVStep.statusCount || {
                ok: 0,
                error: 0,
                total: 0,
            };
            existingACVStep.statusCount.ok +=
                obj.statusIndicator === Constants.DATA_QUALITY_STATUS.ok
                    ? obj.countValue
                    : 0;
            existingACVStep.statusCount.error +=
                obj.statusIndicator !== Constants.DATA_QUALITY_STATUS.ok
                    ? obj.countValue
                    : 0;
            existingACVStep.statusCount.total += obj.countValue;
            existingACVStep.status =
                existingACVStep.statusCount.error > 0
                    ? Constants.DATA_QUALITY_STATUS.error
                    : Constants.DATA_QUALITY_STATUS.ok;
        } else {
            acc[key][nameKey].push({
                acvStep: getLifeCycleMapReverse().get(obj.lifecycleStep),
                sipValue: obj.peopleEqImpact,
                rawValue: obj.unitImpact,
                unit: obj.unit,
                totalNbUsers: obj.numberOfUsers,
                avgUsageTime: obj.quantity * (365 * 24),
                status:
                    obj.statusIndicator === Constants.DATA_QUALITY_STATUS.ok
                        ? Constants.DATA_QUALITY_STATUS.ok
                        : Constants.DATA_QUALITY_STATUS.error,
                statusCount: {
                    ok:
                        obj.statusIndicator === Constants.DATA_QUALITY_STATUS.ok
                            ? obj.countValue
                            : 0,
                    error:
                        obj.statusIndicator !== Constants.DATA_QUALITY_STATUS.ok
                            ? obj.countValue
                            : 0,
                    total: obj.countValue,
                },
            });
        }
        return acc;
    }, {});
    const impactCountry: TerminalsImpactTypeLocation[] = [];
    for (const location in impactCountryTmp) {
        const terminalNamesObj = impactCountryTmp[location];
        const terminalsData: TerminalsImpact[] = [];
        for (const terminal in terminalNamesObj) {
            const impacts = terminalNamesObj[terminal];
            terminalsData.push({
                name: terminal,
                avgUsageTime:
                    sumByProperty(impacts, "avgUsageTime") /
                    sumByProperty(impacts, "totalNbUsers"),
                totalNbUsers: sumByProperty(impacts, "totalNbUsers") / 4,
                totalSipValue: sumByProperty(impacts, "sipValue"),
                rawValue: sumByProperty(impacts, "rawValue"),
                unit: impacts[0].unit,
                impact: impacts,
            });
        }
        const filteredImpacts = data.filter(
            (d) => d[projection as keyof OutPhysicalEquipmentRest] === location,
        );
        impactCountry.push({
            name: location,
            terminals: terminalsData,
            status: {
                ok: filteredImpacts.reduce(
                    (acc: number, i: OutPhysicalEquipmentRest) =>
                        acc +
                        (i.statusIndicator === Constants.DATA_QUALITY_STATUS.ok
                            ? i.countValue
                            : 0),
                    0,
                ),
                error: filteredImpacts.reduce(
                    (acc: number, i: OutPhysicalEquipmentRest) =>
                        acc +
                        (i.statusIndicator !== Constants.DATA_QUALITY_STATUS.ok
                            ? i.countValue
                            : 0),
                    0,
                ),
                total: filteredImpacts.reduce(
                    (acc: number, i: OutPhysicalEquipmentRest) =>
                        acc + (i.countValue ?? 0),
                    0,
                ),
            },
        });
    }
    impactCountry.sort((a, b) => a?.name?.localeCompare(b?.name));
    return impactCountry;
};

const getImpactsForCloud = (
    data: OutVirtualEquipmentRest[],
    projection: string,
    countryMap: MapString = {},
) => {
    const impactTmp = data.reduce((acc: any, obj: any) => {
        const key = obj[projection];
        if (!acc[key]) {
            acc[key] = [];
        }
        acc[key].push({
            lifecycleStep: obj.lifecycleStep,
            peopleEqImpact: obj.peopleEqImpact,
            unitImpact: obj.unitImpact,
            quantity: obj.quantity,
            usageDuration: obj.usageDuration,
            workload: obj.workload,
            unit: obj.unit,
            statusIndicator: obj.statusIndicator,
            countValue: obj.countValue,
            provider: obj.provider,
            statusCount: {
                ok: obj.statusIndicator === "OK" ? obj.countValue : 0,
                error: obj.statusIndicator !== "OK" ? obj.countValue : 0,
                total: obj.countValue,
            },
        });
        return acc;
    }, {});

    const results: CloudsImpact[] = [];
    for (const name in impactTmp) {
        const impacts = impactTmp[name];
        const unit = impacts.find((i: any) => i.unit !== "null");
        const impactWithProvider = impacts.find((i: any) => i.provider);
        const totalQuantity = sumByProperty(impacts, "quantity");
        results.push({
            name:
                countryMap[name] ||
                `${impactWithProvider.provider.toUpperCase()}-${name}`,
            totalAvgUsage: sumByProperty(impacts, "usageDuration") / totalQuantity,
            totalAvgWorkLoad: (sumByProperty(impacts, "workload") * 100) / 4,
            totalQuantity,
            totalSipValue: sumByProperty(impacts, "peopleEqImpact"),
            rawValue: sumByProperty(impacts, "unitImpact"),
            unit: unit?.unit || "",
            impact: aggregateImpacts(impacts, [
                "lifecycleStep",
                "unit",
                "statusIndicator",
            ]).map((item) => {
                return {
                    acvStep: item.lifecycleStep,
                    unit: item.unit,
                    status: item.statusIndicator,
                    rawValue: item.unitImpact,
                    sipValue: item.peopleEqImpact,
                    statusCount: item.statusCount,
                };
            }),
        });
    }
    results.sort((a, b) => a?.name?.localeCompare(b?.name));
    return results;
};

const getImpactsForServer = (
    data: OutPhysicalEquipmentRest[],
    vms: OutVirtualEquipmentRest[],
    refServerTypes: Host[],
): ServersType[] => {
    const vmsByServer = groupByField(vms, "physicalEquipmentName");
    const serversByType = groupByField(data, "equipmentServerType");

    const serverTypes: ServersType[] = [];

    for (const type in serversByType) {
        const serversByServer = groupByField(serversByType[type], "name");
        const serversImpacts: ServerImpact[] = [];
        const reference = serversByType[type][0].reference;
        const serverType = refServerTypes.find(
            (ref) => ref.reference === reference,
        )?.type!;

        for (const serverName in serversByServer) {
            const vms = vmsByServer[serverName] || undefined;
            const impactStep = (
                vms
                    ? aggregateImpacts(vms, ["lifecycleStep", "unit", "statusIndicator"])
                    : serversByServer[serverName]
            ).map((server: OutPhysicalEquipmentRest) => {
                return {
                    acvStep: getLifeCycleMapReverse().get(server.lifecycleStep),
                    countValue: server.countValue,
                    sipValue: server.peopleEqImpact,
                    rawValue: server.unitImpact,
                    status: server.statusIndicator,
                    unit: server.unit,
                } as ImpactACVStep;
            });

            const unit = impactStep.find((impact: any) => impact.unit !== "null");
            const hostingEfficiency = serversByServer[serverName].find(
                (item: any) => item.hostingEfficiency,
            ).hostingEfficiency;

            let impactVmDisk: ImpactSipValue[] = [];
            if (vms === undefined) {
                impactVmDisk = [
                    {
                        countValue: sumByProperty(impactStep, "countValue"),
                        name: serverName,
                        quantity: 1,
                        sipValue: sumByProperty(impactStep, "sipValue"),
                        rawValue: sumByProperty(impactStep, "rawValue"),
                        status: "OK",
                        unit: unit.unit,
                    },
                ];
            } else {
                impactVmDisk = aggregateImpacts(vms, [
                    "name",
                    "unit",
                    "statusIndicator",
                ]).map((item) => {
                    return {
                        countValue: item.countValue,
                        name: item.name,
                        unit: item.unit,
                        status: item.statusIndicator,
                        quantity: item.quantity,
                        sipValue: item.peopleEqImpact,
                        rawValue: item.unitImpact,
                    };
                });
            }

            serversImpacts.push({
                name: serverName,
                hostingEfficiency,
                totalSipValue: sumByProperty(impactStep, "sipValue"),
                impactVmDisk,
                impactStep,
            });
        }

        serverTypes.push({
            mutualizationType: type.split(" ")[0],
            serverType,
            servers: serversImpacts,
        });
    }

    return serverTypes;
};
