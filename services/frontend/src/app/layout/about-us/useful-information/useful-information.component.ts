import { Component, DestroyRef, inject, OnInit } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Title } from "@angular/platform-browser";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { BusinessHours } from "src/app/core/interfaces/business-hours.interface";
import { Organization, Workspace } from "src/app/core/interfaces/user.interfaces";
import { Version } from "src/app/core/interfaces/version.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { BusinessHoursService } from "src/app/core/service/data/business-hours.service";
import { ShareUsefulInformationDataService } from "src/app/core/service/data/share-useful-information-service";
import { VersionDataService } from "src/app/core/service/data/version-data.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { environment } from "src/environments/environment";
@Component({
    selector: "app-useful-information",
    standalone: true,
    imports: [SharedModule],
    templateUrl: "./useful-information.component.html",
    styleUrls: ["./useful-information.component.scss"],
})
export class UsefulInformationComponent implements OnInit {
    private readonly translate = inject(TranslateService);
    private readonly businessHoursService = inject(BusinessHoursService);
    private readonly destroyRef = inject(DestroyRef);

    private readonly versionDataService = inject(VersionDataService);
    protected readonly userService = inject(UserService);
    private readonly title = inject(Title);
    private readonly digitalServiceStore = inject(DigitalServiceStoreService);
    private readonly shareUsefulInformationDataService = inject(
        ShareUsefulInformationDataService,
    );
    private readonly router = inject(Router);
    currentOrganization: Organization = {} as Organization;
    selectedWorkspace: Workspace = {} as Workspace;
    versions: Version[] = [];
    externalVersions: Version[] = [];
    businessHoursData: BusinessHours[] = [];
    selectedLanguage: string = "en";
    isEcoMindModuleEnabled: boolean = environment.isEcomindEnabled;
    repoUrls: { [key: string]: string } = {};
    constructor(private readonly titleService: Title) {}
    ngOnInit() {
        this.translate.get("common.useful-info").subscribe((translatedTitle: string) => {
            this.titleService.setTitle(translatedTitle);
        });
        this.selectedLanguage = this.translate.currentLang;

        const isShared = this.digitalServiceStore.isSharedDS();
        console.log(isShared);

        const [_, _1, sharedToken] = this.router.url.split("/");

        (isShared
            ? this.shareUsefulInformationDataService.getBusinessHours(sharedToken)
            : this.businessHoursService.getBusinessHours()
        )
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((businessHours: BusinessHours[]) => {
                this.businessHoursData = businessHours;
            });

        (isShared
            ? this.shareUsefulInformationDataService.getVersion(sharedToken)
            : this.versionDataService.getVersion()
        )
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((version: VersionRest) => {
                this.versions.push({ name: "g4it", version: version["g4it"] });
                this.repoUrls = {
                    g4it: `https://github.com/teamg4it/g4it/releases/tag/${version["g4it"]}`,
                    ecomindai: `https://github.com/sustain4ai/ecomindai/releases/tag/${version["ecomindai"]}`,
                    boaviztapi: `https://github.com/Boavizta/boaviztapi/releases/tag/v${version["boaviztapi"]}`,
                    numecoeval: `https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval/-/tree/${version["numecoeval"]}`,
                };
                for (const key in version) {
                    if (key !== "g4it") {
                        this.externalVersions.push({ name: key, version: version[key] });
                    }
                }
                this.externalVersions.sort(sortByProperty("name", "asc"));
            });

        this.userService.currentOrganization$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((organization) => (this.currentOrganization = organization));

        this.userService.currentWorkspace$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((workspace: Workspace) => {
                this.selectedWorkspace = workspace;
            });
    }

    composeEmail() {
        globalThis.location.href = this.userService.composeEmail(
            this.currentOrganization,
            this.selectedWorkspace,
        );
    }
}
