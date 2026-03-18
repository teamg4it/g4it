import { Component, EventEmitter, inject, input, Output } from "@angular/core";
import { TranslateService, TranslatePipe } from "@ngx-translate/core";
import { GraphDescriptionContent } from "src/app/core/interfaces/digital-service.interfaces";
import { Button } from "primeng/button";
import { RouterLink } from "@angular/router";

@Component({
    selector: "app-graph-description",
    templateUrl: "./graph-description.component.html",
    styleUrl: "./graph-description.component.scss",
    standalone: true,
    imports: [
        Button,
        RouterLink,
        TranslatePipe,
    ],
})
export class GraphDescriptionComponent {
    private readonly translate = inject(TranslateService);
    contentVisible = true;
    contentText = input<GraphDescriptionContent>();
    ecoMindRecomendation = input<string>("");
    chartType = input<string>("");
    barChartChild = input<boolean>(false);
    selectedParam = input<string>("");
    versionIdNames = input<any[]>([]);
    tableBody = input<any>([]);
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
