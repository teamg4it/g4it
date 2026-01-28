import { CommonModule } from "@angular/common";
import { Component, ElementRef, ViewChild } from "@angular/core";
import { RouterModule, RouterOutlet } from "@angular/router";
import { SharedModule } from "src/app/core/shared/shared.module";
import { LeftSidebarComponent } from "../../header/header-siderbar/left-sidebar/left-sidebar.component";
import { TopHeaderComponent } from "../../header/header-siderbar/top-header/top-header.component";

@Component({
    selector: "app-share-landing-page",
    standalone: true,
    imports: [
        RouterOutlet,
        CommonModule,
        RouterModule,
        SharedModule,
        TopHeaderComponent,
        LeftSidebarComponent,
        SharedModule,
    ],
    templateUrl: "./share-landing-page.component.html",
})
export class ShareLandingPageComponent {
    @ViewChild("mainContent") mainContent!: ElementRef;

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
