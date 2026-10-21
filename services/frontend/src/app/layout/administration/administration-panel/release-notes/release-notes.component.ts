import { Component, type WritableSignal } from "@angular/core";
import { signal } from "@angular/core";
import { CommonEditorComponent } from "../../../common/common-editor/common-editor.component"
import { TranslatePipe } from "@ngx-translate/core";
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
export class ReleaseNotesComponent {
    initialNotes!: WritableSignal<string>
    form!: FormGroup<{
        releaseNotes: FormControl<string | null>
    }>

    constructor(
        public administrationService: AdministrationService
    ) { }

    ngOnInit(): void {
        this.initialNotes = signal<string>("");
        this.form = new FormGroup({
            releaseNotes: new FormControl<string>(this.initialNotes(), [Validators.required])
        })

        this.administrationService.getLatestReleaseNotes().subscribe(({ content: notes }) => {
            console.log("notes", notes)
            if (!!notes) {
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
        if (this.form.valid && this.form.value.releaseNotes && this.form.value.releaseNotes !== this.initialNotes()) {
            this.administrationService.saveReleaseNotes({ content: this.form.value.releaseNotes }).subscribe(() => {
                this.initialNotes.set(this.form.value.releaseNotes as string);
                this.form.markAsPristine();
            });
        }
    }

    undoChanges() {
        this.form.reset({
            releaseNotes: this.initialNotes()
        })
    }
}
