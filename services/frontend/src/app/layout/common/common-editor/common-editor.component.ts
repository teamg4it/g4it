import {
    Component,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    SecurityContext,
    SimpleChanges,
} from "@angular/core";
import { DomSanitizer } from "@angular/platform-browser";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";

@Component({
    selector: "app-common-editor",
    templateUrl: "./common-editor.component.html",
    providers: [ConfirmationService, MessageService],
})
export class CommonEditorComponent implements OnChanges {
    @Input() styleClass = "";
    @Input() maxContentLength = 20000;
    @Input() content: string | undefined = undefined;
    @Input() showButtons: boolean | null = false;
    @Input() title = "Note";
    escape: boolean = false;

    @Output() close: EventEmitter<any> = new EventEmitter();
    @Output() delete: EventEmitter<any> = new EventEmitter();
    @Output() saveValue: EventEmitter<string> = new EventEmitter();

    editorTextValue = "";
    editorTextValueUnmodified = "";

    constructor(
        private readonly sanitizer: DomSanitizer,
        private readonly translate: TranslateService,
        private readonly messageService: MessageService,
        private readonly confirmationService: ConfirmationService,
    ) {}

    ngOnChanges(changes: SimpleChanges) {
        let change = changes["content"];

        if (change?.currentValue === null) {
            this.editorTextValue = "";
            this.editorTextValueUnmodified = "";
        }

        if (change?.currentValue) {
            this.editorTextValue = change?.currentValue;
            this.editorTextValueUnmodified = change?.currentValue;
        }
    }

    removeStylesFromText(htmlText: string) {
        let html = new DOMParser().parseFromString(htmlText, "text/html");
        return html.body.textContent ?? "";
    }

    saveContent() {
        if (
            !this.editorTextValue ||
            this.removeStylesFromText(this.editorTextValue)?.trim() === ""
        ) {
            this.messageService.add({
                severity: "error",
                summary: this.translate.instant("common.note.no-content"),
            });
            return;
        }

        if (this.editorTextValue.length > this.maxContentLength) {
            this.messageService.add({
                severity: "error",
                summary: this.translate.instant("common.note.content-length-exceeded"),
            });
            return;
        }

        const sanitizedData: any = this.sanitizer.sanitize(
            SecurityContext.HTML,
            this.editorTextValue,
        );

        if (sanitizedData) {
            this.saveValue.emit(sanitizedData);
        }
    }

    cancelContent(event: any) {
        this.confirmationService.confirm({
            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: this.translate.instant("common.cancelConfirmMsgForEditor"),
            accept: () => {
                this.editorTextValue = this.editorTextValueUnmodified;
                this.close.emit();
            },
            reject: () => {},
        });
    }

    deleteContent(event: any) {
        this.confirmationService.confirm({
            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: this.translate.instant("common.confirmMessageForEditor"),
            accept: () => {
                this.editorTextValue = "";
                this.delete.emit();
            },
            reject: () => {},
        });
    }
}
