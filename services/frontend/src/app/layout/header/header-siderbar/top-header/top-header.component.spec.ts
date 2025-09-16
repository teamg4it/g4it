import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { ComponentFixture, fakeAsync, TestBed, tick } from "@angular/core/testing";
import { NavigationEnd, Router } from "@angular/router";
import { TranslateModule } from "@ngx-translate/core";
import { KeycloakService } from "keycloak-angular";
import { of } from "rxjs";
import { OrganizationData } from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { WorkspaceService } from "src/app/core/service/business/workspace.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { TopHeaderComponent } from "./top-header.component";

describe("TopHeaderComponent", () => {
    let component: TopHeaderComponent;
    let fixture: ComponentFixture<TopHeaderComponent>;
    let mockRouter: any;
    let routerSpy: any;
    let mockTranslateService: any;
    let mockUserService: any;
    let mockWorkspaceService: any;
    let mockKeycloakService: any;
    let mockGlobalStore: any;
    const org: OrganizationData = {
        id: 1,
        name: "Org1",
        organization: {
            id: 1,
            name: "Demo",
            defaultFlag: true,
            workspaces: [
                {
                    id: 1,
                    name: "Organization 1",
                    defaultFlag: true,
                    status: "",
                    roles: [],
                },
            ],
            roles: [],
            ecomindai: false,
        },
        workspace: {
            id: 1,
            name: "Organization 1",
            defaultFlag: true,
            status: "",
            roles: [],
        },
        color: "#FFFFFF",
    };
    beforeEach(async () => {
        mockRouter = {
            events: of(new NavigationEnd(0, "/path", "/path")),
            navigate: jasmine.createSpy("navigate"),
        };
        routerSpy = mockRouter;

        mockTranslateService = {
            currentLang: "en",
            use: jasmine.createSpy("use"),
        };

        mockUserService = {
            currentOrganization$: of({ name: "organization1" }),
            user$: of({
                firstName: "John",
                lastName: "Doe",
                email: "john@example.com",
                organizations: [
                    {
                        name: "organization1",
                        workspaces: [{ id: 1, name: "Org1" }],
                    },
                ],
            }),
            currentWorkspace$: of({ id: 1, name: "Org1" }),
            ecoDesignPercent: 70,
            getSelectedPage: () => "dashboard",
            checkAndRedirect: jasmine.createSpy("checkAndRedirect"),
        };

        mockKeycloakService = {
            logout: jasmine.createSpy("logout").and.returnValue(Promise.resolve()),
        };

        mockWorkspaceService = {
            setOpen: jasmine.createSpy("setOpen"),
        };

        mockGlobalStore = {
            zoomLevel: jasmine.createSpy("zoomLevel").and.returnValue(100),
            mobileView: jasmine.createSpy("mobileView").and.returnValue(false),
        };

        await TestBed.configureTestingModule({
            imports: [TopHeaderComponent, SharedModule, TranslateModule.forRoot()],
            providers: [
                { provide: Router, useValue: mockRouter },
                { provide: KeycloakService, useValue: mockKeycloakService },
                { provide: UserService, useValue: mockUserService },
                { provide: WorkspaceService, useValue: mockWorkspaceService },
                { provide: GlobalStoreService, useValue: mockGlobalStore },
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).compileComponents();

        fixture = TestBed.createComponent(TopHeaderComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should generate initials after user data is set", () => {
        expect(component.initials).toBe("JD");
    });

    it("should open workspace sidebar", () => {
        component.openWorkspaceSidebar();
        expect(mockWorkspaceService.setOpen).toHaveBeenCalledWith(true);
    });

    it("should return capital letter of a string", () => {
        const capital = component.getCapitaleLetter("hello");
        expect(capital).toBe("H");
    });

    it("should handle language navigation keydown event", () => {
        const event = new KeyboardEvent("keydown", { key: "ArrowRight" });
        spyOn(event, "preventDefault");
        component.handleKeydownLanguage(event);
        expect(component.selectedLanguage).toBe("en");
    });

    it("should call checkAndRedirect on selectCompany", () => {
        component.selectCompany(org);
        expect(mockUserService.checkAndRedirect).toHaveBeenCalled();
    });

    it("should navigate to next organization on ArrowDown", fakeAsync(() => {
        component.workspaces = [org];
        component.modelWorkspace = 1;

        const mockScrollIntoView = jasmine.createSpy("scrollIntoView");

        // Mock ViewChildren (radioItems)
        component.radioItems = {
            toArray: () => [
                { nativeElement: { scrollIntoView: mockScrollIntoView } },
                { nativeElement: { scrollIntoView: mockScrollIntoView } },
            ],
        } as any;

        const event = new KeyboardEvent("keydown", { key: "ArrowDown" });
        spyOn(event, "preventDefault");

        component.handleKeydown(event);
        tick();

        expect(component.modelWorkspace).toBe(1);
    }));

    it("should open Boaviztapi GitHub link when help menu item is clicked", () => {
        const windowOpenSpy = spyOn(window, "open");

        // Trigger ngOnInit to populate items
        component.ngOnInit();

        const helpMenu = component.items?.find(
            (item) => item.label === "common.help-center",
        );
        const boaviztapiItem = helpMenu?.items?.find(
            (item) => item["link"] === "https://github.com/Boavizta/boaviztapi",
        );

        // Call command with a dummy event object
        boaviztapiItem?.command?.({});

        expect(windowOpenSpy).toHaveBeenCalledWith(
            "https://github.com/Boavizta/boaviztapi",
            "_blank",
            "noopener",
        );
    });

    it("should toggle organization menu visibility", () => {
        component.isOrgMenuVisible = false;
        component.toggleOrgMenu();
        expect(component.isOrgMenuVisible).toBeTrue();
    });

    it("should toggle organization menu visibility", () => {
        component.isOrgMenuVisible = true;
        component.toggleOrgMenu();
        expect(component.isOrgMenuVisible).toBeFalse();
    });

    it("should hide menus when clicking outside", () => {
        component.isAccountMenuVisible = true;
        component.isOrgMenuVisible = true;

        const event = new MouseEvent("click");

        component.handleGlobalClick(event);
        expect(component.isAccountMenuVisible).toBeFalse();
        expect(component.isOrgMenuVisible).toBeFalse();
    });

    it("should show dialog", () => {
        component.dialogVisible = false;
        component.showDialog();
        expect(component.dialogVisible).toBeTrue();
    });

    it("should call setSelectedPage on ngOnInit and NavigationEnd", () => {
        const spySetSelectedPage = spyOn(component, "setSelectedPage");
        component.ngOnInit();
        expect(spySetSelectedPage).toHaveBeenCalled();
    });

    it("should subscribe to userService.currentOrganization$ and set currentorganization", () => {
        component.currentOrganization = {} as any;
        component.ngOnInit();
        expect(component.currentOrganization.name).toBe("organization1");
    });

    it("should populate userDetails and workspaces after user$ emits", () => {
        component.userDetails = {} as any;
        component.workspaces = [];
        component.ngOnInit();
        expect(component.userDetails.firstName).toBe("John");
        expect(component.workspaces.length).toBeGreaterThan(0);
        expect(component.workspaces[0].name).toBe("Org1");
    });

    it("should subscribe to currentWorkspace$ and set selectedOrganization, modelOrganization, selectedOrganizationData, selectedPath", () => {
        component.selectedWorkspace = {} as any;
        component.modelWorkspace = 0;
        component.selectedOrganizationData = undefined;
        component.selectedPath = "";
        component.currentOrganization = { name: "organization1" } as any;
        component.ngOnInit();
        expect(component.selectedWorkspace.id).toBe(1);
        expect(component.modelWorkspace).toBe(1);
        expect(component.selectedPath).toBe("/organizations/organization1/workspaces/1");
    });

    it("should set initials after user$ emits", () => {
        component.initials = "";
        component.ngOnInit();
        expect(component.initials).toBe("JD");
    });

    it("should set mobileMenuItems with correct structure", () => {
        component.mobileMenuItems = [];
        component.ngOnInit();
        expect(component.mobileMenuItems?.length).toBe(2);
        expect(component.mobileMenuItems?.[0].label).toBe("common.about");
        expect(component.mobileMenuItems?.[1].label).toBe("common.help-center");
    });

    it("should set items with help-center and correct links", () => {
        component.items = [];
        component.ngOnInit();
        const helpCenter = component.items?.find((i) => i.label === "common.help-center");
        expect(helpCenter).toBeDefined();
        const githubItem = helpCenter?.items?.find(
            (i) => i["link"] === "https://github.com/G4ITTeam/g4it",
        );
        expect(githubItem).toBeDefined();
        const boaviztapiItem = helpCenter?.items?.find(
            (i) => i["link"] === "https://github.com/Boavizta/boaviztapi",
        );
        expect(boaviztapiItem).toBeDefined();
    });

    it("should initialize mobileMenuItems with correct structure", () => {
        expect(component.mobileMenuItems).toBeDefined();
        expect(component.mobileMenuItems?.length).toBe(2);
        expect(component.mobileMenuItems?.[0].label).toBe("common.about");
        expect(component.mobileMenuItems?.[1].label).toBe("common.help-center");
    });

    it("should navigate to /useful-information and close dialog when command is called", () => {
        const item = component.mobileMenuItems?.[0].items?.[0];
        if (item?.command) {
            item.command({} as any);
        }
        expect(routerSpy.navigate).toHaveBeenCalledWith(["/useful-information"]);
        expect(component.dialogVisible).toBe(false);
    });

    it("should navigate to /declarations and close dialog when command is called", () => {
        const item = component.mobileMenuItems?.[0].items?.[1];
        if (item?.command) {
            item.command({} as any);
        }
        expect(routerSpy.navigate).toHaveBeenCalledWith(["/declarations"]);
        expect(component.dialogVisible).toBe(false);
    });

    it("should open github link in new tab when command is called", () => {
        spyOn(window, "open");
        const item = component.mobileMenuItems?.[1].items?.[0];
        if (item?.command) {
            item.command({} as any);
        }
        expect(window.open).toHaveBeenCalledWith(
            "https://github.com/G4ITTeam/g4it",
            "_blank",
            "noopener",
        );
    });

    it("should open documentation link in new tab when command is called", () => {
        spyOn(window, "open");
        const item = component.mobileMenuItems?.[1].items?.[1];
        if (item?.command) {
            item.command({} as any);
        }
        expect(window.open).toHaveBeenCalledWith(
            "https://saas-g4it.com/documentation/",
            "_blank",
            "noopener",
        );
    });

    it("should open boaviztapi github link in new tab when command is called", () => {
        spyOn(window, "open");
        const item = component.mobileMenuItems?.[1].items?.[2];
        if (item?.command) {
            item.command({} as any);
        }
        expect(window.open).toHaveBeenCalledWith(
            "https://github.com/Boavizta/boaviztapi",
            "_blank",
            "noopener",
        );
    });
});
