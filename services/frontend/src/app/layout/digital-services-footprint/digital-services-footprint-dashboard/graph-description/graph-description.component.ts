import { Component } from "@angular/core";

@Component({
    selector: "app-graph-description",
    templateUrl: "./graph-description.component.html",
    styleUrl: "./graph-description.component.scss",
})
export class GraphDescriptionComponent {
    contentVisible = false;

    toggleContentVisibility() {
        this.contentVisible = !this.contentVisible;
    }
}
