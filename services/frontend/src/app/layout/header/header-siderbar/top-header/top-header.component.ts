/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CommonModule } from "@angular/common";
import {
    Component,
    computed,
    DestroyRef,
    ElementRef,
    HostListener,
    inject,
    OnInit,
    QueryList,
    signal,
    ViewChildren,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { FormsModule } from "@angular/forms";
import { NavigationEnd, Router, RouterModule } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { KeycloakService } from "keycloak-angular";
import { MenuItem } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { MenuModule } from "primeng/menu";
import { RadioButtonModule } from "primeng/radiobutton";
import {
    Organization,
    OrganizationData,
    Subscriber,
    User,
    UserInfo,
} from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { WorkspaceService } from "src/app/core/service/business/workspace.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { generateColor } from "src/app/core/utils/color";
import { Constants } from "src/constants";
import { environment } from "src/environments/environment";
@Component({
    standalone: true,
    selector: "app-top-header",
    templateUrl: "./top-header.component.html",
    styleUrls: ["./top-header.component.scss"],
    imports: [
        ButtonModule,
        MenuModule,
        CommonModule,
        RouterModule,
        RadioButtonModule,
        FormsModule,
        TranslateModule,
        SharedModule,
    ],
})
export class TopHeaderComponent implements OnInit {
    private readonly translate = inject(TranslateService);
    private readonly router = inject(Router);
    private keycloak = inject(KeycloakService);
    private userService = inject(UserService);
    private destroyRef = inject(DestroyRef);
    private globalStore = inject(GlobalStoreService);
    ecoDesignPercent = this.userService.ecoDesignPercent;
    maxLength = 30;
    ingredient: string = "english";
    selectedLanguage: string = "en";
    userDetails!: UserInfo;
    organizations: OrganizationData[] = [];
    initials = "";

    items: MenuItem[] | undefined;
    isAccountMenuVisible = false;
    isOrgMenuVisible = false;
    isAboutMenuOpen = false;
    modelOrganization!: number;
    selectedOrganization: Organization = {} as Organization;
    selectedOrganizationData: OrganizationData | undefined = undefined;
    currentSubscriber: Subscriber = {} as Subscriber;
    selectedPath = "";
    selectedPage = signal("");
    isZoomedIn = computed(() => this.globalStore.zoomLevel() >= 150);
    @ViewChildren("radioItem") radioItems!: QueryList<ElementRef>;
    languages = ["en", "fr"];

    constructor(private workspaceService: WorkspaceService) {}

    ngOnInit() {
        this.selectedLanguage = this.translate.currentLang;
        this.setSelectedPage();
        this.router.events.subscribe((event) => {
            if (event instanceof NavigationEnd) {
                this.setSelectedPage();
            }
        });
        this.userService.currentSubscriber$.subscribe(
            (subscriber) => (this.currentSubscriber = subscriber),
        );
        this.userService.user$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((user: User) => {
                this.userDetails = {
                    firstName: user.firstName,
                    lastName: user.lastName,
                    email: user.email,
                };
                this.organizations = [];
                user.subscribers.forEach((subscriber) => {
                    subscriber.organizations.forEach((organization) => {
                        this.organizations.push({
                            color: generateColor(organization.name + subscriber.name),
                            id: organization.id,
                            name: organization.name,
                            organization,
                            subscriber: subscriber,
                        });
                    });
                });
                this.userService.currentOrganization$
                    .pipe(takeUntilDestroyed(this.destroyRef))
                    .subscribe((organization: Organization) => {
                        this.selectedOrganization = organization;
                        this.modelOrganization = organization.id;
                        this.selectedOrganizationData = {
                            color: generateColor(
                                organization.name + this.currentSubscriber.name,
                            ),
                            id: organization.id,
                            name: organization.name,
                            organization,
                            subscriber: this.currentSubscriber,
                        };
                        this.selectedPath = `/subscribers/${this.currentSubscriber.name}/organizations/${this.selectedOrganization?.id}`;
                    });
                this.initials =
                    this.getCapitaleLetter(this.userDetails?.firstName) +
                    this.getCapitaleLetter(this.userDetails?.lastName);
            });
        this.items = [
            {
                label: undefined,
                link: "",
                command: () => {
                    this.router.navigate(["/useful-information"]);
                },
                items: [
                    {
                        label: "common.useful-info",
                        route: Constants.USEFUL_INFORMATION,
                        subHeading: "common.useful-info-desc",

                        command: () => {
                            this.router.navigate(["/useful-information"]);
                        },
                    },
                ],
            },
            {
                label: undefined,
                link: "",
                command: () => {
                    this.router.navigate(["/declarations"]);
                },
                items: [
                    {
                        label: "declarations.title",
                        route: Constants.USEFUL_INFORMATION,
                        subHeading: "declarations.accessibility-text",
                        ecoHeading: "declarations.ecodesign",
                        command: () => {
                            this.router.navigate(["/declarations"]);
                        },
                    },
                ],
            },
            {
                label: "common.help-center",
                items: [
                    {
                        label: "common.github-link",
                        link: "https://github.com/G4ITTeam/g4it",
                        outsideLink: true,
                        borderClass: "border-light-grey-color",
                        command: () => {
                            window.open(
                                "https://github.com/G4ITTeam/g4it",
                                "_blank",
                                "noopener",
                            );
                        },
                    },
                    {
                        label: "common.doc-link",
                        link: "https://saas-g4it.com/documentation/",
                        outsideLink: true,
                        borderClass: "border-light-grey-color",
                        command: () => {
                            window.open(
                                "https://saas-g4it.com/documentation/",
                                "_blank",
                                "noopener",
                            );
                        },
                    },
                ],
            },
        ];
    }

    handleKeydown(event: KeyboardEvent) {
        const currentIndex = this.organizations.findIndex(
            (org) => org.id === this.modelOrganization,
        );

        let nextIndex = currentIndex;

        if (event.key === "ArrowDown" || event.key === "ArrowRight") {
            nextIndex = (currentIndex + 1) % this.organizations.length;
        } else if (event.key === "ArrowUp" || event.key === "ArrowLeft") {
            nextIndex =
                (currentIndex - 1 + this.organizations.length) %
                this.organizations.length;
        } else if (event.key === "Enter" || event.key === " ") {
            this.selectCompany(this.organizations[currentIndex]);
            event.preventDefault();
            return;
        }

        if (nextIndex !== currentIndex) {
            this.modelOrganization = this.organizations[nextIndex].id;

            // 👇 Scroll to that element
            setTimeout(() => {
                const radioEl = this.radioItems.toArray()[nextIndex];
                if (radioEl) {
                    radioEl.nativeElement.scrollIntoView({
                        block: "nearest",
                        behavior: "smooth",
                    });
                }
            });

            event.preventDefault();
        }
    }

    setSelectedPage() {
        this.selectedPage.set(this.userService.getSelectedPage());
    }

    handleKeydownLanguage(event: KeyboardEvent): void {
        const currentIndex = this.languages.findIndex((l) => l === this.selectedLanguage);

        let nextIndex = currentIndex;

        if (event.key === "ArrowDown" || event.key === "ArrowRight") {
            nextIndex = (currentIndex + 1) % this.languages.length;
        } else if (event.key === "ArrowUp" || event.key === "ArrowLeft") {
            nextIndex =
                (currentIndex - 1 + this.languages.length) % this.languages.length;
        } else if (event.key === "Enter" || event.key === " ") {
            this.changeLanguage(this.languages[currentIndex]);
            event.preventDefault();
            return;
        }

        if (nextIndex !== currentIndex) {
            this.selectedLanguage = this.languages[nextIndex];

            event.preventDefault();
        }
    }

    getCapitaleLetter(str: string) {
        if (str === undefined) return "";
        return str.charAt(0).toLocaleUpperCase();
    }

    selectCompany(organization: OrganizationData) {
        this.userService.checkAndRedirect(
            organization.subscriber!,
            organization.organization as Organization,
            this.selectedPage(),
        );
        this.isOrgMenuVisible = false;
    }

    toggleOrgMenu() {
        this.isOrgMenuVisible = !this.isOrgMenuVisible;
        if (this.isOrgMenuVisible) {
            const elementToView = document.querySelector(
                `#org-${this.modelOrganization}`,
            );
            setTimeout(() => {
                elementToView?.scrollIntoView({ behavior: "smooth", block: "start" });
            }, 0);
        }
    }

    openWorkspaceSidebar() {
        this.workspaceService.setOpen(true);
    }

    changeLanguage(lang: string): void {
        this.translate.use(lang);
        localStorage.setItem("lang", lang);
        document.querySelector("html")!.setAttribute("lang", lang);
        this.router.navigate([], {
            skipLocationChange: true,
            queryParamsHandling: "merge",
        });
        window.location.reload();
    }

    async logout() {
        localStorage.removeItem("username");
        localStorage.removeItem("currentOrganization");
        localStorage.removeItem("currentSubscriber");
        if (environment.keycloak.enabled === "true") {
            await this.keycloak.logout();
        } else {
            console.error("keycloak is not enabled");
        }
    }

    @HostListener("document:click", ["$event"])
    handleGlobalClick(event: Event) {
        const accountContainer = document.querySelector(".account-menu");
        const accountBtnContainer = document.querySelector(".account-menu-btn");
        if (
            !accountContainer?.contains(event.target as Node) &&
            !accountBtnContainer?.contains(event.target as Node)
        ) {
            this.isAccountMenuVisible = false;
        }

        const orgContainer = document.querySelector(".org-menu-new");
        const orgBtnContainer = document.querySelector(".org-menu-new-btn");
        if (
            !orgContainer?.contains(event.target as Node) &&
            !orgBtnContainer?.contains(event.target as Node)
        ) {
            this.isOrgMenuVisible = false;
        }
    }
}
