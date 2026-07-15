/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Injectable } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { map, tap } from "rxjs";
import {
    ApplicationFootprint,
    ApplicationImpact,
    Criterias,
    Impact,
    RepartitionImpact,
} from "src/app/core/interfaces/footprint.interface";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import { Constants } from "src/constants";
import { StatusCount, StatusCountMap } from "../../interfaces/digital-service.interfaces";
import {
    ConstantApplicationFilter,
    Filter,
    TransformedDomain,
} from "../../interfaces/filter.interface";
import { FootprintCalculated, SumImpact } from "../../interfaces/footprint.interface";
import { OutApplicationsRest } from "../../interfaces/output.interface";
import { transformCriterion } from "../mapper/array";

@Injectable({
    providedIn: "root",
})
export class FootprintService {
    constructor(
        private readonly footprintDataService: FootprintDataService,
        private readonly translate: TranslateService,
    ) {}

    deleteIndicators(inventoryId: number) {
        return this.footprintDataService.deleteIndicators(inventoryId);
    }

    initApplicationFootprint(inventoryId: number, currentWorkspaceName: string) {
        return this.footprintDataService.getApplicationFootprint(inventoryId).pipe(
            map((footprint) =>
                this.footprintDataService.transformApplicationEquipmentType(
                    footprint,
                    currentWorkspaceName,
                ),
            ),
            tap((footprint) => {
                footprint = this.setUnspecifiedData(footprint);
                for (const indicateur of footprint) {
                    indicateur.criteriaTitle = this.translate.instant(
                        `criteria.${indicateur.criteria}.title`,
                    );
                    indicateur.id = inventoryId;
                }
            }),
        );
    }

    setUnspecifiedData(footprint: ApplicationFootprint[]) {
        for (const element of footprint) {
            for (const impact of element.impacts) {
                this.setUnspecifiedDataImpact(impact);
            }
        }
        return footprint;
    }

    setUnspecifiedDataImpact(impact: ApplicationImpact) {
        for (const key in impact) {
            if (String(impact[key as keyof ApplicationImpact])?.trim() === "") {
                (impact as any)[key] = Constants.UNSPECIFIED;
            }
        }
    }

    addImpact(i1: SumImpact, i2: SumImpact): SumImpact {
        return {
            impact: i1.impact + i2.impact,
            sip: i1.sip + i2.sip,
        };
    }

    addStatus(i1: StatusCount, i2: StatusCount): StatusCount {
        return {
            ok: i1.ok + i2.ok,
            error: i1.error + i2.error,
            total: i1.total + i2.total,
        };
    }

    private calculateDimensionStatus(
        filteredImpacts: Impact[],
        selectedView: string,
        dimension: string,
    ): StatusCount {
        const dimStatusOk = filteredImpacts.filter(
            (i) =>
                this.checkIfEmpty(this.valueImpact(i, selectedView)) === dimension &&
                i.statusIndicator === Constants.DATA_QUALITY_STATUS.ok,
        ).length;
        const dimStatusError = filteredImpacts.filter(
            (i) =>
                this.checkIfEmpty(this.valueImpact(i, selectedView)) === dimension &&
                i.statusIndicator !== Constants.DATA_QUALITY_STATUS.ok,
        ).length;
        return {
            ok: dimStatusOk,
            error: dimStatusError,
            total: dimStatusOk + dimStatusError,
        };
    }

    setGroupedSumImpacts(
        filteredImpacts: Impact[],
        selectedView: string,
        groupedSumImpacts: Map<string, SumImpact>,
    ) {
        for (const impact of filteredImpacts) {
            let key = this.valueImpact(impact, selectedView)!;
            if (!key) key = Constants.EMPTY;

            if (!impact.impact) {
                impact.impact = 0;
                impact.sip = 0;
            }

            groupedSumImpacts.set(
                key,
                this.addImpact(
                    groupedSumImpacts.get(key) || { impact: 0, sip: 0 },
                    impact,
                ),
            );
        }
    }

    performActionsOnFootprint(
        viewExist: FootprintCalculated | undefined,
        impact: Impact,
        view: FootprintCalculated,
        footprintCalculated: FootprintCalculated[],
    ) {
        if (viewExist) {
            viewExist.impacts.push(impact);
            viewExist.total = this.addImpact(viewExist.total, view.total);
            viewExist.status = this.addStatus(viewExist.status, view.status);
        } else {
            footprintCalculated.push(view);
        }
    }

    filterCriteriaImpact(footprint: ApplicationFootprint[]) {
        return footprint.map((impact) => {
            const data = impact.impacts.reduce(
                (sum, current) => this.addImpact(sum, current),
                {
                    impact: 0,
                    sip: 0,
                },
            );
            return {
                name: impact.criteria,
                title: impact.criteriaTitle,
                unite: impact.unit,
                raw: data.impact,
                peopleeq: data.sip,
            };
        });
    }

