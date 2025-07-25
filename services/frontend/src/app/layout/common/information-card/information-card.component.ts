/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, input } from "@angular/core";
import { DomSanitizer, SafeHtml } from "@angular/platform-browser";

@Component({
    selector: "app-information-card",
    templateUrl: "./information-card.component.html",
})
export class InformationCardComponent {
    title = input<string>();
    content = input<string>();

    safeContent = computed(() => this.renderHTML(this.content()));
    safeTitle = computed(() => this.renderHTML(this.title()));

    constructor(private readonly sanitizer: DomSanitizer) {}

    renderHTML(html: string | undefined): SafeHtml {
        if (html) {
            return this.sanitizer.bypassSecurityTrustHtml(html);
        }
        return "";
    }
}
