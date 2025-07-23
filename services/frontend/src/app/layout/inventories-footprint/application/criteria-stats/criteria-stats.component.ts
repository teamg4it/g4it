/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, inject, input, Signal } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { Filter, TransformedDomain } from "src/app/core/interfaces/filter.interface";
import {
    ApplicationFootprint,
    ApplicationImpact,
} from "src/app/core/interfaces/footprint.interface";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";

interface CriteriaData {
    appCount: number;
    averageImpactSip: number;
    averageImpactUnit: number;
}

@Component({
    selector: "app-criteria-stats",
    templateUrl: "./criteria-stats.component.html",
})
export class CriteriaStatsComponent {
    protected footprintStore = inject(FootprintStoreService);
    private readonly filterService = inject(FilterService);
    private readonly translate = inject(TranslateService);

    selectedCriteria = computed(() => {
        return this.translate.instant(
            `criteria.${this.footprintStore.applicationCriteria()}`,
        );
    });

    footprint = input<ApplicationFootprint[]>([]);
    noData: boolean = false;
    isSip: boolean = true;

    criteriaSignal: Signal<CriteriaData> = computed(() => {
        return this.computeApplicationStats();
    });

    constructor(
        private readonly appComponent: InventoriesApplicationFootprintComponent,
    ) {}

    updateSelectedUnite(typeOfUnit: string) {
        switch (typeOfUnit) {
            case "impact":
                this.isSip = false;
                break;
            case "peopleeq":
                this.isSip = true;
                break;
        }
    }

    computeData(
        appNameList: Set<string>,
        impact: ApplicationImpact,
        count: number,
        sipAvgImpact: { value: number },
        unitAvgImpact: { value: number },
    ) {
        appNameList.add(impact.applicationName);
        count++;
        sipAvgImpact.value += impact.sip;
        unitAvgImpact.value += impact.impact;
    }

    domainCase(
        domain: TransformedDomain | undefined,
        appNameList: Set<string>,
        impact: ApplicationImpact,
        count: number,
        sipAvgImpact: { value: number },
        unitAvgImpact: { value: number },
    ) {
        if (domain?.checked) {
            this.computeData(appNameList, impact, count, sipAvgImpact, unitAvgImpact);
        }
    }

    subDomainCase(
        domain: TransformedDomain | undefined,
        appNameList: Set<string>,
        impact: ApplicationImpact,
        count: number,
        sipAvgImpact: { value: number },
        unitAvgImpact: { value: number },
    ) {
        if (
            domain?.children?.some(
                (child) => child.label === impact.subDomain && child.checked,
            )
        ) {
            this.computeData(appNameList, impact, count, sipAvgImpact, unitAvgImpact);
        }
    }

    applicationCase(
        appNameList: Set<string>,
        impact: ApplicationImpact,
        count: number,
        sipAvgImpact: { value: number },
        unitAvgImpact: { value: number },
    ) {
        if (this.footprintStore.appApplication() === impact.applicationName) {
            this.computeData(appNameList, impact, count, sipAvgImpact, unitAvgImpact);
        }
    }

    computeApplicationStats(): CriteriaData {
        let sipAvgImpact = { value: 0 };
        let unitAvgImpact = { value: 0 };
        let count = 0;
        let appNameList: Set<string> = new Set();

        this.footprint().forEach((application) => {
            if (application.criteria === this.footprintStore.applicationCriteria()) {
                application.impacts.forEach((impact: ApplicationImpact) => {
                    const domain = (
                        this.footprintStore.applicationSelectedFilters() as Filter<TransformedDomain>
                    )["domain"].find((d) => d?.label === impact.domain);

                    if (
                        this.filterService.getFilterincludes(
                            this.footprintStore.applicationSelectedFilters(),
                            impact,
                        )
                    ) {
                        switch (this.footprintStore.appGraphType()) {
                            case "global":
                                this.computeData(
                                    appNameList,
                                    impact,
                                    count,
                                    sipAvgImpact,
                                    unitAvgImpact,
                                );
                                break;
                            case "domain":
                                this.domainCase(
                                    domain,
                                    appNameList,
                                    impact,
                                    count,
                                    sipAvgImpact,
                                    unitAvgImpact,
                                );
                                break;
                            case "subdomain":
                                this.subDomainCase(
                                    domain,
                                    appNameList,
                                    impact,
                                    count,
                                    sipAvgImpact,
                                    unitAvgImpact,
                                );
                                break;
                            case "application":
                                this.applicationCase(
                                    appNameList,
                                    impact,
                                    count,
                                    sipAvgImpact,
                                    unitAvgImpact,
                                );
                                break;
                        }
                    }
                });
            }
        });

        let appCount = appNameList.size;
        let averageImpactSip = 0;
        let averageImpactUnit = 0;

        if (appCount !== 0) {
            averageImpactSip = sipAvgImpact.value / appCount;
            averageImpactUnit = unitAvgImpact.value / appCount;
        }

        return {
            appCount,
            averageImpactSip,
            averageImpactUnit,
        };
    }
}
