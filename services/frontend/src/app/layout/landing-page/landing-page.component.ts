/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CommonModule } from "@angular/common";
import { Component, ElementRef, inject, ViewChild } from "@angular/core";
import { RouterModule } from "@angular/router";
import { SharedModule } from "src/app/core/shared/shared.module";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { LeftSidebarComponent } from "../header/header-siderbar/left-sidebar/left-sidebar.component";
import { TopHeaderComponent } from "../header/header-siderbar/top-header/top-header.component";

@Component({
    selector: "app-landing-page",
    templateUrl: "./landing-page.component.html",
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        SharedModule,
        TopHeaderComponent,
        LeftSidebarComponent,
    ],
})
export class LandingPageComponent {
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
