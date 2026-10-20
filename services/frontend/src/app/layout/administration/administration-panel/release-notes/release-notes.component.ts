import { Component } from "@angular/core";
import { CommonEditorComponent } from "../../../common/common-editor/common-editor.component"
import { TranslatePipe } from "@ngx-translate/core";
import { Button } from "primeng/button";
import {
    FormControl,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from "@angular/forms";


@Component({
    selector: "app-release-notes",
    imports: [CommonEditorComponent, TranslatePipe, Button],
    templateUrl: "./release-notes.component.html",
    styleUrl: "./release-notes.component.scss",
})
export class ReleaseNotesComponent {
    initialNotes: string | undefined
    form!: FormGroup

    ngOnInit(): void {
        this.initialNotes = "(old notes)"
        this.form = new FormGroup({
            releaseNotes: new FormControl<string>(this.initialNotes, [Validators.required])
        })
    }

    onEditorContentChange(content: string) {
        this.form.patchValue({
            releaseNotes: content,
        });
        this.form.markAsDirty();
    }

    saveReleaseNotes() {
        console.log("saved!", this.form)
    }

    undoChanges() {
        this.form.reset({
            releaseNotes: this.initialNotes
        })
    }
}
