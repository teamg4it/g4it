import { Component, DestroyRef, inject } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Title } from "@angular/platform-browser";
import { TranslateService } from "@ngx-translate/core";
import { sortByProperty } from "sort-by-property";
import { BusinessHours } from "src/app/core/interfaces/business-hours.interface";
import { Organization, Subscriber } from "src/app/core/interfaces/user.interfaces";
import { Version, VersionRest } from "src/app/core/interfaces/version.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { BusinessHoursService } from "src/app/core/service/data/business-hours.service";
import { VersionDataService } from "src/app/core/service/data/version-data.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { Constants } from "src/constants";
@Component({
    selector: "app-useful-information",
    standalone: true,
    imports: [SharedModule],
    templateUrl: "./useful-information.component.html",
    styleUrl: "./useful-information.component.scss",
})
export class UsefulInformationComponent {
    private readonly translate = inject(TranslateService);
    private readonly businessHoursService = inject(BusinessHoursService);
    private readonly destroyRef = inject(DestroyRef);

    private readonly versionDataService = inject(VersionDataService);
    private readonly userService = inject(UserService);
    private readonly title = inject(Title);
    currentSubscriber: Subscriber = {} as Subscriber;
    selectedOrganization: Organization = {} as Organization;
    versions: Version[] = [];
    businessHoursData: BusinessHours[] = [];
    selectedLanguage: string = "en";
    constructor(private readonly titleService: Title) {}
    ngOnInit() {
        this.translate.get("common.useful-info").subscribe((translatedTitle: string) => {
            this.titleService.setTitle(translatedTitle);
        });
        this.selectedLanguage = this.translate.currentLang;

        this.businessHoursService
            .getBusinessHours()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((businessHours: BusinessHours[]) => {
                this.businessHoursData = businessHours;
            });

        this.versionDataService
            .getVersion()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((version: VersionRest) => {
                this.versions.push({ name: "g4it", version: version["g4it"] });
                const externalVersions = [];
                for (const key in version) {
                    if (key !== "g4it") {
                        externalVersions.push({ name: key, version: version[key] });
                    }
                }
                externalVersions.sort(sortByProperty("name", "asc"));
                this.versions.push(...externalVersions);
            });

        this.userService.currentSubscriber$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((subscriber) => (this.currentSubscriber = subscriber));

        this.userService.currentOrganization$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((organization: Organization) => {
                this.selectedOrganization = organization;
            });
    }

    composeEmail() {
        let subject = `[${this.currentSubscriber.name}/${this.selectedOrganization?.id}] ${Constants.SUBJECT_MAIL}`;
        let email = `mailto:${Constants.RECIPIENT_MAIL}?subject=${subject}`;
        window.location.href = email;
    }
}
