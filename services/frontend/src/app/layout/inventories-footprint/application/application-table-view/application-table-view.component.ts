import { Component, computed, inject, input } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { Filter } from "src/app/core/interfaces/filter.interface";
import {
    ApplicationFootprint,
    ApplicationImpact,
} from "src/app/core/interfaces/footprint.interface";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FilterService } from "src/app/core/service/business/filter.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { FootprintStoreService } from "src/app/core/store/footprint.store";

@Component({
    selector: "app-application-table-view",
    standalone: true,
    imports: [SharedModule],
    templateUrl: "./application-table-view.component.html",
    styleUrls: ["./application-table-view.component.scss"],
})
export class ApplicationTableViewComponent {
    footprint = input<ApplicationFootprint[]>([]);

    private readonly footprintStore = inject(FootprintStoreService);
    private readonly filterService = inject(FilterService);
    protected readonly translate = inject(TranslateService);
    protected readonly integerPipe = inject(IntegerPipe);
    protected readonly decimalsPipe = inject(DecimalsPipe);
    tableView = computed(() => {
        console.log(this.footprint());
        console.log(
            this.filterImpacts(
                this.footprintStore.applicationSelectedFilters(),
                this.footprint(),
            ),
        );
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
        console.log(data);
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
}
