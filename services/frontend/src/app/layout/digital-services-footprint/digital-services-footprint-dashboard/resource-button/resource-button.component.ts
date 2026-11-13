import { Component, EventEmitter, Input, Output } from "@angular/core";
import { TranslateModule } from "@ngx-translate/core";
import { ButtonModule } from "primeng/button";

export type ResourceCategories =
    | "devices"
    | "networks"
    | "private-infrastructures"
    | "external-services";

@Component({
    selector: "app-resource-button",
    templateUrl: "./resource-button.component.html",
    standalone: true,
    imports: [TranslateModule, ButtonModule],
})
export class ResourceButtonComponent {
    @Input() category: ResourceCategories = "devices";
    @Input() translationKey = "";
    @Input() isSelected = false;

    @Output() selectedCategoryChange = new EventEmitter<ResourceCategories>();

    selectCategory(): void {
        this.selectedCategoryChange.emit(this.category);
    }
}
