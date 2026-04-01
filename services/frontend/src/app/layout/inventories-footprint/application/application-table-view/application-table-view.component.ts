import { Component, computed, inject, input } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { GraphDescriptionContent } from "src/app/core/interfaces/digital-service.interfaces";
import { Filter } from "src/app/core/interfaces/filter.interface";
import {
    ApplicationFootprint,
    ApplicationImpact,
} from "src/app/core/interfaces/footprint.interface";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FilterService } from "src/app/core/service/business/filter.service";
import { SharedChartsModule } from "src/app/core/shared/common-chart-module";
import { SharedModule } from "src/app/core/shared/shared.module";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { Constants } from "src/constants";

@Component({
    selector: "app-application-table-view",
    standalone: true,
    imports: [SharedModule, SharedChartsModule],
    templateUrl: "./application-table-view.component.html",
})
export class ApplicationTableViewComponent {
    footprint = input<ApplicationFootprint[]>([]);
    isMulticriteria = input<boolean>(false);

    private readonly footprintStore = inject(FootprintStoreService);
    private readonly filterService = inject(FilterService);
    protected readonly translate = inject(TranslateService);
    protected readonly integerPipe = inject(IntegerPipe);
    protected readonly decimalsPipe = inject(DecimalsPipe);
    tableView = computed(() => {
        return this.filterImpacts(
            this.footprintStore.applicationSelectedFilters(),
            this.footprint(),
        );
    });

    filterImpacts(selectedFilters: Filter, footprint: ApplicationFootprint[]) {
        let data: ApplicationImpact[] = [];
        for (const footprintItem of footprint) {
            for (const impact of footprintItem?.impacts ?? []) {
                if (this.filterService.getFilterincludes(selectedFilters, impact)) {
                    const ifExists = data.find(
                        (d) =>
                            d.applicationName === impact.applicationName &&
                            d.criteria === footprintItem.criteria,
                    );
                    if (ifExists) {
                        ifExists.sip += impact.sip;
                        ifExists.impact += impact.impact;
                    } else {
                        data.push({ ...impact, criteria: footprintItem.criteria });
                    }
                }
            }
        }
        return data.map((d) => {
            const criteria = this.translate.instant("criteria." + d.criteria);
            return {
                applicationName: d.applicationName,
                criteria: criteria.title,
                sip: d.sip,
                impact: d.impact,
                unit: criteria.unite,
            };
        });
    }

    getContentText = computed((): GraphDescriptionContent => {
        let translationKey: string;
        let textResourceDescription: string = "";
        let descriptionData = "";
        let key = "";
        if (this.footprintStore.applicationCriteria() === Constants.MUTLI_CRITERIA) {
            translationKey = `ds-graph-description.global-vision.`;
            descriptionData = this.translate.instant(`${translationKey}description`, {
                criteria: this.footprint()
                    .map(
                        (impact) =>
                            this.translate.instant(`criteria.${impact.criteria}`).title,
                    )
                    .join(", "),
                module: this.translate.instant("ds-graph-module.inventory"),
            });
            key = translationKey;
        } else {
            translationKey = `ds-graph-description.criteria.`;
            descriptionData = this.translate.instant(`${translationKey}description`, {
                module: this.translate.instant("ds-graph-module.inventory"),
            });

            key =
                "criteria." +
                this.footprintStore
                    .applicationCriteria()
                    .toLowerCase()
                    .replaceAll(" ", "-") +
                ".";
        }

        return {
            description: descriptionData,
            scale: this.translate.instant(`${key}scale`),
            analysis: this.translate.instant(
                `ds-graph-description.global-vision.analysis`,
                {
                    module: this.translate.instant("ds-graph-module.inventory"),
                },
            ),
            toGoFurther: this.translate.instant(
                `ds-graph-description.global-vision.inventory-to-go-further`,
            ),
        };
    });
}
