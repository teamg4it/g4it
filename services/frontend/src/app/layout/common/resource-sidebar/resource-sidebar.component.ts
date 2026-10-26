import { CommonModule } from "@angular/common";
import { Component, EventEmitter, Input, Output } from "@angular/core";
import { TranslateModule } from "@ngx-translate/core";
import { ButtonModule } from "primeng/button";
import {
    ResourceButtonComponent,
    ResourceCategories,
} from "../../digital-services-footprint/digital-services-footprint-dashboard/resource-button/resource-button.component";

@Component({
    selector: "app-resource-sidebar",
    templateUrl: "./resource-sidebar.component.html",
    standalone: true,
    imports: [TranslateModule, CommonModule, ButtonModule, ResourceButtonComponent],
})
export class ResourceSidebarComponent {
    @Input() selectedCategory: ResourceCategories = "devices";
    @Input() isCollapsed: boolean = false;

    @Output() selectedCategoryChange = new EventEmitter<ResourceCategories>();
    @Output() isCollapsedChange = new EventEmitter<boolean>();

    readonly categories: {
        id: ResourceCategories;
        translationKey: string;
    }[] = [
        {
            id: "devices",
            translationKey: "digital-services-resources.categories.devices",
        },
        {
            id: "networks",
            translationKey: "digital-services-resources.categories.networks",
        },
        {
            id: "private-infrastructures",
            translationKey:
                "digital-services-resources.categories.private-infrastructures",
        },
        {
            id: "external-services",
            translationKey: "digital-services-resources.categories.external-services",
        },
    ];

    selectCategory(category: ResourceCategories): void {
        this.selectedCategoryChange.emit(category);
    }

    onCollapseToggle(collapsed: boolean): void {
        this.isCollapsed = collapsed;
        this.isCollapsedChange.emit(collapsed);
    }
}