    calculate(
        footprint: Criterias,
        filters: Filter,
        selectedView: string,
        filterFields: string[],
        isAxisInverted: boolean,
        shouldShowStackBarChart: boolean,
    ): {
        footprintCalculated: FootprintCalculated[];
        criteriaCountMap: StatusCountMap;
        impactsWithMaxDimensions: RepartitionImpact[];
    } {
        if (footprint === undefined)
            return {
                footprintCalculated: [],
                criteriaCountMap: {},
                impactsWithMaxDimensions: [],
            };

        const footprintCalculated: FootprintCalculated[] = [];
        const criteriaCountMap: StatusCountMap = {};
        let impactsWithMaxDimensions = [];

        const order = LifeCycleUtils.getLifeCycleList();
        const lifeCycleMap = LifeCycleUtils.getLifeCycleMap();

        const filtersSet: any = {};
        for (const field of filterFields) {
            filtersSet[field] = new Set(filters[field]);
        }

        const hasAllFilters = Object.keys(filtersSet).every((item) =>
            filtersSet[item].has(Constants.ALL),
        );

        for (let criteria in footprint) {
            if (!footprint[criteria]?.impacts) continue;

            const filteredImpacts = hasAllFilters
                ? footprint[criteria].impacts
                : footprint[criteria].impacts.filter((impact: Impact) => {
                      let isPresent = true;
                      for (const field in filtersSet) {
                          let value = this.valueImpact(impact, field)!;
                          if (!value) value = Constants.EMPTY;

                          if (!filtersSet[field].has(value)) {
                              isPresent = false;
                              break;
                          }
                      }
                      return isPresent;
                  });

            const { impact, sip } = filteredImpacts.reduce(
                (sum, current) => this.addImpact(sum, current),
                {
                    impact: 0,
                    sip: 0,
                },
            );
            impactsWithMaxDimensions.push({
                name: transformCriterion(footprint[criteria].label),
                title: this.translate.instant(`criteria.${criteria}`).title,
                unite: this.translate.instant(`criteria.${criteria}`).unite,
                raw: impact,
                peopleeq: sip,
                maxCriteria: this.findMaxRepartition(filteredImpacts, selectedView),
            });

            const groupedSumImpacts = new Map<string, SumImpact>();

            this.setGroupedSumImpacts(filteredImpacts, selectedView, groupedSumImpacts);

            if (isAxisInverted) {
                // When inverted: populate criteriaCountMap per dimension (which becomes the key)
                for (let [dimension] of groupedSumImpacts) {
                    const status = this.calculateDimensionStatus(
                        filteredImpacts,
                        selectedView,
                        dimension,
                    );
                    const translatedDim = lifeCycleMap.get(dimension) ?? dimension;
                    if (!criteriaCountMap[translatedDim]) {
                        criteriaCountMap[translatedDim] = { status };
                    } else {
                        criteriaCountMap[translatedDim].status = this.addStatus(
                            criteriaCountMap[translatedDim].status,
                            status,
                        );
                    }
                }
            } else {
                // Normal mode: populate criteriaCountMap per criteria
                criteriaCountMap[criteria] = {
                    status: {
                        ok: filteredImpacts.filter(
                            (i) => i.statusIndicator === Constants.DATA_QUALITY_STATUS.ok,
                        ).length,
                        error: filteredImpacts.filter(
                            (i) => i.statusIndicator !== Constants.DATA_QUALITY_STATUS.ok,
                        ).length,
                        total: filteredImpacts.length,
                    },
                };
            }

            if (isAxisInverted !== shouldShowStackBarChart) {
                // When inverted: criteria is the outer grouping, dimensions are inner
                for (let [dimension, sumImpact] of groupedSumImpacts) {
                    const translated = lifeCycleMap.get(dimension);
                    const impact = {
                        criteria: translated ?? dimension,
                        sumSip: sumImpact.sip,
                        sumImpact: sumImpact.impact,
                    } as Impact;

                    const status = this.calculateDimensionStatus(
                        filteredImpacts,
                        selectedView,
                        dimension,
                    );

                    let viewExist = footprintCalculated.find(
                        (data: any) => data.data === criteria,
                    );

                    if (viewExist) {
                        viewExist.impacts.push(impact);
                        viewExist.total.impact += impact.sumImpact;
                        viewExist.total.sip += impact.sumSip;
                        viewExist.status = this.addStatus(viewExist.status, status);
                    } else {
                        footprintCalculated.push({
                            data: criteria,
                            impacts: [impact],
                            total: {
                                impact: impact.sumImpact,
                                sip: impact.sumSip,
                            },
                            status,
                        });
                    }
                }
            } else {
                // Normal mode: dimensions are outer grouping, criteria are inner
                for (let [dimension, sumImpact] of groupedSumImpacts) {
                    const translated = lifeCycleMap.get(dimension);
                    const impact = {
                        criteria,
                        sumSip: sumImpact.sip,
                        sumImpact: sumImpact.impact,
                    } as Impact;

                    const status = this.calculateDimensionStatus(
                        filteredImpacts,
                        selectedView,
                        dimension,
                    );

                    const view: FootprintCalculated = {
                        data: translated ?? dimension,
                        impacts: [impact],
                        total: {
                            impact: impact.sumImpact,
                            sip: impact.sumSip,
                        },
                        status,
                    };

                    const viewExist = footprintCalculated.find(
                        (data: any) => data.data === view.data,
                    );
                    this.performActionsOnFootprint(
                        viewExist,
                        impact,
                        view,
                        footprintCalculated,
                    );
                }
            }
        }
        // In case of Inverse sort is handled in multi criteria component file
        if (selectedView === Constants.ACV_STEP && !isAxisInverted) {
            footprintCalculated.sort((a: any, b: any) => {
                return order.indexOf(a.data) - order.indexOf(b.data);
            });
        } else {
            // Sort by alphabetical order
            footprintCalculated.sort((a: any, b: any) => a.data.localeCompare(b.data));
        }
        const filterAllImpactsWithMax = [...impactsWithMaxDimensions]
            .sort((a, b) => b.peopleeq - a.peopleeq)
            .slice(0, 3);
        return {
            footprintCalculated,
            criteriaCountMap,
            impactsWithMaxDimensions: filterAllImpactsWithMax,
        };
    }

