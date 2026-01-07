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
    input,
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
import { DialogModule } from "primeng/dialog";
import { MenuModule } from "primeng/menu";
import { RadioButtonModule } from "primeng/radiobutton";
import {
    Organization,
    OrganizationData,
    User,
    UserInfo,
    Workspace,
} from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { WorkspaceService } from "src/app/core/service/business/workspace.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { generateColor } from "src/app/core/utils/color";
import { Constants } from "src/constants";
import { environment } from "src/environments/environment";
import { LeftSidebarComponent } from "../left-sidebar/left-sidebar.component";
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
        DialogModule,
        LeftSidebarComponent,
    ],
})
export class TopHeaderComponent implements OnInit {
    isSharedDs = input<boolean>(false);
    private readonly translate = inject(TranslateService);
    private readonly router = inject(Router);
    private readonly keycloak = inject(KeycloakService);
    private readonly userService = inject(UserService);
    private readonly destroyRef = inject(DestroyRef);
    private readonly globalStore = inject(GlobalStoreService);
    ecoDesignPercent = this.userService.ecoDesignPercent;
    maxLength = 30;
    ingredient: string = "english";
    selectedLanguage: string = "en";
    userDetails!: UserInfo;
    workspaces: OrganizationData[] = [];
    filteredWorkspaces: OrganizationData[] = [];
    initials = "";

    items: MenuItem[] | undefined;
    isAccountMenuVisible = false;
    isOrgMenuVisible = false;
    isAboutMenuOpen = false;
    enableSearchField = true;
    searchFieldTouched = true;
    searchModel!: string;
    modelWorkspace!: number;
    selectedWorkspace: Workspace = {} as Workspace;
    selectedOrganizationData: OrganizationData | undefined = undefined;
    currentOrganization: Organization = {} as Organization;
    selectedPath = "";
    selectedPage = signal("");
    isZoomedIn = computed(() => this.globalStore.zoomLevel() >= 150);
    @ViewChildren("radioItem") radioItems!: QueryList<ElementRef>;
    languages = ["en", "fr"];
    isMobile = computed(() => this.globalStore.mobileView());
    dialogVisible: boolean = false;
    mobileMenuItems: MenuItem[] | undefined;

    constructor(private readonly workspaceService: WorkspaceService) {}

    ngOnInit() {
        this.filteredWorkspaces = this.workspaces;
        this.selectedLanguage = this.translate.currentLang;
        this.setSelectedPage();
        this.router.events.subscribe((event) => {
            if (event instanceof NavigationEnd) {
                this.setSelectedPage();
                if (this.isMobile()) {
                    this.dialogVisible = false;
                }
            }
        });
        this.userService.currentOrganization$.subscribe(
            (organization) => (this.currentOrganization = organization),
        );
        this.userService.user$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((user: User) => {
                this.userDetails = {
                    firstName: user.firstName,
                    lastName: user.lastName,
                    email: user.email,
                };
                this.workspaces = [];
                for (const organization of user.organizations) {
                    for (const workspace of organization.workspaces) {
                        this.workspaces.push({
                            color: generateColor(workspace.name + organization.name),
                            id: workspace.id,
                            name: workspace.name,
                            workspace,
                            organization: organization,
                        });
                    }
                }
                this.userService.currentWorkspace$
                    .pipe(takeUntilDestroyed(this.destroyRef))
                    .subscribe((workspace: Workspace) => {
                        this.selectedWorkspace = workspace;
                        this.modelWorkspace = workspace.id;
                        this.selectedOrganizationData = {
                            color: generateColor(
                                workspace.name + this.currentOrganization.name,
                            ),
                            id: workspace.id,
                            name: workspace.name,
                            workspace,
                            organization: this.currentOrganization,
                        };
                        this.selectedPath = `/organizations/${this.currentOrganization.name}/workspaces/${this.selectedWorkspace?.id}`;
                    });
                this.initials =
                    this.getCapitaleLetter(this.userDetails?.firstName) +
                    this.getCapitaleLetter(this.userDetails?.lastName);

                this.searchWorkspaceList();
            });
        this.mobileMenuItems = [
            {
                label: "common.about",
                items: [
                    {
                        label: "common.useful-info",
                        route: Constants.USEFUL_INFORMATION,
                        subHeading: this.isSharedDs()
                            ? "common.shared-useful-info-desc"
                            : "common.useful-info-desc",
                        command: () => {
                            this.isSharedDs()
                                ? this.openDeclarationsInNewTab(
                                      Constants.USEFUL_INFORMATION,
                                  )
                                : this.router.navigate(["/useful-information"]);
                            this.dialogVisible = false;
                        },
                    },
                    {
                        label: "declarations.title",
                        route: Constants.USEFUL_INFORMATION,
                        subHeading: "declarations.accessibility-text",
                        ecoHeading: "declarations.ecodesign",
                        command: () => {
                            this.isSharedDs()
                                ? this.openDeclarationsInNewTab(Constants.DECLARATIONS)
                                : this.router.navigate(["/declarations"]);
                            this.dialogVisible = false;
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
                        command: () => {
                            window.open(
                                "https://saas-g4it.com/documentation/",
                                "_blank",
                                "noopener",
                            );
                        },
                    },
                    {
                        label: "common.boaviztapi-github-link",
                        link: "https://github.com/Boavizta/boaviztapi",
                        outsideLink: true,
                        command: () => {
                            window.open(
                                "https://github.com/Boavizta/boaviztapi",
                                "_blank",
                                "noopener",
                            );
                        },
                    },
                ],
            },
        ];
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
                        subHeading: this.isSharedDs()
                            ? "common.shared-useful-info-desc"
                            : "common.useful-info-desc",
                        command: () => {
                            this.isSharedDs()
                                ? this.openDeclarationsInNewTab(
                                      Constants.USEFUL_INFORMATION,
                                  )
                                : this.router.navigate(["/useful-information"]);
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
                        route: Constants.DECLARATIONS,
                        subHeading: "declarations.accessibility-text",
                        ecoHeading: "declarations.ecodesign",
                        command: () => {
                            this.isSharedDs()
                                ? this.openDeclarationsInNewTab(Constants.DECLARATIONS)
                                : this.router.navigate(["/declarations"]);
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
                    {
                        label: "common.boaviztapi-github-link",
                        link: "https://github.com/Boavizta/boaviztapi",
                        outsideLink: true,
                        borderClass: "border-light-grey-color",
                        command: () => {
                            window.open(
                                "https://github.com/Boavizta/boaviztapi",
                                "_blank",
                                "noopener",
                            );
                        },
                    },
                ],
            },
        ];
    }

