import { Component, DestroyRef, inject } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { Title } from "@angular/platform-browser";
import { TranslateService } from "@ngx-translate/core";
import { Organization, Subscriber } from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { Constants } from "src/constants";

@Component({
    selector: "app-declarations",
    standalone: true,
    imports: [SharedModule],
    templateUrl: "./declarations.component.html",
    styleUrls: ["./declarations.component.scss"],
})
export class DeclarationsComponent {
    private readonly translate = inject(TranslateService);
    private readonly titleService = inject(Title);
    private readonly userService = inject(UserService);
    private readonly destroyRef = inject(DestroyRef);
    currentLang: string = this.translate.currentLang;
    currentSubscriber: Subscriber = {} as Subscriber;
    selectedOrganization: Organization = {} as Organization;
    pdfSize = 0;
    ngOnInit() {
        this.translate.get("declarations.title").subscribe((translatedTitle: string) => {
            this.titleService.setTitle(translatedTitle);
        });
        this.currentLang = this.translate.currentLang;
        this.pdfSize = this.currentLang === "en" ? 139 : 253;

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
