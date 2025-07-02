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
    let mockTranslateService: any;
    let mockUserService: any;
    let mockWorkspaceService: any;
    let mockKeycloakService: any;
    let mockGlobalStore: any;
    const org: OrganizationData = {
        id: 1,
        name: "Org1",
        subscriber: {
            id: 1,
            name: "Demo",
            defaultFlag: true,
            organizations: [
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
        organization: {
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

        mockTranslateService = {
            currentLang: "en",
            use: jasmine.createSpy("use"),
        };

        mockUserService = {
            currentSubscriber$: of({ name: "subscriber1" }),
            user$: of({
                firstName: "John",
                lastName: "Doe",
                email: "john@example.com",
                subscribers: [
                    {
                        name: "subscriber1",
                        organizations: [{ id: 1, name: "Org1" }],
                    },
                ],
            }),
            currentOrganization$: of({ id: 1, name: "Org1" }),
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
        component.organizations = [org];
        component.modelOrganization = 1;

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

        expect(component.modelOrganization).toBe(1);
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

        // ✅ Call command with a dummy event object
        boaviztapiItem?.command?.({});

        expect(windowOpenSpy).toHaveBeenCalledWith(
            "https://github.com/Boavizta/boaviztapi",
            "_blank",
            "noopener",
        );
    });
});
