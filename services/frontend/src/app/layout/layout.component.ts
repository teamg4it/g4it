/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, ElementRef, inject, ViewChild } from "@angular/core";
import { GlobalStoreService } from "../core/store/global.store";
import { TopHeaderComponent } from "./header/header-siderbar/top-header/top-header.component";

@Component({
    selector: "app-layout",
    templateUrl: "./layout.component.html",
})
export class LayoutComponent {
    public globalStore = inject(GlobalStoreService);

    @ViewChild("mainContent") mainContent!: ElementRef;
    @ViewChild(TopHeaderComponent) topHeader!: TopHeaderComponent;

    focusFirstElement() {
        const mainElement = this.mainContent.nativeElement;
        const focusableElements = mainElement.querySelectorAll(
            'a, button, input, textarea, select, [tabindex]:not([tabindex="-1"])',
        );

        if (focusableElements.length > 0) {
            (focusableElements[0] as HTMLElement).focus();
        }
    }
}
