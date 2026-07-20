import { CommonModule } from "@angular/common";
import { Component, EventEmitter, Output, signal } from "@angular/core";
import { TranslateModule } from "@ngx-translate/core";
import { ButtonModule } from "primeng/button";

@Component({
    selector: "app-inverse-axis-button",
    imports: [CommonModule, ButtonModule, TranslateModule],
    templateUrl: "./inverse-axis-button.component.html",
})
export class InverseAxisButtonComponent {
    @Output() inverseAxisChange = new EventEmitter<boolean>();

    isInverted = signal<boolean>(false);

    toggleAxis(): void {
        this.isInverted.set(!this.isInverted());
        this.inverseAxisChange.emit(this.isInverted());
    }
}
