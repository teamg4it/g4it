/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import {
    AfterViewInit,
    Component,
    computed,
    DestroyRef,
    ElementRef,
    inject,
    NgZone,
    OnInit,
    ViewChild,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { RouterModule } from "@angular/router";
import { take } from "rxjs";
import { WorkspaceService } from "src/app/core/service/business/workspace.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { LeftSidebarComponent } from "../header/header-siderbar/left-sidebar/left-sidebar.component";
import { TopHeaderComponent } from "../header/header-siderbar/top-header/top-header.component";

@Component({
    selector: "app-landing-page",
    templateUrl: "./landing-page.component.html",
    standalone: true,
    imports: [RouterModule, SharedModule, TopHeaderComponent, LeftSidebarComponent],
})
export class LandingPageComponent implements OnInit, AfterViewInit {
    private readonly globalStore = inject(GlobalStoreService);
    private readonly ngZone = inject(NgZone);
    private readonly destroyRef = inject(DestroyRef);
    private readonly workspaceService = inject(WorkspaceService);
    protected spaceSidebarVisible: boolean = false;
    isZoom125 = computed(() => this.globalStore.zoomLevel() >= 125);
    isMobile = computed(() => this.globalStore.mobileView());

    @ViewChild("mainContent") mainContent!: ElementRef;
    @ViewChild("skipButton") skipButton!: ElementRef<HTMLButtonElement>;

    ngOnInit() {
        this.workspaceService
            .getIsOpen()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((isOpen: boolean) => {
                this.spaceSidebarVisible = isOpen;
            });
    }

    ngAfterViewInit(): void {
        this.ngZone.onStable.pipe(take(1)).subscribe(() => {
            requestAnimationFrame(() => {
                this.skipButton?.nativeElement?.focus();
            });
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
