/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CommonModule } from "@angular/common";
import { Component, EventEmitter, input, Input, OnInit, Output } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { ButtonModule } from "primeng/button";
import { SharedModule } from "src/app/core/shared/shared.module";

@Component({
    selector: "app-impact-button",
    templateUrl: "./impact-button.component.html",
    standalone: true,
    imports: [CommonModule, ButtonModule, SharedModule],
})
export class ImpactButtonComponent implements OnInit {
    @Input() impact: string = "...";
    @Input() impactText: string = "Other";
    @Input() impactUnite: string = "";
    @Input() value: any;
    @Input() selectedCriteria: string = "";
    @Output() selectedCriteriaChange: EventEmitter<any> = new EventEmitter();
    @Input() selectedUnit: string = "";
    @Input() disabled = false;
    isCollapsed = input<boolean>(false);
    impactImage: string = "";
    selectedLang: string = "en";

    constructor(private readonly translate: TranslateService) {}

    ngOnInit(): void {
        if (this.impact === "...") {
            this.impactImage = "assets/images/icons/icon-hourglass.svg";
            this.impactUnite = "N/A";
        }
        this.selectedLang = this.translate.currentLang;
        this.impactImage = `assets/images/icons/icon-${this.translate.translations[this.selectedLang]["criteria"][this.impact].icon}.svg`;
    }

    changeCritere(critere: string) {
        this.selectedCriteriaChange.emit(critere);
    }
}