    findMaxRepartition(filteredImpacts: Impact[], selectedView: string) {
        if (!filteredImpacts || filteredImpacts.length === 0) {
            return {
                name: Constants.EMPTY,
                peopleeq: 0,
                raw: 0,
            };
        }

        const groupedByDimension = new Map<string, SumImpact>();

        // Reuse setGroupedSumImpacts to group impacts by selectedView dimension
        this.setGroupedSumImpacts(filteredImpacts, selectedView, groupedByDimension);
        // Find the dimension with maximum peopleeq (sip)
        let maxDimension = Constants.EMPTY;
        let maxSumImpact: SumImpact = { impact: 0, sip: 0 };

        for (const [dimension, sumImpact] of groupedByDimension) {
            if (sumImpact.sip > maxSumImpact.sip) {
                maxSumImpact = sumImpact;
                maxDimension =
                    selectedView === Constants.ACV_STEP
                        ? this.translate.instant(
                              "acvStep." +
                                  LifeCycleUtils.getLifeCycleMap().get(dimension),
                          )
                        : dimension;
            }
        }

        return {
            name: Constants.EMPTY === maxDimension ? "Empty" : maxDimension,
            peopleeq: maxSumImpact.sip,
            raw: maxSumImpact.impact,
        };
    }

    checkIfEmpty(input: string): string {
        if (!input) {
            return Constants.EMPTY;
        }
        return input;
    }

    valueImpact(v: Impact, dimension: string) {
        switch (dimension) {
            case Constants.ACV_STEP:
                return v.acvStep;
            case "country":
                return v.country;
            case "entity":
                return v.entity;
            case "equipment":
                return v.equipment;
            case "status":
                return v.status;

            // application
            case "lifeCycle":
                return (v as any).lifeCycle;
            case "environment":
                return (v as any).environment;
            case "equipmentType":
                return (v as any).equipmentType;
            case "domain":
                return (v as any).domain;
            case "subDomain":
                return (v as any).subDomain;
            case "location":
                return (v as any).location;
            default:
                return null;
        }
    }

    getUniqueValues(
        footprint: ApplicationFootprint[] | Criterias,
        appConstant: ConstantApplicationFilter[] | string[],
        isEquipment: boolean,
    ) {
        const uniqueValues = this.initializeUniqueSets(appConstant, isEquipment);
        const normalizedFootprint = this.prepareFootprint(footprint, isEquipment);
        this.collectUniqueValues(
            normalizedFootprint,
            appConstant,
            uniqueValues,
            isEquipment,
        );
        return this.convertSetsToArrays(uniqueValues);
    }

