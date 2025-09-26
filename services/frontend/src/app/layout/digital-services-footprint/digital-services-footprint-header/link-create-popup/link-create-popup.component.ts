import {
    Component,
    computed,
    EventEmitter,
    inject,
    input,
    Input,
    Output,
} from "@angular/core";
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
    @Output() outExtendDate = new EventEmitter<void>();
    isLinkCopied = false;
    extendLinkDisabled = computed(() => {
        if (this.expiryDate()) {
            const todayUtc = new Date();
            todayUtc.setUTCHours(23, 59, 0, 0);
            const diffInDays =
                (this.expiryDate()!.getTime() - todayUtc.getTime()) / 86400000; // 1000*60*60*24
            return diffInDays > 59;
        } else {
            return true;
        }
    });

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

    extendDate(): void {
        this.outExtendDate.emit();
    }
}
