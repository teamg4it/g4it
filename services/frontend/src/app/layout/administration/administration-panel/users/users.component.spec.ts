/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormBuilder, ReactiveFormsModule } from "@angular/forms";
import { Router } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { of, Subject } from "rxjs";
import {
    Organization as AdminOrganization,
    WorkspaceCriteriaRest,
    WorkspaceWithOrganization,
} from "src/app/core/interfaces/administration.interfaces";
import { Role } from "src/app/core/interfaces/roles.interfaces";
import { Organization, UserDetails } from "src/app/core/interfaces/user.interfaces";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { UserService } from "src/app/core/service/business/user.service";
import { UserDataService } from "src/app/core/service/data/user-data.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { UsersComponent } from "./users.component";

describe("UsersComponent", () => {
    let component: UsersComponent;
    let fixture: ComponentFixture<UsersComponent>;
    let mockAdministrationService: jasmine.SpyObj<AdministrationService>;
    let mockUserService: jasmine.SpyObj<UserService>;
    let mockUserDataService: jasmine.SpyObj<UserDataService>;
    let mockGlobalStore: jasmine.SpyObj<GlobalStoreService>;
    let mockRouter: jasmine.SpyObj<Router>;
    let mockTranslateService: jasmine.SpyObj<TranslateService>;
    let mockConfirmationService: jasmine.SpyObj<ConfirmationService>;

    const mockUser = {
        id: 1,
        email: "test@example.com",
        firstName: "Test",
        lastName: "User",
        roles: [Role.InventoryRead],
        isSuperAdmin: false,
        organizations: [
            {
                id: 1,
                name: "Test Org",
                workspaces: [
                    {
                        id: 1,
                        name: "Test Workspace",
                        roles: [Role.InventoryRead],
                    },
                ],
            },
        ],
    } as UserDetails;

    const mockWorkspace: WorkspaceWithOrganization = {
        workspaceId: 1,
        workspaceName: "Test Workspace",
        organizationId: 1,
        organizationName: "Test Org",
        authorizedDomains: ["example.com"],
        criteriaDs: ["criteria1"],
        criteriaIs: ["criteria2"],
    } as WorkspaceWithOrganization;

    const mockOrganization: Organization = {
        id: 1,
        name: "Test Org",
        criteria: ["criteria1", "criteria2"],
        ecomindai: true,
    } as Organization;

    const mockAdminOrganization: AdminOrganization = {
        id: 1,
        name: "Test Org",
        defaultFlag: false,
        workspaces: [],
        roles: [],
        criteria: ["criteria1", "criteria2"],
        authorizedDomains: ["example.com"],
        ecomindai: true,
    };

    beforeEach(async () => {
        const getUsersTriggeredSubject = new Subject<boolean>();

        mockAdministrationService = jasmine.createSpyObj(
            "AdministrationService",
            [
                "getAdminWorkspaceList",
                "getUserDetails",
                "getSearchDetails",
                "deleteUserDetails",
                "updateWorkspaceCriteria",
                "getOrganizationById",
                "getUsersTriggered",
            ],
            {
                getUsersTriggered: jasmine
                    .createSpy()
                    .and.returnValue(() => getUsersTriggeredSubject.asObservable()),
            },
        );

        mockUserService = jasmine.createSpyObj(
            "UserService",
            [],
            {
                user$: of(mockUser),
                currentOrganization$: of(mockOrganization),
            },
        );

        mockUserDataService = jasmine.createSpyObj("UserDataService", ["fetchUserInfo"]);
        mockGlobalStore = jasmine.createSpyObj("GlobalStoreService", ["criteriaList"]);
        mockRouter = jasmine.createSpyObj("Router", ["navigateByUrl"]);
        mockTranslateService = jasmine.createSpyObj("TranslateService", [
            "instant",
            "get",
            "use",
            "setDefaultLang",
        ]);
        mockConfirmationService = jasmine.createSpyObj("ConfirmationService", [
            "confirm",
        ]);

        mockTranslateService.instant.and.callFake((key: string, params?: any) => {
            if (params) {
                return `${key} ${params.FirstName} ${params.LastName}`;
            }
            return key;
        });

        mockTranslateService.get.and.callFake((key: string | string[]) => {
            if (typeof key === 'string') {
                return of(key);
            }
            return of(key);
        });

        mockTranslateService.use.and.returnValue(of({}));
        mockTranslateService.setDefaultLang.and.stub();

        // Add properties that TranslatePipe needs
        (mockTranslateService as any).currentLang = 'en';
        (mockTranslateService as any).onLangChange = new Subject();
        (mockTranslateService as any).onTranslationChange = new Subject();
        (mockTranslateService as any).onDefaultLangChange = new Subject();

        mockGlobalStore.criteriaList.and.returnValue({
            criteria1: { label: "Criteria 1", unit: "unit", impacts: [] },
            criteria2: { label: "Criteria 2", unit: "unit", impacts: [] },
            criteria3: { label: "Criteria 3", unit: "unit", impacts: [] },
            criteria4: { label: "Criteria 4", unit: "unit", impacts: [] },
            criteria5: { label: "Criteria 5", unit: "unit", impacts: [] },
        } as any);

        mockAdministrationService.getAdminWorkspaceList.and.returnValue(
            of([mockWorkspace]),
        );
        mockAdministrationService.getUsersTriggered.and.returnValue(false);
        mockUserDataService.fetchUserInfo.and.returnValue(of(mockUser as any));

        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot(), ReactiveFormsModule, UsersComponent],
            providers: [
                FormBuilder,
                MessageService,
                { provide: AdministrationService, useValue: mockAdministrationService },
                { provide: UserService, useValue: mockUserService },
                { provide: UserDataService, useValue: mockUserDataService },
                { provide: GlobalStoreService, useValue: mockGlobalStore },
                { provide: Router, useValue: mockRouter },
                { provide: TranslateService, useValue: mockTranslateService },
                { provide: ConfirmationService, useValue: mockConfirmationService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(UsersComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    describe("ngOnInit", () => {
        it("should call getUsers and initialize searchForm", () => {
            spyOn(component, "getUsers");
            component.ngOnInit();

            expect(component.getUsers).toHaveBeenCalled();
            expect(component.searchForm).toBeDefined();
            expect(component.organization).toEqual(mockOrganization);
        });
    });

    describe("getUsers", () => {
        it("should fetch workspace list and update workspace", () => {
            component.workspace = mockWorkspace;
            const updatedWorkspace = {
                ...mockWorkspace,
                workspaceName: "Updated Workspace",
            };
            mockAdministrationService.getAdminWorkspaceList.and.returnValue(
                of([updatedWorkspace]),
            );

            component.getUsers(true);

            expect(mockAdministrationService.getAdminWorkspaceList).toHaveBeenCalled();
            expect(component.workspace.workspaceName).toBe("Updated Workspace");
        });
    });

    describe("searchList", () => {
        beforeEach(() => {
            component.ngOnInit();
            component.workspace = mockWorkspace;
        });

        it("should call getUsersDetails when search is empty", () => {
            spyOn(component, "getUsersDetails");
            component.searchForm.controls["searchName"].setValue("   ");

            component.searchList();

            expect(component.getUsersDetails).toHaveBeenCalled();
        });

        it("should call getSearchDetails and set filteredMembers", () => {
            const searchValue = "test";
            component.searchForm.controls["searchName"].setValue(searchValue);
            mockAdministrationService.getSearchDetails.and.returnValue(
                of([{ id: 1, email: "test@test.com", roles: [Role.InventoryRead] }]),
            );

            component.searchList();

            expect(mockAdministrationService.getSearchDetails).toHaveBeenCalled();
            expect(component.filteredMembers).toHaveSize(1);
        });
    });

    describe("getUsersDetails", () => {
        it("should fetch user details and set membersList", () => {
            component.workspace = mockWorkspace;
            const mockUsers = [{ id: 1, email: "test@test.com", roles: [Role.InventoryRead] }];
            mockAdministrationService.getUserDetails.and.returnValue(of(mockUsers));

            component.getUsersDetails();

            expect(mockAdministrationService.getUserDetails).toHaveBeenCalled();
            expect(component.membersList).toHaveSize(1);
        });
    });

    describe("isAdmin", () => {
        it("should return true for admin roles", () => {
            expect(component.isAdmin([Role.WorkspaceAdmin])).toBe(true);
            expect(component.isAdmin([Role.OrganizationAdmin])).toBe(true);
        });

        it("should return false for non-admin roles", () => {
            expect(component.isAdmin([Role.InventoryRead])).toBe(false);
        });
    });

    describe("getRole", () => {
        it("should return correct role for module types", () => {
            expect(component.getRole([Role.EcoMindAiWrite], "ECO_MIND_AI_")).toBe("administration.role.write");
            expect(component.getRole([Role.EcoMindAiRead], "ECO_MIND_AI_")).toBe("administration.role.read");
        });

        it("should return admin or user for ADMINISTRATOR type", () => {
            expect(component.getRole([Role.WorkspaceAdmin], "ADMINISTRATOR")).toBe("administration.role.admin");
            expect(component.getRole([Role.InventoryRead], "ADMINISTRATOR")).toBe("administration.role.user");
        });
    });



    describe("handleAcceptEvent", () => {
        it("should call deleteUserDetails and searchList", () => {
            component.workspace = mockWorkspace;
            spyOn(component, "searchList");
            mockAdministrationService.deleteUserDetails.and.returnValue(of({}));
            const user = { ...mockUser, roles: [Role.InventoryRead] };

            component.handleAcceptEvent(user, 2);

            expect(mockAdministrationService.deleteUserDetails).toHaveBeenCalled();
            expect(component.searchList).toHaveBeenCalled();
        });
    });

    describe("openSidepanelForAddORUpdateOrg", () => {
        it("should set sidebar visibility and user details", () => {
            component.sidebarVisible = false;

            component.openSidepanelForAddORUpdateOrg(mockUser, false);

            expect(component.sidebarVisible).toBe(true);
            expect(component.userDetail).toEqual(mockUser);
        });
    });

    describe("displayPopupFct", () => {
        it("should set displayPopup and criteria", () => {
            component.workspace = mockWorkspace;
            component.organization = mockOrganization;
            component.displayPopup = false;

            component.displayPopupFct();

            expect(component.displayPopup).toBe(true);
            expect(component.selectedCriteriaDS).toEqual(["criteria1"]);
        });
    });

    describe("handleSaveWorkspace", () => {
        it("should update workspace criteria", () => {
            component.workspace = mockWorkspace;
            spyOn(component, "getUsers");
            const criteria: WorkspaceCriteriaRest = {
                organizationId: 1,
                name: "Test Workspace",
                status: "ACTIVE",
                dataRetentionDays: 30,
                criteriaDs: ["criteria1"],
                criteriaIs: ["criteria2"],
            };
            mockAdministrationService.updateWorkspaceCriteria.and.returnValue(of(criteria));

            component.handleSaveWorkspace(criteria);

            expect(mockAdministrationService.updateWorkspaceCriteria).toHaveBeenCalled();
            expect(component.displayPopup).toBe(false);
        });
    });

    describe("getSelectedOrganization", () => {
        it("should fetch organization and set ecomind status", () => {
            component.workspace = mockWorkspace;
            const orgWithEcomind = { ...mockAdminOrganization, ecomindai: true };
            mockAdministrationService.getOrganizationById.and.returnValue(of(orgWithEcomind));

            component.getSelectedOrganization();

            expect(mockAdministrationService.getOrganizationById).toHaveBeenCalled();
            expect(component.isEcoMindEnabledForCurrentOrganizationSelected).toBe(true);
        });
    });
});
