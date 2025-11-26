import { Component, inject, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { DigitalServiceVersionResponse } from "src/app/core/interfaces/digital-service-version.interface";
import { DigitalServiceVersionDataService } from "src/app/core/service/data/digital-service-version-data-service";

@Component({
    selector: "app-digital-service-manage-version-table",
    templateUrl: "./digital-service-manage-version-table.component.html",
    styleUrl: "./digital-service-manage-version-table.component.scss",
})
export class DigitalServiceManageVersionTableComponent implements OnInit {
    private readonly digitalServiceVersionDataService = inject(
        DigitalServiceVersionDataService,
    );
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    versionData: DigitalServiceVersionResponse[] = [];

    ngOnInit() {
        const dsVersionUid =
            this.route.snapshot.paramMap.get("digitalServiceVersionId") ?? "";
        this.digitalServiceVersionDataService
            .getVersions(dsVersionUid)
            .subscribe((versions) => {
                this.versionData = versions;
            });
    }

    redirectToVersionDetails(version: string): void {
        this.router.navigate(
            ["digital-service-version", version, "footprint", "resources"],
            { relativeTo: this.route.parent?.parent },
        );
    }

    duplicateDigitalServiceVersion(version: string): void {
        // redirect to version
    }
}
