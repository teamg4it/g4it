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
    inject,
    input,
    OnInit,
    signal,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { NavigationEnd, Router, RouterModule } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import {
    Organization,
    OrganizationData,
    User,
    UserInfo,
    Workspace,
} from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { generateColor } from "src/app/core/utils/color";
import { Constants } from "src/constants";
import { environment } from "src/environments/environment";

@Component({
    standalone: true,
    selector: "app-left-sidebar",
    templateUrl: "./left-sidebar.component.html",
    imports: [CommonModule, TranslateModule, RouterModule],
})
export class LeftSidebarComponent implements OnInit {
    isSharedDs = input<boolean>(false);
    private readonly destroyRef = inject(DestroyRef);
    private readonly router = inject(Router);
    public userService = inject(UserService);
    private readonly translate = inject(TranslateService);
    constants = Constants;
    homeTitle = computed(() =>
        this.getTitle("welcome-page.title", this.constants.WELCOME_PAGE),
    );
    digitalServicesTitle = computed(() =>
        this.getTitle("digital-services.title", "digital-services"),
    );
    ecoMindAiTitle = computed(() => this.getTitle("eco-mind-ai.title", "eco-mind-ai"));
    inventoriesTitle = computed(() => this.getTitle("inventories.title", "inventories"));
    administrationTitle = computed(() =>
        this.getTitle("common.administration", "administration"),
    );
    digitalServicesAriaCurrent = computed(() => this.getAriaCurrent("digital-services"));
    inventoriesAriaCurrent = computed(() => this.getAriaCurrent("inventories"));
    ecoMindAiAriaCurrent = computed(() => this.getAriaCurrent("eco-mind-ai"));
    administrationAriaCurrent = computed(() => this.getAriaCurrent("administration"));
    public globalStore = inject(GlobalStoreService);
    selectedPage = signal("");

    selectedLanguage: string = "en";

    workspaces: OrganizationData[] = [];

    selectedWorkspace: Workspace = {} as Workspace;
    selectedWorkspaceData: OrganizationData | undefined = undefined;
    selectedPath = "";

    currentOrganization: Organization = {} as Organization;

    isAdminOnWorkspaceOrOrganization = false;
    userDetails!: UserInfo;
    isZoomedIn = computed(() => this.globalStore.zoomLevel() >= 150);
    isZoomedIn125 = computed(() => this.globalStore.zoomLevel() >= 125);
    isEcoMindEnabledForCurrentOrganization: boolean = false;
    isEcoMindModuleEnabled: boolean = environment.isEcomindEnabled;
    isMobile = computed(() => this.globalStore.mobileView());

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
                this.workspaces = [];
                for (const organization of user.organizations) {
                    for (const workspace of organization.workspaces) {
                        this.workspaces.push({
                            color: generateColor(workspace.name + organization.name),
                            id: workspace.id,
                            name: workspace.name,
                            workspace,
                            organization,
                        });
                    }
                }
                this.isAdminOnWorkspaceOrOrganization =
                    this.userService.hasAnyAdminRole(user);
            });

        this.userService.currentOrganization$.subscribe((organization) => {
            this.currentOrganization = organization;
            this.isEcoMindEnabledForCurrentOrganization =
                this.currentOrganization.ecomindai;
        });

        this.userService.currentWorkspace$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((workspace) => {
                this.selectedWorkspace = workspace;
                this.selectedWorkspaceData = {
                    color: generateColor(workspace.name + this.currentOrganization.name),
                    id: workspace.id,
                    name: workspace.name,
                    workspace,
                    organization: this.currentOrganization as any,
                };
                this.selectedPath = `/organizations/${this.currentOrganization.name}/workspaces/${this.selectedWorkspace?.id}`;
            });
    }

    constructor(private readonly el: ElementRef) {}

    setSelectedPage() {
        this.selectedPage.set(this.userService.getSelectedPage());
    }

    getTitle(name: string, page: string): any {
        return this.selectedPage() === page
            ? this.translate.instant(name, {
                  WorkspaceName: this.selectedWorkspace.name,
              }) +
                  " - " +
                  this.translate.instant("common.active-page")
            : this.translate.instant(name, {
                  WorkspaceName: this.selectedWorkspace.name,
              });
    }

    getAriaCurrent(page: string): any {
        return this.selectedPage() === page ? "page" : null;
    }
}
