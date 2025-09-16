import { HttpClientTestingModule } from "@angular/common/http/testing";
import { TestBed, fakeAsync, tick } from "@angular/core/testing";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateLoader, TranslateModule, TranslateService } from "@ngx-translate/core";
import { BehaviorSubject, of } from "rxjs";
import { Organization } from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { WorkspaceService } from "src/app/core/service/business/workspace.service";
import { WelcomePageComponent } from "./welcome-page.component";

describe("WelcomePageComponent", () => {
    let component: WelcomePageComponent;
    let fixture: any;
    let userServiceMock: any;
    let workspaceServiceMock: any;

    beforeEach(async () => {
        // Mock UserService with BehaviorSubjects to simulate async data
        userServiceMock = {
            user$: of({ firstName: "John", lastName: "Doe" }),
            currentOrganization$: new BehaviorSubject({ name: "TestOrganization" }),
            currentWorkspace$: new BehaviorSubject({ id: "123" }),
            isAllowedInventoryRead$: of(true),
            isAllowedDigitalServiceRead$: of(false),
            isAllowedEcoMindAiRead$: of(false),
        };

        // Mock WorkspaceService
        workspaceServiceMock = {
            setOpen: jasmine.createSpy("setOpen"),
        };

        await TestBed.configureTestingModule({
            imports: [
                HttpClientTestingModule,
                RouterTestingModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useValue: {
                            getTranslation: () => of({}),
                        },
                    },
                }),
            ],
            providers: [
                TranslateService,
                { provide: UserService, useValue: userServiceMock },
                { provide: WorkspaceService, useValue: workspaceServiceMock },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(WelcomePageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges(); // Trigger initial data binding
    });

    afterEach(() => {
        // Complete all BehaviorSubjects to ensure proper cleanup
        userServiceMock.currentOrganization$.complete();
        userServiceMock.currentWorkspace$.complete();
    });

    it("should set userName on ngOnInit", fakeAsync(() => {
        component.ngOnInit();
        tick(); // Simulate passage of time for async operations
        expect(component.userName).toBe("John Doe");
    }));

    it("should update currentOrganization when currentOrganization$ emits", fakeAsync(() => {
        const organizationMock = {
            name: "UpdatedOrganization",
            ecomindai: true,
        } as Organization;
        userServiceMock.currentOrganization$.next(organizationMock);
        tick();
        fixture.detectChanges();
        expect(component.currentOrganization).toEqual(organizationMock);
    }));

    it("should update selectedPath when currentWorkspace$ emits", fakeAsync(() => {
        const organizationMock = { id: "456" };
        userServiceMock.currentWorkspace$.next(organizationMock);
        tick();
        fixture.detectChanges();
        expect(component.selectedPath).toBe(
            "/organizations/TestOrganization/workspaces/456",
        );
    }));

    it("should call workspaceService.setOpen(true) when openWorkspaceSidebar is called", () => {
        component.openWorkspaceSidebar();
        expect(workspaceServiceMock.setOpen).toHaveBeenCalledWith(true);
    });

    it("should render the inventories button as enabled", () => {
        const compiled = fixture.nativeElement;
        const inventoriesButton = compiled.querySelector(".inventories-button");
        expect(inventoriesButton.classList.contains("disabled")).toBeFalse();
    });

    it("should render the digital services button as disabled", () => {
        const compiled = fixture.nativeElement;
        const digitalServicesButton = compiled.querySelector(".digital-service-button");
        expect(digitalServicesButton.classList.contains("disabled")).toBeTrue();
    });

    it("should not render the AI button when condition is false", () => {
        const compiled = fixture.nativeElement;
        const ecoMindAiButton = compiled.querySelector(".eco-mind-ai-button");
        expect(ecoMindAiButton).toBeNull();
    });
});
