/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    Component,
    EventEmitter,
    inject,
    Input,
    OnChanges,
    Output,
    SimpleChanges,
} from "@angular/core";
import { TranslateService, TranslatePipe } from "@ngx-translate/core";
import { NgClass, NgIf } from "@angular/common";
import { CardModule } from "primeng/card";
import { DecimalsPipe } from "../../../core/pipes/decimal.pipe";
import { IntegerPipe } from "../../../core/pipes/integer.pipe";

@Component({
    selector: "app-impact-aggregate-infos",
    templateUrl: "./impact-aggregate-infos.component.html",
    standalone: true,
    imports: [
        NgClass,
        NgIf,
        CardModule,
        DecimalsPipe,
        IntegerPipe,
        TranslatePipe,
    ],
})
export class ImpactAggregateInfosComponent implements OnChanges {
    private readonly translate = inject(TranslateService);

    @Input() displayValue = 0;
    @Input() criteria = "";
    @Input() unit = "";

    @Output() changeUnit = new EventEmitter<string>();
    criteriaLabel = "";

    icon = "";
    unitOfCriteria = "";

    ngOnChanges(changes: SimpleChanges) {
        this.icon = this.translate.instant(`criteria.${this.criteria}.icon`);
        this.unitOfCriteria = this.translate.instant(`criteria.${this.criteria}.unite`);
        this.criteriaLabel = this.translate.instant(`criteria.${this.criteria}.title`);
    }

    setUnit(unit: string) {
        this.changeUnit.emit(unit);
    }
}
