import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { FormControl, FormGroup } from "@angular/forms";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";

@Component({
    selector: "app-set-view-popup",
    templateUrl: "./set-view-popup.component.html",
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
    resetForm(): void {
        this.formGroup.get("dataConsistencyCheckbox")?.setValue(false);
    }

    closePopup(): void {
        this.outClose.emit();
    }
}
