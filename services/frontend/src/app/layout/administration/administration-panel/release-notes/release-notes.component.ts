import { Component, type WritableSignal, signal, type OnInit } from "@angular/core";
import { CommonEditorComponent } from "../../../common/common-editor/common-editor.component";
import { TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { Button } from "primeng/button";
import {
    FormControl,
    FormGroup,
    Validators,
} from "@angular/forms";
import { AdministrationService } from "src/app/core/service/business/administration.service";


@Component({
    selector: "app-release-notes",
    imports: [CommonEditorComponent, TranslatePipe, Button],
    templateUrl: "./release-notes.component.html",
    styleUrl: "./release-notes.component.scss",
})
export class ReleaseNotesComponent implements OnInit {
    initialNotes!: WritableSignal<string>
    form!: FormGroup<{
        releaseNotes: FormControl<string | null>
    }>

    constructor(
        public administrationService: AdministrationService,
        private readonly messageService: MessageService,
        private readonly translate: TranslateService,
    ) { }

    ngOnInit(): void {
        this.initialNotes = signal<string>("");
        this.form = new FormGroup({
            releaseNotes: new FormControl<string>(this.initialNotes(), [Validators.required])
        })

        this.administrationService.getLatestReleaseNotes().subscribe(({ content: notes }) => {
            if (notes) {
                this.initialNotes.set(notes);
                this.form.patchValue({
                    releaseNotes: notes,
                });
            }
        });
    }

    onEditorContentChange(content: string) {
        this.form.patchValue({
            releaseNotes: content,
        });
        this.form.markAsDirty();
    }

    saveReleaseNotes() {
        const content = this.form.value.releaseNotes;
        if (!this.form.valid || !content) {
            this.messageService.add({
                severity: "warn",
                summary: this.translate.instant("common.attention"),
                detail: this.translate.instant("administration.release-notes.save-invalid"),
            });
            return;
        }

        if (content === this.initialNotes()) {
            this.messageService.add({
                severity: "info",
                summary: this.translate.instant("common.attention"),
                detail: this.translate.instant("administration.release-notes.save-unchanged"),
            });
            return;
        }

        this.administrationService.saveReleaseNotes({ content }).subscribe({
            next: () => {
                this.initialNotes.set(content);
                this.form.markAsPristine();
                this.messageService.add({
                    severity: "success",
                    summary: this.translate.instant("common.success"),
                    detail: this.translate.instant("administration.release-notes.save-success"),
                });
            },
            error: () => {
                this.messageService.add({
                    severity: "error",
                    summary: this.translate.instant("common.error"),
                    detail: this.translate.instant("administration.release-notes.save-error"),
                });
            },
        });
    }

    undoChanges() {
        this.form.reset({
            releaseNotes: this.initialNotes()
        })
    }
}
