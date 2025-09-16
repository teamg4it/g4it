/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { TestBed } from "@angular/core/testing";

import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { NavigationEnd, Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { ToastModule } from "primeng/toast";
import { of, ReplaySubject } from "rxjs";
import { BasicRoles, Role } from "../../interfaces/roles.interfaces";
import { Organization, User, Workspace } from "../../interfaces/user.interfaces";
import { UserDataService } from "../data/user-data.service";
import { UserService } from "./user.service";

describe("UserService", () => {
    let httpMock: HttpTestingController;
    let service: UserService;
    let router: Router;
    let userDataService: UserDataService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [
                RouterTestingModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
                ToastModule,
            ],
            providers: [UserService, MessageService, TranslateService, UserDataService],
        });
        userDataService = TestBed.inject(UserDataService);
        service = TestBed.inject(UserService);
        router = TestBed.inject(Router);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be create", () => {
        expect(UserService).toBeTruthy();
    });

    it("should check if organization admin is allowed to see page admnistration ", () => {
        const organization = {
            roles: [Role.OrganizationAdmin],
        } as Organization;

        const workspace = {} as Workspace;

        let result = service.checkIfAllowed(organization, workspace, "administration");

        expect(result).toBeTrue();
    });

    it("should check if organization admin is allowed to see page admnistration ", () => {
        const organization = {
            roles: [Role.DigitalServiceRead],
        } as Organization;

        const workspace = {
            roles: [Role.WorkspaceAdmin],
        } as Workspace;

        var result = service.checkIfAllowed(organization, workspace, "administration");

        expect(result).toBeTrue();
    });

    it("should check if read only user is allowed to see page admnistration ", () => {
        const organization = {
            roles: [Role.DigitalServiceRead],
        } as Organization;

        const workspace = {
            roles: [Role.DigitalServiceRead],
        } as Workspace;

        var result = service.checkIfAllowed(organization, workspace, "administration");

        expect(result).toBeFalse();
    });

    it("should check if organization administrator is allowed to see page inventories and digital-services page", () => {
        const organization = {
            roles: [Role.OrganizationAdmin],
        } as Organization;

        const workspace = {
            roles: [] as any,
        } as Workspace;

        var resultIS = service.checkIfAllowed(organization, workspace, "inventories");
        var resultDS = service.checkIfAllowed(
            organization,
            workspace,
            "digital-services",
        );

        expect(resultIS).toBeTrue();
        expect(resultDS).toBeTrue();
    });

    describe("checkRouterEvents", () => {
        it("should call organizationWorkspaceHandling on router events", () => {
            const navigationEnd = new NavigationEnd(
                1,
                "/organizations/test/workspaces/1/inventories",
                "/",
            );
            spyOnProperty(router, "events", "get").and.returnValue(of(navigationEnd));
            userDataService.userSubject = new ReplaySubject<User>(1);
            const user: User = {
                organizations: [{ name: "test", workspaces: [{ id: 1 }] }],
            } as User;
            userDataService.userSubject.next(user);

            spyOn(service, "organizationWorkspaceHandling");

            service.checkRouterEvents();

            expect(service.organizationWorkspaceHandling).toHaveBeenCalledWith(user, "");
        });
    });

    describe("checkIfAllowed", () => {
        it("should check if user is allowed to see page correctly", () => {
            const organization = {
                roles: [Role.OrganizationAdmin],
            } as Organization;

            const workspace = {
                roles: [Role.WorkspaceAdmin],
            } as Workspace;

            expect(
                service.checkIfAllowed(organization, workspace, "administration"),
            ).toBeTrue();
        });
    });

    describe("handleRoutingEvents", () => {
        it('should return if organizations is "something-went-wrong"', () => {
            const user: User = { organizations: [] } as any;

            spyOn(service, "errorMessage");
            spyOn(router, "navigateByUrl");

            service.handleRoutingEvents(
                "something-went-wrong",
                user,
                "test",
                "1",
                "inventories",
            );

            expect(service.errorMessage).not.toHaveBeenCalled();
            expect(router.navigateByUrl).not.toHaveBeenCalled();
        });

        it("should show error message and navigate to 403 if currentUser has no organizations", () => {
            const user: User = { organizations: [] } as any;

            spyOn(service, "errorMessage");
            spyOn(router, "navigateByUrl");

            service.handleRoutingEvents(
                "organizations",
                user,
                "test",
                "1",
                "inventories",
            );

            expect(router.navigateByUrl).toHaveBeenCalledWith("something-went-wrong/403");
        });

        it('should call handlePageRouting if page is "inventories" or "digital-services"', () => {
            const user: User = {
                organizations: [{ name: "test", workspaces: [{ id: 1 }] }],
            } as User;

            spyOn(service, "handlePageRouting");

            service.handleRoutingEvents(
                "organizations",
                user,
                "test",
                "1",
                "inventories",
            );
            expect(service.handlePageRouting).toHaveBeenCalledWith(
                user,
                "test",
                "1",
                "inventories",
            );

            service.handleRoutingEvents(
                "organizations",
                user,
                "test",
                "1",
                "digital-services",
            );
            expect(service.handlePageRouting).toHaveBeenCalledWith(
                user,
                "test",
                "1",
                "digital-services",
            );
        });

        it("should call organizationWorkspaceHandling for other pages", () => {
            const user: User = {
                organizations: [{ name: "test", workspaces: [{ id: 1 }] }],
            } as User;

            spyOn(service, "organizationWorkspaceHandling");

            service.handleRoutingEvents("organizations", user, "test", "1", "other-page");
            expect(service.organizationWorkspaceHandling).toHaveBeenCalledWith(
                user,
                "organizations",
            );
        });

        it("should handle case when organization is not found", () => {
            const user: User = {
                organizations: [{ name: "test", workspaces: [{ id: 1 }] }],
            } as User;

            spyOn(service, "errorMessage");
            spyOn(router, "navigateByUrl");

            service.handleRoutingEvents(
                "organizations",
                user,
                "non-existent-organization",
                "1",
                "inventories",
            );

            expect(service.errorMessage).toHaveBeenCalledWith(
                "insuffisant-right-organization",
            );
            expect(router.navigateByUrl).toHaveBeenCalledWith("/");
        });

        it("should handle case when organization is not found", () => {
            const user: User = {
                organizations: [{ name: "test", workspaces: [{ id: 1 }] }],
            } as User;

            spyOn(service, "errorMessage");
            spyOn(router, "navigateByUrl");

            service.handleRoutingEvents(
                "organizations",
                user,
                "test",
                "non-existent-organization",
                "inventories",
            );

            expect(service.errorMessage).toHaveBeenCalledWith(
                "insuffisant-right-workspace",
            );
            expect(router.navigateByUrl).toHaveBeenCalledWith("/");
        });

        it("should call setOrganizationAndWorkspace if checkIfAllowed returns true", () => {
            const user: User = {
                organizations: [{ name: "test", workspaces: [{ id: 1 }] }],
            } as any;
            const organization = user.organizations[0];
            const workspace = organization.workspaces[0];

            spyOn(service, "checkIfAllowed").and.returnValue(true);
            spyOn(service, "setOrganizationAndWorkspace");

            service.handleRoutingEvents(
                "organizations",
                user,
                "test",
                "1",
                "inventories",
            );

            expect(service.checkIfAllowed).toHaveBeenCalledWith(
                organization,
                workspace,
                "inventories",
            );
            expect(service.setOrganizationAndWorkspace).toHaveBeenCalledWith(
                organization,
                workspace,
            );
        });
    });

    it("should navigate to the specified page if user is allowed", () => {
        const organization = {
            roles: [Role.OrganizationAdmin],
        } as Organization;

        const workspace = {
            roles: [Role.WorkspaceAdmin],
        } as Workspace;

        spyOn(service, "checkIfAllowed").and.returnValue(true);
        spyOn(service, "setOrganizationAndWorkspace");
        spyOn(router, "navigateByUrl");

        service.checkAndRedirect(organization, workspace, "inventories");

        expect(service.checkIfAllowed).toHaveBeenCalledWith(
            organization,
            workspace,
            "inventories",
        );
        expect(service.setOrganizationAndWorkspace).toHaveBeenCalledWith(
            organization,
            workspace,
        );
        expect(router.navigateByUrl).toHaveBeenCalledWith(
            `organizations/${organization.name}/workspaces/${workspace.id}/inventories`,
        );
    });

    it("should navigate to the 403 page if user is not allowed", () => {
        const organization = {
            name: "testOrganization",
            roles: [Role.DigitalServiceRead],
        } as Organization;

        const workspace = {
            id: 1,
            roles: [Role.DigitalServiceRead],
        } as Workspace;

        spyOn(service, "checkIfAllowed").and.returnValue(false);
        spyOn(router, "navigateByUrl");

        service.checkAndRedirect(organization, workspace, "administration");

        expect(service.checkIfAllowed).toHaveBeenCalledWith(
            organization,
            workspace,
            "administration",
        );
        expect(router.navigateByUrl).toHaveBeenCalledWith("welcome-page");
    });

    it("should set the workspace, organization, and roles", () => {
        const organization = {
            name: "testOrganization",
            roles: [Role.OrganizationAdmin],
            workspaces: [
                {
                    id: 1,
                    roles: [Role.WorkspaceAdmin],
                },
            ],
        } as Organization;

        const workspace = {
            id: 1,
            roles: [Role.WorkspaceAdmin],
        } as Workspace;

        spyOn(service.organizationSubject, "next");
        spyOn(service.workspaceSubject, "next");
        spyOn(localStorage, "setItem");
        spyOn(service["rolesSubject"], "next");

        service.setOrganizationAndWorkspace(organization, workspace);

        expect(service.organizationSubject.next).toHaveBeenCalledWith(organization);
        expect(service.workspaceSubject.next).toHaveBeenCalledWith(workspace);
        expect(localStorage.setItem).toHaveBeenCalledWith(
            "currentOrganization",
            organization.name,
        );
        expect(localStorage.setItem).toHaveBeenCalledWith(
            "currentWorkspace",
            workspace.id.toString(),
        );
        expect(service["rolesSubject"].next).toHaveBeenCalledWith([
            Role.OrganizationAdmin,
            Role.WorkspaceAdmin,
            ...BasicRoles,
        ]);
    });

    describe("hasAnyWorkspaceAdminRole", () => {
        it("should return true if the user has any organization admin role", () => {
            const user: User = {
                organizations: [
                    {
                        name: "test",
                        workspaces: [
                            {
                                id: 1,
                                roles: [Role.WorkspaceAdmin],
                            },
                        ],
                    },
                ],
            } as User;

            const result = service.hasAnyWorkspaceAdminRole(user);

            expect(result).toBeTrue();
        });

        it("should return false if the user does not have any organization admin role", () => {
            const user: User = {
                organizations: [
                    {
                        name: "test",
                        workspaces: [
                            {
                                id: 1,
                                roles: [Role.DigitalServiceRead],
                            },
                        ],
                    },
                ],
            } as User;

            const result = service.hasAnyWorkspaceAdminRole(user);

            expect(result).toBeFalse();
        });
    });

    describe("hasAnyOrganizationAdminRole", () => {
        it("should return true if the user has a organization with OrganizationAdmin role", () => {
            const user: User = {
                organizations: [
                    {
                        roles: [Role.OrganizationAdmin],
                    },
                    {
                        roles: [Role.DigitalServiceRead],
                    },
                ],
            } as User;

            const result = service.hasAnyOrganizationAdminRole(user);

            expect(result).toBeTrue();
        });

        it("should return false if the user does not have a organization with OrganizationAdmin role", () => {
            const user: User = {
                organizations: [
                    {
                        roles: [Role.DigitalServiceRead],
                    },
                    {
                        roles: [Role.DigitalServiceRead],
                    },
                ],
            } as User;

            const result = service.hasAnyOrganizationAdminRole(user);

            expect(result).toBeFalse();
        });
    });

    describe("errorMessage", () => {
        it("should add a warning message to the message service", () => {
            const messageServiceSpy = spyOn(service["messageService"], "add");

            service.errorMessage("test-key");

            expect(messageServiceSpy).toHaveBeenCalledWith({
                severity: "warn",
                summary: jasmine.any(String),
                detail: jasmine.any(String),
            });
        });
    });

    describe("hasAnyAdminRole", () => {
        it("should return true if the user has any admin role", () => {
            const user: User = {
                organizations: [
                    {
                        roles: [Role.OrganizationAdmin],
                        workspaces: [
                            {
                                roles: [Role.WorkspaceAdmin],
                            },
                        ],
                    },
                ],
            } as User;

            const result = service.hasAnyAdminRole(user);

            expect(result).toBeTrue();
        });

        it("should return false if the user does not have any admin role", () => {
            const user: User = {
                organizations: [
                    {
                        roles: [Role.DigitalServiceRead],
                        workspaces: [
                            {
                                roles: [Role.DigitalServiceRead],
                            },
                        ],
                    },
                ],
            } as User;

            const result = service.hasAnyAdminRole(user);

            expect(result).toBeFalse();
        });
    });
    describe("organizationWorkspaceHandling", () => {
        it("should set the default workspace and organization if the URL is unknown", () => {
            const currentUser: User = {
                organizations: [
                    {
                        name: "testOrganization",
                        workspaces: [
                            {
                                id: 1,
                                roles: [Role.WorkspaceAdmin],
                            },
                        ],
                    },
                ],
            } as User;

            spyOn(service, "getOrganization").and.returnValue(
                currentUser.organizations[0],
            );
            spyOn(service, "getWorkspace").and.returnValue(
                currentUser.organizations[0].workspaces[0],
            );
            spyOn(service, "checkIfAllowed").and.returnValue(true);
            spyOn(service, "setOrganizationAndWorkspace");

            service.organizationWorkspaceHandling(currentUser, "unknown-url");

            expect(service.getOrganization).toHaveBeenCalledWith(currentUser);
            expect(service.getWorkspace).toHaveBeenCalledWith(
                currentUser.organizations[0],
            );
            expect(service.setOrganizationAndWorkspace).toHaveBeenCalledWith(
                currentUser.organizations[0],
                currentUser.organizations[0].workspaces[0],
            );
        });

        it("should navigate to the 403 page if the current user is not allowed", () => {
            const currentUser: User = {
                organizations: [
                    {
                        name: "testOrganization",
                        workspaces: [
                            {
                                id: 1,
                                roles: [Role.DigitalServiceRead],
                            },
                        ],
                    },
                ],
            } as User;

            spyOn(service, "checkIfAllowed").and.returnValue(false);
            spyOn(router, "navigateByUrl");

            service.organizationWorkspaceHandling(currentUser, "inventories");

            expect(service.checkIfAllowed).toHaveBeenCalledWith(
                currentUser.organizations[0],
                currentUser.organizations[0].workspaces[0],
                "inventories",
            );
        });
    });
});