    private initializeUniqueSets(
        appConstant: ConstantApplicationFilter[] | string[],
        isEquipment: boolean,
    ): { [key: string]: Set<string> } {
        const uniqueValues: { [key: string]: Set<string> } = {};
        if (isEquipment) {
            for (const fieldObj of appConstant as string[]) {
                uniqueValues[fieldObj] = new Set<string>();
            }
        } else {
            for (const fieldObj of appConstant as ConstantApplicationFilter[]) {
                uniqueValues[fieldObj.field] = new Set<string>();
            }
        }
        return uniqueValues;
    }

    private prepareFootprint(
        footprint: ApplicationFootprint[] | Criterias,
        isEquipment: boolean,
    ): any[] {
        if (!isEquipment) {
            return footprint as ApplicationFootprint[];
        }
        return Object.keys(footprint as Criterias).map((key) => ({
            criteria: key,
            ...(footprint as Criterias)[key],
        }));
    }

    private collectUniqueValues(
        footprint: any[],
        appConstant: ConstantApplicationFilter[] | string[],
        uniqueValues: { [key: string]: Set<string> },
        isEquipment: boolean,
    ): void {
        for (const criteria of footprint) {
            for (const impact of criteria.impacts) {
                const criteriaImpact = impact;
                if (isEquipment) {
                    this.addEquipmentValues(
                        criteriaImpact,
                        appConstant as string[],
                        uniqueValues,
                    );
                } else {
                    this.addApplicationValues(
                        criteriaImpact,
                        appConstant as ConstantApplicationFilter[],
                        uniqueValues,
                    );
                }
            }
        }
    }

    private addEquipmentValues(
        impact: any,
        fields: string[],
        uniqueValues: { [key: string]: Set<string> },
    ): void {
        for (const field of fields) {
            uniqueValues[field].add(impact[field] ?? "");
        }
    }

    private addApplicationValues(
        impact: any,
        fields: ConstantApplicationFilter[],
        uniqueValues: { [key: string]: Set<string> },
    ): void {
        for (const fieldObj of fields) {
            this.populateSets(uniqueValues, fieldObj, impact);
        }
    }

    private convertSetsToArrays(uniqueValues: { [key: string]: Set<string> }): {
        [key: string]: string[] | TransformedDomain[];
    } {
        const result: { [key: string]: string[] | TransformedDomain[] } = {};
        for (const key in uniqueValues) {
            if (key === "domain") {
                result[key] = this.convertToDesiredFormat(uniqueValues[key] as any);
            } else {
                result[key] = Array.from(uniqueValues[key]);
            }
        }
        return result;
    }

    populateSets(
        uniqueValues: { [key: string]: Set<string> },
        fieldObj: ConstantApplicationFilter,
        criteriaImpact: any,
    ) {
        const fieldSet = uniqueValues[fieldObj.field] as any;

        let domainSet = fieldSet[criteriaImpact[fieldObj.field]];
        if (!domainSet) {
            fieldSet[criteriaImpact[fieldObj.field]] = new Set<string>();
        }

        if (fieldObj.children) {
            for (const child of fieldObj.children) {
                fieldSet[criteriaImpact[fieldObj.field]].add(criteriaImpact[child.field]);
            }
        } else {
            fieldSet.add(criteriaImpact[fieldObj.field]);
        }
    }

    convertToDesiredFormat(domainObject: {
        [key: string]: Set<string>;
    }): TransformedDomain[] {
        const result: TransformedDomain[] = [];

        for (const domain in domainObject) {
            const domainEntry: TransformedDomain = {
                field: "domain",
                label: domain,
                key: domain?.toLowerCase(),
                checked: true,
                visible: true,
                children: [],
                collapsed: true,
            };

            for (const subDomain of domainObject[domain]) {
                domainEntry.children.push({
                    field: "subDomain",
                    label: subDomain,
                    key: subDomain?.toLowerCase(),
                    checked: true,
                    visible: true,
                });
            }

            result.push(domainEntry);
        }

        return result;
    }

    getTransformOutApplications(outApplication: OutApplicationsRest[]) {
        return outApplication.map(
            (outApp) =>
                ({
                    criteria: transformCriterion(outApp.criterion),
                    applicationName: outApp.name,
                    domain: outApp.filters?.[0],
                    subDomain: outApp.filters?.[1],
                    environment: outApp.environment,
                    equipmentType: `Cloud ${(outApp?.provider ?? "").toUpperCase()}`,
                    lifeCycle:
                        LifeCycleUtils.getLifeCycleMapReverse().get(
                            outApp.lifecycleStep,
                        ) ?? outApp.lifecycleStep,
                    virtualEquipmentName: outApp.virtualEquipmentName,
                    cluster: outApp.filtersVirtualEquipment?.[0],
                    impact: outApp.unitImpact,
                    sip: outApp.peopleEqImpact,
                    statusIndicator: outApp.statusIndicator,
                }) as ApplicationImpact,
        );
    }
}
