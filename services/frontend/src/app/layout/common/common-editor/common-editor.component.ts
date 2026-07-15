import {
    Component,
    EventEmitter,
    input,
    Input,
    OnChanges,
    Output,
    SecurityContext,
    SimpleChanges,
} from "@angular/core";
import { FormsModule } from "@angular/forms";
import { DomSanitizer } from "@angular/platform-browser";
import { TranslatePipe, TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { Button } from "primeng/button";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { EditorModule } from "primeng/editor";
import { ToastModule } from "primeng/toast";
import { AutofocusDirective } from "../../../core/directives/auto-focus.directive";

@Component({
    selector: "app-common-editor",
    templateUrl: "./common-editor.component.html",
    providers: [ConfirmationService, MessageService],
    standalone: true,
    imports: [
        AutofocusDirective,
        EditorModule,
        FormsModule,
        ToastModule,
        ConfirmPopupModule,
        Button,
        TranslatePipe,
    ],
})
export class CommonEditorComponent implements OnChanges {
    @Input() styleClass = "";
    @Input() maxContentLength = 20000;
    @Input() content: string | undefined = undefined;
    @Input() isWriteRole: boolean | null = false;
    showTitle = input(true);
    showButtons = input(true);
    @Input() title = "Note";
    escape: boolean = false;

    @Output() outClose: EventEmitter<any> = new EventEmitter();
    @Output() delete: EventEmitter<any> = new EventEmitter();
    @Output() saveValue: EventEmitter<string> = new EventEmitter();
    @Output() contentChange: EventEmitter<string> = new EventEmitter();

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

    onTextChange(value: string) {
        this.contentChange.emit(value);
    }

    removeStylesFromText(htmlText: string) {
        let html = new DOMParser().parseFromString(htmlText, "text/html");
        return html.body.textContent ?? "";
    }

    /**
     * Validates and returns sanitized content, or null if validation fails
     * Shows error messages via MessageService
     */
    validateAndGetSanitizedContent(): string | null {
        if (this.editorTextValue.length > this.maxContentLength) {
            this.messageService.add({
                severity: "error",
                summary: this.translate.instant("common.note.content-length-exceeded"),
            });
            return null;
        }

        const sanitizedData: any = this.sanitizer.sanitize(
            SecurityContext.HTML,
            this.editorTextValue,
        );

        return sanitizedData || null;
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
        const sanitizedData = this.validateAndGetSanitizedContent();
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
                this.outClose.emit(false);
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
                this.delete.emit(false);
            },
            reject: () => {},
        });
    }
}
