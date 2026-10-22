import { Component, ElementRef, signal, ViewChild } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { TranslatePipe } from "@ngx-translate/core";
import { Button } from "primeng/button";
import { CheckboxModule } from 'primeng/checkbox';
import { ReleaseNotesService } from "src/app/core/service/business/release-notes.service";

@Component({
    selector: "app-release-notes",
    imports: [Button, TranslatePipe, CheckboxModule, FormsModule],
    templateUrl: "./release-notes.component.html",
    styleUrl: "./release-notes.component.scss",
})
export class ReleaseNotesComponent {
    @ViewChild("releaseNotesDialog")
    dialog!: ElementRef<HTMLDialogElement>;
    @ViewChild("releaseNotesSection")
    section!: ElementRef<HTMLElement>;

    checked = signal<boolean>(false)

    constructor(
        public releaseNotesService: ReleaseNotesService
    ) { }

    ngOnInit() {
        this.releaseNotesService.getLatestReleaseNotes().subscribe((notes) => {
            if (notes) {
                // setHTML is a new method to set the inner HTML in a XSS-safe way
                // see: https://developer.mozilla.org/en-US/docs/Web/API/Element/setHTML
                if ('setHTML' in this.section.nativeElement
                    && typeof this.section.nativeElement['setHTML'] === 'function'
                    && 'Sanitizer' in window
                    && typeof window['Sanitizer'] === 'function'
                ) {
                    const sanitizer = new (window as any).Sanitizer();
                    sanitizer.allowAttribute("class")
                    sanitizer.allowElement("br")
                    this.section.nativeElement.setHTML(notes.content, { sanitizer })
                } else {
                    console.log("Using innerHTML to set the release notes content")
                    // but not widely available so define a fallback
                    this.section.nativeElement.innerHTML = notes.content
                }

                this.dialog.nativeElement.showModal()
            }
        })
    }

    closeDialog() {
        this.dialog.nativeElement.close();

        if (this.checked()) {
            this.releaseNotesService.markAsSeen().subscribe(() => {
                this.checked.set(true)
            })
        }
    }
}
