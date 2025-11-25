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
    textDescriptionImpacts = input<
        { text: string; impactName: string; impactNameVisible: string }[]
    >([]);

    @Output() impactSelectedEvent: EventEmitter<string> = new EventEmitter();

    toggleContentVisibility() {
        this.contentVisible = !this.contentVisible;
    }

    impactClick(event: string) {
        this.impactSelectedEvent.emit(event);
    }
}
