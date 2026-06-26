/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { Router } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { of } from "rxjs";
import { WorkspaceWithOrganization } from "src/app/core/interfaces/administration.interfaces";
import { Role } from "src/app/core/interfaces/roles.interfaces";
import { UserDetails } from "src/app/core/interfaces/user.interfaces";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { UserService } from "src/app/core/service/business/user.service";
import { UserDataService } from "src/app/core/service/data/user-data.service";
import { AddWorkspaceComponent } from "./add-workspace.component";

describe("AddWorkspaceComponent", () => {
    let component: AddWorkspaceComponent;
    let fixture: ComponentFixture<AddWorkspaceComponent>;
    let mockAdministrationService: jasmine.SpyObj<AdministrationService>;
    let mockTranslateService: jasmine.SpyObj<TranslateService>;
    let mockUserDataService: jasmine.SpyObj<UserDataService>;
    let mockUserService: jasmine.SpyObj<UserService>;
    let mockRouter: jasmine.SpyObj<Router>;

    const mockUser = {
        id: 1,
        email: "test@example.com",
        firstName: "Test",
        lastName: "User",
        roles: [Role.InventoryRead],
    } as UserDetails;

    const mockWorkspace: WorkspaceWithOrganization = {
        workspaceId: 1,
        workspaceName: "Test Workspace",
        organizationId: 1,
        organizationName: "Test Org",
        authorizedDomains: ["example.com"],
    } as WorkspaceWithOrganization;

    beforeEach(async () => {
        mockAdministrationService = jasmine.createSpyObj("AdministrationService", [
            "postUserToWorkspaceAndAddRoles",
        ]);
        mockTranslateService = jasmine.createSpyObj("TranslateService", ["instant"]);
        mockUserDataService = jasmine.createSpyObj("UserDataService", ["fetchUserInfo"]);
        mockUserService = jasmine.createSpyObj("UserService", [], {
            user$: of(mockUser),
        });
        mockRouter = jasmine.createSpyObj("Router", ["navigateByUrl"]);

        mockTranslateService.instant.and.callFake((key: string) => {
            const translations: { [key: string]: string } = {
                "administration.role.user": "User",
                "administration.role.admin": "Admin",
                "administration.role.read": "Read",
                "administration.role.write": "Write",
            };
            return translations[key] || key;
        });

        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot(), AddWorkspaceComponent],
            providers: [
                { provide: AdministrationService, useValue: mockAdministrationService },
                { provide: TranslateService, useValue: mockTranslateService },
                { provide: UserDataService, useValue: mockUserDataService },
                { provide: UserService, useValue: mockUserService },
                { provide: Router, useValue: mockRouter },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(AddWorkspaceComponent);
        component = fixture.componentInstance;
        component.workspace = mockWorkspace;
        component.userDetail = mockUser;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    describe("ngOnInit", () => {
        it("should initialize module values with correct roles", () => {
            component.ngOnInit();

            expect(component.isModuleValues.length).toBe(2);
            expect(component.dsModuleValues.length).toBe(2);
            expect(component.ecomindModuleValues.length).toBe(2);
            expect(component.adminModuleValues.length).toBe(2);
        });

        it("should set up IS module values correctly", () => {
            component.ngOnInit();

            expect(component.isModuleValues[0].code).toBe(Role.InventoryRead);
            expect(component.isModuleValues[1].code).toBe(Role.InventoryWrite);
        });

        it("should set up DS module values correctly", () => {
            component.ngOnInit();

            expect(component.dsModuleValues[0].code).toBe(Role.DigitalServiceRead);
            expect(component.dsModuleValues[1].code).toBe(Role.DigitalServiceWrite);
        });

        it("should set up EcoMind module values correctly", () => {
            component.ngOnInit();

            expect(component.ecomindModuleValues[0].code).toBe(Role.EcoMindAiRead);
            expect(component.ecomindModuleValues[1].code).toBe(Role.EcoMindAiWrite);
        });

        it("should set up admin module values correctly", () => {
            component.ngOnInit();

            expect(component.adminModuleValues[0].code).toBe("SimpleUser" as Role);
            expect(component.adminModuleValues[0].value).toBe("User");
            expect(component.adminModuleValues[1].code).toBe(Role.WorkspaceAdmin);
            expect(component.adminModuleValues[1].value).toBe("Admin");
        });

        it("should call restrictAdminRoleByDomain", () => {
            spyOn(component, "restrictAdminRoleByDomain");
            component.ngOnInit();

            expect(component.restrictAdminRoleByDomain).toHaveBeenCalled();
        });
    });

    describe("ngOnChanges", () => {
        beforeEach(() => {
            component.ngOnInit();
        });



        it("should return early if userDetail.roles is undefined", () => {
            component.userDetail = { ...mockUser, roles: undefined } as any;
            component.ngOnChanges();

            expect(component.adminModule).toEqual({} as any);
        });

        it("should force admin if user has WorkspaceAdmin role", () => {
            component.userDetail = {
                ...mockUser,
                roles: [Role.WorkspaceAdmin],
            };
            spyOn(component, "forceAdmin");
            component.ngOnChanges();

            expect(component.forceAdmin).toHaveBeenCalled();
        });

        it("should set adminModule to SimpleUser for non-admin users", () => {
            component.userDetail = {
                ...mockUser,
                roles: [Role.InventoryRead],
            };
            component.ngOnChanges();

            expect(component.adminModule.code).toBe("SimpleUser" as Role);
            expect(component.adminModule.value).toBe("User");
        });

        it("should set isModule for users with inventory roles", () => {
            component.userDetail = {
                ...mockUser,
                roles: [Role.InventoryRead, Role.InventoryWrite],
            };
            component.ngOnChanges();

            expect(component.isModule.code).toBe(Role.InventoryWrite);
        });

        it("should set dsModule for users with digital service roles", () => {
            component.userDetail = {
                ...mockUser,
                roles: [Role.DigitalServiceRead, Role.DigitalServiceWrite],
            };
            component.ngOnChanges();

            expect(component.dsModule.code).toBe(Role.DigitalServiceWrite);
        });

        it("should set ecomindModule for users with ecomind roles", () => {
            component.userDetail = {
                ...mockUser,
                roles: [Role.EcoMindAiRead, Role.EcoMindAiWrite],
            };
            component.ngOnChanges();

            expect(component.ecomindModule.code).toBe(Role.EcoMindAiWrite);
        });

        it("should prioritize higher permission roles", () => {
            component.userDetail = {
                ...mockUser,
                roles: [Role.InventoryRead, Role.InventoryWrite],
            };
            component.ngOnChanges();

            expect(component.isModule.code).toBe(Role.InventoryWrite);
        });
    });

    describe("getRoleValue", () => {
        beforeEach(() => {
            component.ngOnInit();
        });

        it("should return role value with correct code and translated value", () => {
            const result = component.getRoleValue(Role.InventoryRead);

            expect(result.code).toBe(Role.InventoryRead);
            expect(result.value).toBe("Read");
        });

        it("should handle write roles correctly", () => {
            const result = component.getRoleValue(Role.InventoryWrite);

            expect(result.code).toBe(Role.InventoryWrite);
            expect(result.value).toBe("Write");
        });

        it("should call translate.instant with correct key", () => {
            component.getRoleValue(Role.DigitalServiceRead);

            expect(mockTranslateService.instant).toHaveBeenCalledWith(
                "administration.role.read",
            );
        });
    });

    describe("readOrWrite", () => {
        it("should return 'read' for roles ending with READ", () => {
            const result = component.readOrWrite(Role.InventoryRead);

            expect(result).toBe("read");
        });

        it("should return 'write' for roles ending with WRITE", () => {
            const result = component.readOrWrite(Role.InventoryWrite);

            expect(result).toBe("write");
        });

        it("should return undefined for other roles", () => {
            const result = component.readOrWrite(Role.WorkspaceAdmin);

            expect(result).toBeUndefined();
        });
    });

    describe("getWorkspaceBody", () => {
        beforeEach(() => {
            component.ngOnInit();
            component.userDetail = mockUser;
        });

        it("should return body with WorkspaceAdmin role when admin is selected", () => {
            component.adminModule = {
                code: Role.WorkspaceAdmin,
                value: "Admin",
            };

            const body = component.getWorkspaceBody();

            expect(body.workspaceId).toBe(1);
            expect(body.users[0].userId).toBe(1);
            expect(body.users[0].roles).toEqual([Role.WorkspaceAdmin]);
        });

        it("should return body with module roles when not admin", () => {
            component.adminModule = { code: "SimpleUser" as Role, value: "User" };
            component.isModule = { code: Role.InventoryRead, value: "Read" };
            component.dsModule = { code: Role.DigitalServiceWrite, value: "Write" };

            const body = component.getWorkspaceBody();

            expect(body.users[0].roles).toContain(Role.InventoryRead);
            expect(body.users[0].roles).toContain(Role.DigitalServiceWrite);
        });

        it("should include ecomind role when present", () => {
            component.adminModule = { code: "SimpleUser" as Role, value: "User" };
            component.ecomindModule = { code: Role.EcoMindAiWrite, value: "Write" };

            const body = component.getWorkspaceBody();

            expect(body.users[0].roles).toContain(Role.EcoMindAiWrite);
        });


    });

    describe("addUpdateWorkspace", () => {
        beforeEach(() => {
            component.ngOnInit();
            component.adminModule = { code: "SimpleUser" as Role, value: "User" };
            component.isModule = { code: Role.InventoryRead, value: "Read" };
            mockUserDataService.fetchUserInfo.and.returnValue(
                of({ email: "test@example.com" } as any),
            );
        });

        it("should call postUserToWorkspaceAndAddRoles with correct body", () => {
            mockAdministrationService.postUserToWorkspaceAndAddRoles.and.returnValue(
                of({} as any),
            );

            component.addUpdateWorkspace();

            expect(
                mockAdministrationService.postUserToWorkspaceAndAddRoles,
            ).toHaveBeenCalled();
            const callArgs =
                mockAdministrationService.postUserToWorkspaceAndAddRoles.calls.argsFor(
                    0,
                )[0];
            expect(callArgs.workspaceId).toBe(1);
            expect(callArgs.users[0].userId).toBe(1);
        });





        it("should fetch user info after posting workspace", () => {
            mockAdministrationService.postUserToWorkspaceAndAddRoles.and.returnValue(
                of({} as any),
            );

            component.addUpdateWorkspace();

            expect(mockUserDataService.fetchUserInfo).toHaveBeenCalled();
        });
    });

    describe("forceAdmin", () => {
        beforeEach(() => {
            component.ngOnInit();
        });

        it("should set all modules to write permissions", () => {
            component.forceAdmin();

            expect(component.dsModule.code).toBe(Role.DigitalServiceWrite);
            expect(component.isModule.code).toBe(Role.InventoryWrite);
            expect(component.ecomindModule.code).toBe(Role.EcoMindAiWrite);
        });

        it("should set adminModule to WorkspaceAdmin", () => {
            component.forceAdmin();

            expect(component.adminModule.code).toBe(Role.WorkspaceAdmin);
            expect(component.adminModule.value).toBe("Admin");
        });

        it("should set isAdmin flag to true", () => {
            component.forceAdmin();

            expect(component.isAdmin).toBe(true);
        });
    });

    describe("validateOnAdmin", () => {
        beforeEach(() => {
            component.ngOnInit();
        });

        it("should call forceAdmin when adminModule is WorkspaceAdmin", () => {
            component.adminModule = {
                code: Role.WorkspaceAdmin,
                value: "Admin",
            };
            spyOn(component, "forceAdmin");

            component.validateOnAdmin();

            expect(component.forceAdmin).toHaveBeenCalled();
        });

        it("should set isAdmin to false when adminModule is not WorkspaceAdmin", () => {
            component.adminModule = { code: "SimpleUser" as Role, value: "User" };
            component.isAdmin = true;

            component.validateOnAdmin();

            expect(component.isAdmin).toBe(false);
        });
    });

    describe("cancel", () => {
        it("should call clearFormData", () => {
            spyOn(component, "clearFormData");

            component.cancel();

            expect(component.clearFormData).toHaveBeenCalled();
        });


    });

    describe("clearFormData", () => {
        beforeEach(() => {
            component.ngOnInit();
            component.isAdmin = true;
            component.adminModule = { code: Role.WorkspaceAdmin, value: "Admin" };
            component.dsModule = { code: Role.DigitalServiceWrite, value: "Write" };
            component.isModule = { code: Role.InventoryWrite, value: "Write" };
            component.ecomindModule = { code: Role.EcoMindAiWrite, value: "Write" };
            component.isAdminRoleDisabled = true;
        });

        it("should reset isAdmin to false", () => {
            component.clearFormData();

            expect(component.isAdmin).toBe(false);
        });

        it("should reset adminModule to empty object", () => {
            component.clearFormData();

            expect(component.adminModule).toEqual({} as any);
        });

        it("should reset dsModule to empty object", () => {
            component.clearFormData();

            expect(component.dsModule).toEqual({} as any);
        });

        it("should reset isModule to empty object", () => {
            component.clearFormData();

            expect(component.isModule).toEqual({} as any);
        });

        it("should reset ecomindModule to empty object", () => {
            component.clearFormData();

            expect(component.ecomindModule).toEqual({} as any);
        });

        it("should reset isAdminRoleDisabled to false", () => {
            component.clearFormData();

            expect(component.isAdminRoleDisabled).toBe(false);
        });
    });

    describe("restrictAdminRoleByDomain", () => {
        beforeEach(() => {
            component.ngOnInit();
        });

        it("should disable admin role when user domain is not in authorized domains", () => {
            component.userDetail = {
                ...mockUser,
                email: "user@unauthorized.com",
            };
            component.workspace = {
                ...mockWorkspace,
                authorizedDomains: ["example.com"],
            };

            component.restrictAdminRoleByDomain();

            expect(component.isAdminRoleDisabled).toBe(true);
        });

        it("should not disable admin role when user domain is in authorized domains", () => {
            component.userDetail = {
                ...mockUser,
                email: "user@example.com",
            };
            component.workspace = {
                ...mockWorkspace,
                authorizedDomains: ["example.com"],
            };

            component.restrictAdminRoleByDomain();

            expect(component.isAdminRoleDisabled).toBe(false);
        });

        it("should filter out WorkspaceAdmin from adminModuleValues when domain is unauthorized", () => {
            component.userDetail = {
                ...mockUser,
                email: "user@unauthorized.com",
            };
            component.workspace = {
                ...mockWorkspace,
                authorizedDomains: ["example.com"],
            };

            component.restrictAdminRoleByDomain();

            const hasWorkspaceAdmin = component.adminModuleValues.some(
                (role) => role.code === Role.WorkspaceAdmin,
            );
            expect(hasWorkspaceAdmin).toBe(false);
        });

        it("should not filter adminModuleValues when domain is authorized", () => {
            component.userDetail = {
                ...mockUser,
                email: "user@example.com",
            };
            component.workspace = {
                ...mockWorkspace,
                authorizedDomains: ["example.com"],
            };

            component.restrictAdminRoleByDomain();

            const hasWorkspaceAdmin = component.adminModuleValues.some(
                (role) => role.code === Role.WorkspaceAdmin,
            );
            expect(hasWorkspaceAdmin).toBe(true);
        });

        it("should not restrict when authorizedDomains is undefined", () => {
            component.workspace = {
                ...mockWorkspace,
                authorizedDomains: undefined,
            };

            component.restrictAdminRoleByDomain();

            expect(component.isAdminRoleDisabled).toBe(false);
        });
    });

    describe("Inputs and Outputs", () => {
        it("should have userDetail input", () => {
            const testUser = { ...mockUser };
            component.userDetail = testUser;

            expect(component.userDetail).toBe(testUser);
        });

        it("should have workspace input", () => {
            const testWorkspace = { ...mockWorkspace };
            component.workspace = testWorkspace;

            expect(component.workspace).toBe(testWorkspace);
        });

        it("should have userDetailEcoMind input with default value false", () => {
            expect(component.userDetailEcoMind).toBe(false);
        });

        it("should have updateOrganizationEnable input with default value false", () => {
            expect(component.updateOrganizationEnable).toBe(false);
        });


    });

    describe("Role Arrays", () => {
        it("should have correct dsRoles", () => {
            expect(component.dsRoles).toEqual([
                Role.DigitalServiceRead,
                Role.DigitalServiceWrite,
            ]);
        });

        it("should have correct isRoles", () => {
            expect(component.isRoles).toEqual([Role.InventoryRead, Role.InventoryWrite]);
        });

        it("should have correct ecomindRoles", () => {
            expect(component.ecomindRoles).toEqual([
                Role.EcoMindAiRead,
                Role.EcoMindAiWrite,
            ]);
        });
    });
});
