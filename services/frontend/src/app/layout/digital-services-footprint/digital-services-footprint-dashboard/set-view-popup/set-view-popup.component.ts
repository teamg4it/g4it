import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from "@angular/forms";
import { TranslatePipe } from "@ngx-translate/core";
import { PrimeTemplate } from "primeng/api";
import { Button } from "primeng/button";
import { CheckboxModule } from "primeng/checkbox";
import { DialogModule } from "primeng/dialog";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";

@Component({
    selector: "app-set-view-popup",
    templateUrl: "./set-view-popup.component.html",
    standalone: true,
    imports: [
        DialogModule,
        PrimeTemplate,
        FormsModule,
        ReactiveFormsModule,
        CheckboxModule,
        Button,
        TranslatePipe,
    ],
})
export class SetViewPopupComponent implements OnInit {
    @Input() displayPopup: boolean = false;
    @Input() digitalService!: DigitalService;
    @Output() outClose = new EventEmitter<void>();
    formGroup!: FormGroup;
    intitalDataState: boolean = false;
    ngOnInit() {
        this.intitalDataState = this.digitalService.enableDataInconsistency;
        this.formGroup = new FormGroup({
            dataConsistencyCheckbox: new FormControl<boolean>(
                this.digitalService.enableDataInconsistency || false,
            ),
        });
    }

    closePopup(): void {
        this.outClose.emit();
    }
}
