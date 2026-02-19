import { Component, EventEmitter, input, OnInit, Output } from "@angular/core";
import { FormControl, FormGroup } from "@angular/forms";
import { SharedModule } from "src/app/core/shared/shared.module";

@Component({
    selector: "app-configure-view-filters",
    standalone: true,
    imports: [SharedModule],
    templateUrl: "./configure-view-filters.component.html",
    styleUrl: "./configure-view-filters.component.scss",
})
export class ConfigureViewFiltersComponent implements OnInit {
    enableConsistency = false;
    unitType = "Raw";
    formGroup!: FormGroup;
    enableDataInconsistency = input(false);
    selectedUnit = input(this.unitType);
    @Output() sidebarVisibleChange: EventEmitter<boolean> = new EventEmitter<boolean>();
    @Output() filtersApplied: EventEmitter<{
        enableConsistency: boolean;
        unitType: string;
    }> = new EventEmitter<{ enableConsistency: boolean; unitType: string }>();
    ngOnInit() {
        this.formGroup = new FormGroup({
            dataConsistencyCheckbox: new FormControl<boolean>(
                this.enableDataInconsistency() || false,
            ),
            unitType: new FormControl<string>(this.selectedUnit() || this.unitType),
        });
    }
    apply() {
        this.enableConsistency = this.formGroup.get("dataConsistencyCheckbox")?.value;
        this.unitType = this.formGroup.get("unitType")?.value;

        this.filtersApplied.emit({
            enableConsistency: this.enableConsistency,
            unitType: this.unitType,
        });

        this.sidebarVisibleChange.emit(false);
    }

    closeSidebar() {
        this.sidebarVisibleChange.emit(false);
    }
}
