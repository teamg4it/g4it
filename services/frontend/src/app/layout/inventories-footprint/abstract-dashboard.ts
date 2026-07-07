/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { Subject } from "rxjs";
import { StatusCountMap } from "src/app/core/interfaces/digital-service.interfaces";
import { FootprintCalculated, Impact } from "src/app/core/interfaces/footprint.interface";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import {
    getColorFormatter,
    getLabelFormatter,
} from "src/app/core/service/mapper/graphs-mapper";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";

@Component({
    template: "",
})
export class AbstractDashboard {
    ngUnsubscribe = new Subject<void>();
    selectedLang: string = "en";

    constructor(
        protected translate: TranslateService,
        protected integerPipe: IntegerPipe,
        protected decimalsPipe: DecimalsPipe,
        protected globalStore: GlobalStoreService,
    ) {}

    existingTranslation(param: string, view: string, type: string = "series") {
        let key = view + "." + param;
        if (param === "other") {
            key = type === "legend" ? "common.otherLegend" : "common.other";
        }
        if (param === Constants.EMPTY) {
            return this.translate.instant("common.empty");
        }
        return this.translate.instant(key) === key ? param : this.translate.instant(key);
    }

    getCriteriaDimensionTranslation(
        isInverted: boolean,
        dimension: string,
        selectedView: string,
    ) {
        return isInverted
            ? this.translate.instant(`criteria.${dimension}`).title
            : this.existingTranslation(dimension, selectedView);
    }

    getCriteriaTranslation(input: string) {
        this.selectedLang = this.translate.currentLang;
        if (Object.keys(this.globalStore.criteriaList()).includes(input)) {
            return this.translate.instant(
                "criteria." + input.toLowerCase().replace(/\s+/g, "-") + ".title",
            );
        } else {
            return this.translate.instant(
                "criteria-title." + input.toLowerCase().replace(/\s+/g, "-") + ".title",
            );
        }
    }

    getSelectedCriteriaData(
        barChartData: any,
        key: string,
        selectedCriteria: string,
    ): any[] {
        const selectedData = barChartData?.find(
            (impact: any) => impact.criteria === selectedCriteria,
        );
        return selectedData ? selectedData[key] : [];
    }

    createAngleAxisConfig(
        footprintCalculated: FootprintCalculated[],
        criteriaCountMap: StatusCountMap,
        isInverted: boolean,
        selectedView: string,
        enableDataInconsistency: boolean,
    ) {
        return {
            type: "category" as const,
            data: footprintCalculated[0].impacts.map((impact: Impact) => {
                return {
                    value: impact.criteria,
                    textStyle: {
                        color: getColorFormatter(
                            !!criteriaCountMap[impact.criteria]?.status?.error,
                            enableDataInconsistency,
                        ),
                    },
                };
            }),
            axisLabel: {
                formatter: (value: any) => {
                    const title = this.getCriteriaDimensionTranslation(
                        !isInverted,
                        value,
                        selectedView,
                    );
                    const hasError = !!criteriaCountMap[value]?.status?.error;
                    return getLabelFormatter(hasError, enableDataInconsistency, title);
                },
                rich: Constants.CHART_RICH as any,
                margin: 26,
            },
        };
    }

    createRadiusAxisConfig() {
        return {
            name: this.translate.instant("common.peopleeq"),
            nameLocation: "end" as const,
            nameGap: 21,
            nameTextStyle: {
                fontStyle: "italic" as const,
            },
        };
    }

    createLegendConfig(
        footprintCalculated: FootprintCalculated[],
        isInverted: boolean,
        selectedView: string,
    ) {
        return {
            type: "scroll" as const,
            bottom: 0,
            data: footprintCalculated.map((item: FootprintCalculated) => item.data),
            formatter: (param: any) => {
                return this.getCriteriaDimensionTranslation(
                    isInverted,
                    param,
                    selectedView,
                );
            },
        };
    }
}
