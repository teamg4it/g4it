import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from "@angular/forms";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { DialogModule } from "primeng/dialog";
import { PrimeTemplate } from "primeng/api";
import { CheckboxModule } from "primeng/checkbox";
import { Button } from "primeng/button";
import { TranslatePipe } from "@ngx-translate/core";

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
