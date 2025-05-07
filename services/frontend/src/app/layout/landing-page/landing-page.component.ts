/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CommonModule } from "@angular/common";
import { Component, DestroyRef, ElementRef, inject, ViewChild } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { RouterModule } from "@angular/router";
import { WorkspaceService } from "src/app/core/service/business/workspace.service";
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
    private readonly destroyRef = inject(DestroyRef);
    private workspaceService = inject(WorkspaceService);
    protected spaceSidebarVisible: boolean = false;

    @ViewChild("mainContent") mainContent!: ElementRef;

    ngOnInit() {
        this.workspaceService
            .getIsOpen()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((isOpen: boolean) => {
                this.spaceSidebarVisible = isOpen;
            });
    }

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
