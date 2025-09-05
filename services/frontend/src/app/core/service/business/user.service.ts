/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Injectable } from "@angular/core";
import { ReplaySubject, filter, map } from "rxjs";
import { UserDataService } from "../data/user-data.service";
import { Organization, User, Workspace } from "./../../interfaces/user.interfaces";

import { NavigationEnd, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { Constants } from "src/constants";
import { BasicRoles, Role } from "../../interfaces/roles.interfaces";

@Injectable({
    providedIn: "root",
})
export class UserService {
    ecoDesignPercent = 77;
    public workspaceSubject = new ReplaySubject<Workspace>(1);

    public organizationSubject = new ReplaySubject<Organization>(1);

    private readonly rolesSubject = new ReplaySubject<Role[]>(1);

    roles$ = this.rolesSubject.asObservable();

    currentOrganization$ = this.organizationSubject.asObservable();

    currentWorkspace$ = this.workspaceSubject.asObservable();

    user$ = this.userDataService.userSubject.asObservable();

    isAllowedSubscriberAdmin$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.SubscriberAdmin)),
    );

    isAllowedOrganizationAdmin$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.OrganizationAdmin)),
    );

    isAllowedDigitalServiceRead$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.DigitalServiceRead)),
    );

    isAllowedInventoryRead$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.InventoryRead)),
    );

    isAllowedInventoryWrite$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.InventoryWrite)),
    );

    isAllowedDigitalServiceWrite$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.DigitalServiceWrite)),
    );

    isAllowedEcoMindAiRead$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.EcoMindAiRead)),
    );

    isAllowedEcoMindAiWrite$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.EcoMindAiWrite)),
    );

    constructor(
        private readonly router: Router,
        private readonly userDataService: UserDataService,
        private readonly messageService: MessageService,
        private readonly translate: TranslateService,
    ) {
        this.checkRouterEvents();
    }

    checkRouterEvents(): void {
        if (this.router?.events) {
            this.router.events
                .pipe(filter((event) => event instanceof NavigationEnd))
                .subscribe(() => {
                    this.userDataService.userSubject.subscribe((currentUser) => {
                        const [
                            _,
                            organizations,
                            organizationName,
                            _1,
                            workspaceId,
                            page,
                        ] = this.router.url.split("/");
                        this.handleRoutingEvents(
                            organizations,
                            currentUser,
                            organizationName,
                            workspaceId,
                            page,
                        );
                    });
                });
        }
    }

    handleRoutingEvents(
        organizations: string,
        currentUser: User,
        organizationName: string,
        workspaceId: string,
        page: string,
    ): void {
        if (organizations === "something-went-wrong") {
            return;
        }

        if (currentUser.organizations.length === 0) {
            this.errorMessage("organization-or-workspace-not-found");
            this.router.navigateByUrl(`something-went-wrong/403`);
            return;
        }

        if (
            page !== undefined &&
            ["inventories", "digital-services", "eco-mind-ai"].includes(page)
        ) {
            return this.handlePageRouting(
                currentUser,
                organizationName,
                workspaceId,
                page,
            );
        }

        return this.organizationWorkspaceHandling(currentUser, organizations);
    }

    handlePageRouting(
        currentUser: User,
        organizationName: string,
        workspaceId: string,
        page: string,
    ): void {
        const organization = currentUser?.organizations.find(
            (org) => org.name == organizationName,
        );

        const workspace = organization?.workspaces.find(
            (w) => w.id === Number(workspaceId),
        );

        if (organization === undefined) {
            this.errorMessage("insuffisant-right-organization");
            this.router.navigateByUrl("/");
            return;
        }
        if (workspace === undefined) {
            this.errorMessage("insuffisant-right-workspace");
            this.router.navigateByUrl("/");
            return;
        }
        this.setOrganizationAndWorkspace(organization, workspace);
        if (!this.checkIfAllowed(organization, workspace, page)) {
            this.router.navigateByUrl(Constants.WELCOME_PAGE);
        }
    }

    organizationWorkspaceHandling(currentUser: User, organizations: string): void {
        // If the url is unknown, we set the default organization and the default workspace
        let organization: Organization | undefined = this.getOrganization(currentUser);
        let workspace: Workspace | undefined;

        if (organization) {
            workspace = this.getWorkspace(organization);
        }

        if (Constants.VALID_PAGES.includes(organizations)) {
            this.setOrganizationAndWorkspace(organization, workspace!);
            return;
        }
        if (organizations === "administration") {
            if (this.hasAnyAdminRole(currentUser)) {
                this.setOrganizationAndWorkspace(organization, workspace!);
                return;
            } else {
                this.setOrganizationAndWorkspace(organization, workspace!);
                this.router.navigateByUrl(Constants.WELCOME_PAGE);
            }
        }

        if (organization && workspace) {
            for (const type of ["inventories", "digital-services"]) {
                if (this.checkIfAllowed(organization, workspace, type)) {
                    this.setOrganizationAndWorkspace(organization, workspace);
                    this.router.navigateByUrl(
                        `organizations/${organization.name}/workspaces/${workspace.id}/${type}`,
                    );
                    break;
                }
            }
        }
    }

    getWorkspace(organization: Organization): Workspace {
        let workspace: Workspace | undefined;
        let workspaceNameLS = localStorage.getItem("currentWorkspace") ?? undefined;

        if (workspaceNameLS && Number.isNaN(workspaceNameLS)) {
            localStorage.removeItem("currentWorkspace");
            workspaceNameLS = undefined;
        }

        if (workspaceNameLS) {
            const tmpWorkspaces = organization.workspaces.filter(
                (o) => o.id === Number(workspaceNameLS),
            );
            if (tmpWorkspaces.length > 0) {
                workspace = tmpWorkspaces[0];
            }
        }
        if (workspace === undefined) workspace = organization.workspaces[0];
        return workspace;
    }

    getOrganization(currentUser: User): Organization {
        let organization: Organization | undefined;
        const organizationNameLS =
            localStorage.getItem("currentOrganization") ?? undefined;

        if (organizationNameLS) {
            const tmpSubs = currentUser.organizations.filter(
                (s) => s.name === organizationNameLS,
            );
            if (tmpSubs.length === 0) {
                organization = currentUser.organizations[0];
            } else {
                organization = tmpSubs[0];
            }
        } else {
            organization = currentUser.organizations[0];
        }
        return organization;
    }

    errorMessage(key: string): void {
        this.messageService.add({
            severity: "warn",
            summary: this.translate.instant(`toast-errors.${key}.title`),
            detail: this.translate.instant(`toast-errors.${key}.text`),
        });
    }

    hasAnyAdminRole(user: User): boolean {
        return (
            this.hasAnyOrganizationAdminRole(user) || this.hasAnySubscriberAdminRole(user)
        );
    }

    hasAnySubscriberAdminRole(user: User): boolean {
        return user.organizations.some((organization) =>
            organization.roles.includes(Role.SubscriberAdmin),
        );
    }

    hasAnyOrganizationAdminRole(user: User): boolean {
        return user.organizations.some((organization) =>
            organization.workspaces.some((workspace) =>
                workspace.roles.includes(Role.OrganizationAdmin),
            ),
        );
    }

    getRoles(organization: Organization, workspace: Workspace): Role[] {
        if (organization.roles.includes(Role.SubscriberAdmin)) {
            return [Role.SubscriberAdmin, Role.OrganizationAdmin, ...BasicRoles];
        }

        if (workspace.roles.includes(Role.OrganizationAdmin)) {
            return [Role.OrganizationAdmin, ...BasicRoles];
        }

        const roles = [...workspace.roles];

        if (workspace.roles.includes(Role.InventoryWrite)) {
            roles.push(Role.InventoryRead);
        }

        if (workspace.roles.includes(Role.DigitalServiceWrite)) {
            roles.push(Role.DigitalServiceRead);
        }

        if (workspace.roles.includes(Role.EcoMindAiWrite)) {
            roles.push(Role.EcoMindAiRead);
        }

        return roles;
    }

    checkIfAllowed(
        organization: Organization,
        workspace: Workspace,
        uri: string,
    ): boolean {
        let roles: Role[] = this.getRoles(organization, workspace);

        if (Constants.VALID_PAGES.includes(uri)) {
            return true;
        }

        if (uri === "inventories" && roles.includes(Role.InventoryRead)) {
            return true;
        }

        if (uri === "digital-services" && roles.includes(Role.DigitalServiceRead)) {
            return true;
        }

        if (
            uri === "eco-mind-ai" &&
            roles.includes(Role.EcoMindAiRead) &&
            organization.ecomindai
        ) {
            return true;
        }

        if (
            uri === "administration" &&
            (roles.includes(Role.SubscriberAdmin) ||
                roles.includes(Role.OrganizationAdmin))
        ) {
            return true;
        }

        return false;
    }

    setOrganizationAndWorkspace(organization: Organization, workspace: Workspace): void {
        this.organizationSubject.next(organization);
        this.workspaceSubject.next(workspace);
        localStorage.setItem("currentOrganization", organization.name);
        localStorage.setItem("currentWorkspace", workspace.id.toString());
        this.rolesSubject.next(this.getRoles(organization, workspace));
    }

    checkAndRedirect(
        organization: Organization,
        workspace: Workspace,
        page: string,
    ): void {
        this.setOrganizationAndWorkspace(organization, workspace);
        if (this.checkIfAllowed(organization, workspace, page)) {
            if (
                page === "inventories" ||
                page === "digital-services" ||
                page === "eco-mind-ai"
            ) {
                this.router.navigateByUrl(
                    `organizations/${organization.name}/workspaces/${workspace.id}/${page}`,
                );
            }
        } else {
            this.router.navigateByUrl(Constants.WELCOME_PAGE);
        }
    }

    getSelectedPage(): string {
        let [_, organizations, _1, _2, _3, page] = this.router.url.split("/");

        const validPages = ["administration", ...Constants.VALID_PAGES];
        return validPages.includes(organizations) ? organizations : page;
    }

    composeEmail(
        currentOrganization: Organization,
        selectedWorkspace: Workspace,
    ): string {
        let subject = `[${currentOrganization.name}/${selectedWorkspace?.id}] ${Constants.SUBJECT_MAIL}`;
        return `mailto:${Constants.RECIPIENT_MAIL}?subject=${subject}`;
    }
}
