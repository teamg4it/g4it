import { Component, inject, input } from "@angular/core";
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

    toggleContentVisibility() {
        this.contentVisible = !this.contentVisible;
    }
}