    openDeclarationsInNewTab(page: string) {
        let [_, shared, sharedId, dsv, dsvId] = this.router.url.split("/");

        const url = this.router.serializeUrl(
            this.router.createUrlTree([shared, sharedId, dsv, dsvId, page]),
        );
        window.open(url, "_blank");
    }

    handleKeydown(event: KeyboardEvent) {
        const currentIndex = this.workspaces.findIndex(
            (workspace) => workspace.id === this.modelWorkspace,
        );

        let nextIndex = currentIndex;

        if (event.key === "ArrowDown" || event.key === "ArrowRight") {
            nextIndex = (currentIndex + 1) % this.workspaces.length;
        } else if (event.key === "ArrowUp" || event.key === "ArrowLeft") {
            nextIndex =
                (currentIndex - 1 + this.workspaces.length) % this.workspaces.length;
        } else if (event.key === "Enter" || event.key === " ") {
            this.selectCompany(this.workspaces[currentIndex]);
            event.preventDefault();
            return;
        }

        if (nextIndex !== currentIndex) {
            this.modelWorkspace = this.workspaces[nextIndex].id;

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

    selectCompany(workspace: OrganizationData) {
        this.userService.checkAndRedirect(
            workspace.organization!,
            workspace.workspace as Workspace,
            this.selectedPage(),
        );
        this.isOrgMenuVisible = false;
        if (this.isMobile()) {
            this.dialogVisible = false;
        }
    }

    toggleOrgMenu() {
        this.isOrgMenuVisible = !this.isOrgMenuVisible;
        this.searchModel = "";

        if (this.isOrgMenuVisible) {
            const elementToView = document.querySelector(`#org-${this.modelWorkspace}`);
            setTimeout(() => {
                elementToView?.scrollIntoView({ behavior: "smooth", block: "start" });
            }, 0);
            this.showDialog();
            this.searchWorkspaceList();
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
        localStorage.removeItem("currentWorkspace");
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

    showDialog() {
        this.dialogVisible = true;
    }

    searchWorkspaceList() {
        const filter = this.searchModel?.toLowerCase() || "";
        this.filteredWorkspaces = filter
            ? this.workspaces.filter((o) =>
                  o.workspace!.name.toLowerCase().includes(filter),
              )
            : this.workspaces;
    }
    onClick($event: MouseEvent) {
        $event.stopPropagation();
    }
}
