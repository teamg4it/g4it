import { HttpClientTestingModule } from "@angular/common/http/testing";
import { TestBed, fakeAsync, tick } from "@angular/core/testing";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateLoader, TranslateModule, TranslateService } from "@ngx-translate/core";
import { BehaviorSubject, of } from "rxjs";
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
            currentSubscriber$: new BehaviorSubject({ name: "TestSubscriber" }),
            currentOrganization$: new BehaviorSubject({ id: "123" }),
            isAllowedInventoryRead$: of(true),
            isAllowedDigitalServiceRead$: of(false),
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
        userServiceMock.currentSubscriber$.complete();
        userServiceMock.currentOrganization$.complete();
    });

    it("should set userName on ngOnInit", fakeAsync(() => {
        component.ngOnInit();
        tick(); // Simulate passage of time for async operations
        expect(component.userName).toBe("John Doe");
    }));

    it("should update currentSubscriber when currentSubscriber$ emits", fakeAsync(() => {
        const subscriberMock = { name: "UpdatedSubscriber" };
        userServiceMock.currentSubscriber$.next(subscriberMock);
        tick();
        fixture.detectChanges();
        expect(component.currentSubscriber).toEqual(subscriberMock);
    }));

    it("should update selectedPath when currentOrganization$ emits", fakeAsync(() => {
        const organizationMock = { id: "456" };
        userServiceMock.currentOrganization$.next(organizationMock);
        tick();
        fixture.detectChanges();
        expect(component.selectedPath).toBe(
            "/subscribers/TestSubscriber/organizations/456",
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
});
