import { Component, EventEmitter, inject, input, Output } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { GraphDescriptionContent } from "src/app/core/interfaces/digital-service.interfaces";

@Component({
    selector: "app-graph-description",
    templateUrl: "./graph-description.component.html",
    styleUrl: "./graph-description.component.scss",
})
export class GraphDescriptionComponent {
    private readonly translate = inject(TranslateService);
    contentVisible = true;
    contentText = input<GraphDescriptionContent>();
    ecoMindRecomendation = input<string>("");
    chartType = input<string>("");
    barChartChild = input<boolean>(false);
    selectedParam = input<string>("");
    textDescriptionImpacts = input<
        {
            text: string;
            impactName: string;
            impactNameVisible: string;
            impactType?: string;
        }[]
    >([]);
    textDescriptionResourceImpacts = input<
        {
            text: string;
            impactName: string;
            impactNameVisible: string;
            impactType?: string;
        }[]
    >([]);

    @Output() impactSelectedEvent: EventEmitter<any> = new EventEmitter();

    toggleContentVisibility() {
        this.contentVisible = !this.contentVisible;
    }

    impactClick(impactName: string, impactType: string) {
        this.impactSelectedEvent.emit(impactName);
        if (impactType) {
            this.impactSelectedEvent.emit({
                impactName,
                impactType,
            });
        }
    }
}
