import { Component, EventEmitter, inject, input, Input, Output } from "@angular/core";
import { ClipboardService } from "ngx-clipboard";

@Component({
    selector: "app-link-create-popup",
    templateUrl: "./link-create-popup.component.html",
    styleUrls: ["./link-create-popup.component.scss"],
})
export class LinkCreatePopupComponent {
    private readonly clipboardService = inject(ClipboardService);
    @Input() displayPopup = false;
    sharedLink = input<string>("");
    expiryDate = input<Date | null>(null);
    @Output() outClose = new EventEmitter<void>();
    isLinkCopied = false;
    loading = false;

    closePopup(): void {
        this.outClose.emit();
    }

    moveCaretToStart(input: HTMLInputElement) {
        // Deselect auto selection
        input.setSelectionRange(0, 0);
        input.scrollLeft = 0;
    }

    copyUrl(): void {
        this.isLinkCopied = true;
        const url = this.sharedLink();
        this.clipboardService.copy(url);
    }
}
