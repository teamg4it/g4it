/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CommonModule } from "@angular/common";
import { Component, computed, DestroyRef, inject, OnInit, signal } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { NavigationEnd, Router, RouterModule } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { KeycloakService } from "keycloak-angular";
import { Subscriber } from "src/app/core/interfaces/administration.interfaces";
import {
    Organization,
    OrganizationData,
    User,
    UserInfo,
} from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { generateColor } from "src/app/core/utils/color";
import { Constants } from "src/constants";

@Component({
    standalone: true,
    selector: "app-left-sidebar",
    templateUrl: "./left-sidebar.component.html",
    styleUrls: ["./left-sidebar.component.scss"],
    imports: [CommonModule, TranslateModule, RouterModule],
})
export class LeftSidebarComponent implements OnInit {
    private readonly destroyRef = inject(DestroyRef);
    private readonly router = inject(Router);
    public userService = inject(UserService);
    private readonly translate = inject(TranslateService);
    private readonly keycloak = inject(KeycloakService);
    constants = Constants;
    homeTitle = computed(() =>
        this.getTitle("welcome-page.title", this.constants.WELCOME_PAGE),
    );
    digitalServicesTitle = computed(() =>
        this.getTitle("digital-services.title", "digital-services"),
    );
    inventoriesTitle = computed(() => this.getTitle("inventories.title", "inventories"));
    administrationTitle = computed(() =>
        this.getTitle("common.administration", "administration"),
    );
    digitalServicesAriaCurrent = computed(() => this.getAriaCurrent("digital-services"));
    inventoriesAriaCurrent = computed(() => this.getAriaCurrent("inventories"));
    administrationAriaCurrent = computed(() => this.getAriaCurrent("administration"));
    public globalStore = inject(GlobalStoreService);
    selectedPage = signal("");

    selectedLanguage: string = "en";

    organizations: OrganizationData[] = [];

    selectedOrganization: Organization = {} as Organization;
    selectedOrganizationData: OrganizationData | undefined = undefined;
    selectedPath = "";

    currentSubscriber: Subscriber = {} as Subscriber;

    isAdminOnSubscriberOrOrganization = false;
    userDetails!: UserInfo;
    isZoomedIn = computed(() => this.globalStore.zoomLevel() >= 150);

    ngOnInit() {
        this.selectedLanguage = this.translate.currentLang;

        this.setSelectedPage();
        this.router.events.subscribe((event) => {
            if (event instanceof NavigationEnd) {
                this.setSelectedPage();
            }
        });

        this.userService.user$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((user: User) => {
                this.userDetails = {
                    firstName: user.firstName,
                    lastName: user.lastName,
                    email: user.email,
                };
                this.organizations = [];
                user.subscribers.forEach((subscriber: any) => {
                    subscriber.organizations.forEach((organization: any) => {
                        this.organizations.push({
                            color: generateColor(organization.name + subscriber.name),
                            id: organization.id,
                            name: organization.name,
                            organization,
                            subscriber: subscriber,
                        });
                    });
                });
                this.isAdminOnSubscriberOrOrganization =
                    this.userService.hasAnyAdminRole(user);
            });

        this.userService.currentSubscriber$.subscribe(
            (subscriber: any) => (this.currentSubscriber = subscriber),
        );

        this.userService.currentOrganization$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((organization: any) => {
                this.selectedOrganization = organization;
                this.selectedOrganizationData = {
                    color: generateColor(organization.name + this.currentSubscriber.name),
                    id: organization.id,
                    name: organization.name,
                    organization,
                    subscriber: this.currentSubscriber as any,
                };
                this.selectedPath = `/subscribers/${this.currentSubscriber.name}/organizations/${this.selectedOrganization?.id}`;
            });
    }

    setSelectedPage() {
        this.selectedPage.set(this.userService.getSelectedPage());
    }

    getTitle(name: string, page: string): any {
        return this.selectedPage() === page
            ? this.translate.instant(name, {
                  OrganizationName: this.selectedOrganization.name,
              }) +
                  " - " +
                  this.translate.instant("common.active-page")
            : this.translate.instant(name, {
                  OrganizationName: this.selectedOrganization.name,
              });
    }

    getAriaCurrent(page: string): any {
        return this.selectedPage() === page ? "page" : null;
    }
}
