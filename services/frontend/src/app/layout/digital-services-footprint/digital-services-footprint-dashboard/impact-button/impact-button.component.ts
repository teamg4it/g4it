/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CommonModule, DecimalPipe } from "@angular/common";
import {
    Component,
    EventEmitter,
    inject,
    input,
    Input,
    OnInit,
    Output,
} from "@angular/core";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { ButtonModule } from "primeng/button";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";

@Component({
    selector: "app-impact-button",
    templateUrl: "./impact-button.component.html",
    standalone: true,
    imports: [CommonModule, ButtonModule, TranslateModule],
    providers: [DecimalsPipe, IntegerPipe],
})
export class ImpactButtonComponent implements OnInit {
    protected readonly decimalPipe = inject(DecimalPipe);
    protected readonly integerPipe = inject(IntegerPipe);
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
