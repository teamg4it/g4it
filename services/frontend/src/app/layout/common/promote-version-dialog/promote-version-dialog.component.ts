import { Component, EventEmitter, inject, input, Input, Output } from "@angular/core";
import { DigitalServiceVersionDataService } from "src/app/core/service/data/digital-service-version-data-service";

@Component({
    selector: "app-promote-version-dialog",
    templateUrl: "./promote-version-dialog.component.html",
})
export class PromoteVersionDialogComponent {
    private readonly digitalServiceVersionDataService = inject(
        DigitalServiceVersionDataService,
    );
    dsvId = input("");
    @Input() displayPopup = false;
    @Output() outClose = new EventEmitter<void>();
    @Output() promotedEvent = new EventEmitter<boolean>();

    closePopup() {
        this.outClose.emit();
    }

    promoteVersion(): void {
        if (!this.dsvId()) {
            return;
        }
        this.digitalServiceVersionDataService
            .promoteVersion(this.dsvId())
            .subscribe((res) => {
                if (res.isPromoted) {
                    this.promotedEvent.emit(true);
                }
                this.closePopup();
            });
    }
}
