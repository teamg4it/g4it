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
                footprint.forEach((indicateur) => {
                    indicateur.criteriaTitle = this.translate.instant(
                        `criteria.${indicateur.criteria}.title`,
                    );
                    indicateur.id = inventoryId;
                });
            }),
        );
    }

    setUnspecifiedData(footprint: ApplicationFootprint[]) {
        footprint.forEach((element) => {
            element.impacts.forEach((impact) => {
                this.setUnspecifiedDataImpact(impact);
            });
        });
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

    calculate(
        footprint: Criterias,
        filters: Filter,
        selectedView: string,
        filterFields: string[],
    ): { footprintCalculated: FootprintCalculated[]; criteriaCountMap: StatusCountMap } {
        if (footprint === undefined)
            return { footprintCalculated: [], criteriaCountMap: {} };

        const footprintCalculated: FootprintCalculated[] = [];
        const criteriaCountMap: StatusCountMap = {};

        const order = LifeCycleUtils.getLifeCycleList();
        const lifeCycleMap = LifeCycleUtils.getLifeCycleMap();

        const filtersSet: any = {};
        filterFields.forEach((field) => (filtersSet[field] = new Set(filters[field])));

        const hasAllFilters = Object.keys(filtersSet).every((item) =>
            filtersSet[item].has(Constants.ALL),
        );

        for (let criteria in footprint) {
            if (!footprint[criteria] || !footprint[criteria].impacts) continue;

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
            const groupedSumImpacts = new Map<string, SumImpact>();

            this.setGroupedSumImpacts(filteredImpacts, selectedView, groupedSumImpacts);
            for (let [dimension, sumImpact] of groupedSumImpacts) {
                const impact = {
                    criteria,
                    sumSip: sumImpact.sip,
                    sumImpact: sumImpact.impact,
                } as Impact;

                const translated = lifeCycleMap.get(dimension);

                const view: FootprintCalculated = {
                    data: translated ?? dimension,
                    impacts: [impact],
                    total: {
                        impact: impact.sumImpact,
                        sip: impact.sumSip,
                    },
                    status: {
                        ok: filteredImpacts.filter(
                            (i) =>
                                this.checkIfEmpty(this.valueImpact(i, selectedView)!) ===
                                    dimension && i.statusIndicator === "OK",
                        ).length,
                        error: filteredImpacts.filter(
                            (i) =>
                                this.checkIfEmpty(this.valueImpact(i, selectedView)!) ===
                                    dimension && i.statusIndicator !== "OK",
                        ).length,
                        total: filteredImpacts.filter(
                            (i) =>
                                this.checkIfEmpty(this.valueImpact(i, selectedView)!) ===
                                dimension,
                        ).length,
                    },
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

        if (selectedView === Constants.ACV_STEP) {
            footprintCalculated.sort((a: any, b: any) => {
                return order.indexOf(a.data) - order.indexOf(b.data);
            });
        } else {
            // Sort by alphabetical order
            footprintCalculated.sort((a: any, b: any) => a.data.localeCompare(b.data));
        }

        return { footprintCalculated, criteriaCountMap };
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
            default:
                return null;
        }
    }

    getUniqueValues(
        footprint: ApplicationFootprint[] | Criterias,
        appConstant: ConstantApplicationFilter[] | string[],
        isEquipment: boolean,
    ) {
        const uniqueValues: { [key: string]: Set<string> } = {};
        let equipmentFootprint: any = [];

        // Initialize sets for each field
        if (!isEquipment) {
            (appConstant as ConstantApplicationFilter[]).forEach((fieldObj) => {
                uniqueValues[fieldObj.field] = new Set<string>();
            });
        } else {
            (appConstant as string[]).forEach((fieldObj) => {
                uniqueValues[fieldObj] = new Set<string>();
            });
        }
        if (isEquipment) {
            equipmentFootprint = Object.keys(footprint as Criterias).map((key) => ({
                criteria: key,
                ...(footprint as Criterias)[key],
            }));
        }
        // Populate sets with unique values
        (
            (!isEquipment ? footprint : equipmentFootprint) as ApplicationFootprint[]
        ).forEach((criteria) => {
            criteria.impacts.forEach((impact) => {
                const criteriaImpact = impact as any;
                if (!isEquipment) {
                    (appConstant as ConstantApplicationFilter[]).forEach((fieldObj) => {
                        this.populateSets(uniqueValues, fieldObj, criteriaImpact);
                    });
                } else {
                    (appConstant as string[]).forEach((fieldObj) => {
                        uniqueValues[fieldObj].add(criteriaImpact[fieldObj] ?? "");
                    });
                }
            });
        });
        // Convert sets to arrays
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
            fieldObj.children.forEach((child) => {
                fieldSet[criteriaImpact[fieldObj.field]].add(criteriaImpact[child.field]);
            });
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

            domainObject[domain].forEach((subDomain) => {
                domainEntry.children.push({
                    field: "subDomain",
                    label: subDomain,
                    key: subDomain?.toLowerCase(),
                    checked: true,
                    visible: true,
                });
            });

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
