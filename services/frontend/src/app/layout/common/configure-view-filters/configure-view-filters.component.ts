import { Component, EventEmitter, OnInit, Output } from "@angular/core";
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
    unitType = "raw";
    formGroup!: FormGroup;
    @Output() sidebarVisibleChange: EventEmitter<boolean> = new EventEmitter<boolean>();
    ngOnInit() {
        this.formGroup = new FormGroup({
            dataConsistencyCheckbox: new FormControl<boolean>(false),
        });
    }
    apply() {}

    closeSidebar() {
        this.sidebarVisibleChange.emit(false);
    }
}
