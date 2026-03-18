/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CommonModule } from "@angular/common";
import { Component, EventEmitter, Input, Output } from "@angular/core";
import { TranslateModule } from "@ngx-translate/core";
import { ButtonModule } from "primeng/button";
import { Observable } from "rxjs";
import { ImpactButtonComponent } from "src/app/layout/digital-services-footprint/digital-services-footprint-dashboard/impact-button/impact-button.component";

export interface ImpactData {
    name: string;
    title: string;
    unite: string;
    peopleeq: number;
    raw: number;
}

@Component({
    selector: "app-impact-sidebar",
    templateUrl: "./impact-sidebar.component.html",
    imports: [CommonModule, ButtonModule, TranslateModule, ImpactButtonComponent]
})
export class ImpactSidebarComponent {
    @Input() isCollapsed: boolean = false;
    @Input() impacts!: ImpactData[] | (() => ImpactData[]);
    @Input() selectedCriteria!: string;
    @Input() selectedUnit!: string;
    @Input() entityName!: string;
    @Input() isAllowedWrite$!: Observable<boolean>;
    @Input() onlyOneCriteria: boolean = false;

    @Output() isCollapsedChange = new EventEmitter<boolean>();
    @Output() selectedCriteriaChange = new EventEmitter<string>();
    @Output() displayPopup = new EventEmitter<void>();

    get impactsList(): ImpactData[] {
        return typeof this.impacts === "function" ? this.impacts() : this.impacts;
    }

    onCollapseToggle(collapsed: boolean): void {
        this.isCollapsed = collapsed;
        this.isCollapsedChange.emit(collapsed);
    }

    onChartChange(criteria: string): void {
        this.selectedCriteriaChange.emit(criteria);
    }

    onDisplayPopup(): void {
        this.displayPopup.emit();
    }
}
