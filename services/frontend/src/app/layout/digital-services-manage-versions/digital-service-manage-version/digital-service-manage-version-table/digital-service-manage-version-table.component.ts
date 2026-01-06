import { Component, DestroyRef, inject, OnInit } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService } from "primeng/api";
import { finalize } from "rxjs";
import { DigitalServiceVersionResponse } from "src/app/core/interfaces/digital-service-version.interface";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServiceVersionDataService } from "src/app/core/service/data/digital-service-version-data-service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { GlobalStoreService } from "src/app/core/store/global.store";

@Component({
    selector: "app-digital-service-manage-version-table",
    templateUrl: "./digital-service-manage-version-table.component.html",
    providers: [ConfirmationService],
})
export class DigitalServiceManageVersionTableComponent implements OnInit {
    private readonly digitalServiceVersionDataService = inject(
        DigitalServiceVersionDataService,
    );
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly confirmationService = inject(ConfirmationService);
    private readonly translate = inject(TranslateService);
    private readonly global = inject(GlobalStoreService);
    private readonly digitalServicesDataService = inject(DigitalServicesDataService);
    private readonly destroyRef = inject(DestroyRef);
    public readonly userService = inject(UserService);
    versionData: DigitalServiceVersionResponse[] = [];
    dsVersionUid: string = "";
    selectedVersions: string[] = [];
    isPromoteVersionDialogVisible = false;
    promoteDsvId = "";

    ngOnInit() {
        this.dsVersionUid =
            this.route.snapshot.paramMap.get("digitalServiceVersionId") ?? "";
        this.getVersions();
    }

    getVersions(): void {
        this.digitalServiceVersionDataService
            .getVersions(
                this.versionData.length
                    ? this.versionData.find((v) => v.versionType === "active")
                          ?.digitalServiceVersionUid!
                    : this.dsVersionUid,
            )
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((versions) => {
                this.versionData = versions;
            });
    }

    redirectToVersionDetails(version: string): void {
        let [_, _1, _2, _3, _4, moduleType] = this.router.url.split("/");
        if (moduleType === "eco-mind-ai") {
            this.router.navigate(
                ["eco-mind-ai", version, "footprint", "ecomind-parameters"],
                { relativeTo: this.route.parent?.parent },
            );
            return;
        }
        this.router.navigate(
            ["digital-service-version", version, "footprint", "resources"],
            { relativeTo: this.route.parent?.parent },
        );
    }
    compareVersions(): void {
        if (this.selectedVersions.length === 2) {
            // Implement your comparison logic here
            this.router.navigate(["../compare-versions"], {
                relativeTo: this.route,
                queryParams: {
                    version1: this.selectedVersions[0],
                    version2: this.selectedVersions[1],
                },
            });
        }
    }

    onVersionSelect(version: DigitalServiceVersionResponse): void {
        const index = this.selectedVersions.findIndex(
            (v: string) => v === version.digitalServiceVersionUid,
        );

        if (index > -1) {
            // Version is already selected, remove it
            this.selectedVersions.splice(index, 1);
            version.selected = false;
        } else if (this.selectedVersions.length < 2) {
            // Add version if less than 2 are selected
            this.selectedVersions.push(version.digitalServiceVersionUid);
            version.selected = true;
        } else {
            // Prevent selection if already 2 selected
            version.selected = false;
        }
    }

    duplicateDigitalServiceVersion(dsvUid: string): void {
        this.digitalServiceVersionDataService
            .duplicateVersion(dsvUid)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((version) => {
                this.redirectToVersionDetails(version.uid);
            });
    }

    confirmDelete(event: Event, version: DigitalServiceVersionResponse) {
        this.confirmationService.confirm({
            closeOnEscape: true,

            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: `${this.translate.instant(
                "digital-services.version.popup.delete-question",
            )} "${version.versionName}" ?
                ${this.translate.instant("digital-services.popup.delete-text")}
                ${version.versionType === "archived" ? this.translate.instant("digital-services.version.popup.archived-text") : ""}`,

            icon: "pi pi-exclamation-triangle",

            accept: () => {
                this.global.setLoading(true);
                this.digitalServicesDataService
                    .deleteVersion(version.digitalServiceVersionUid)
                    .pipe(
                        takeUntilDestroyed(this.destroyRef),
                        finalize(() => {
                            this.global.setLoading(false);
                        }),
                    )
                    .subscribe(() => {
                        if (version.digitalServiceVersionUid === this.dsVersionUid) {
                            const activeVersion = this.versionData.find(
                                (v) => v.versionType === "active",
                            )?.digitalServiceVersionUid!;
                            this.router.navigate(
                                [
                                    "digital-service-version",
                                    activeVersion,
                                    "manage-versions",
                                ],
                                { relativeTo: this.route.parent?.parent },
                            );
                        }
                        this.getVersions();
                    });
            },
        });
    }
}
