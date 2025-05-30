import { TestBed } from "@angular/core/testing";
import { NavigationEnd, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { of, Subject } from "rxjs";
import { Subscriber } from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { WorkspaceService } from "src/app/core/service/business/workspace.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { TopHeaderComponent } from "./top-header.component";

describe("TopHeaderComponent", () => {
    let component: TopHeaderComponent;
    let translateService: jasmine.SpyObj<TranslateService>;
    let router: jasmine.SpyObj<Router>;
    let userService: jasmine.SpyObj<UserService>;
    let workspaceService: jasmine.SpyObj<WorkspaceService>;
    let globalStoreService: jasmine.SpyObj<GlobalStoreService>;
    let routerEvents$: Subject<any>;

    beforeEach(() => {
        routerEvents$ = new Subject<any>();

        translateService = jasmine.createSpyObj("TranslateService", [
            "currentLang",
            "use",
        ]);
        router = jasmine.createSpyObj("Router", ["navigate"], {
            events: routerEvents$.asObservable(),
        });
        userService = jasmine.createSpyObj("UserService", [
            "currentSubscriber$",
            "user$",
            "currentOrganization$",
            "getSelectedPage",
        ]);
        workspaceService = jasmine.createSpyObj("WorkspaceService", ["setOpen"]);
        globalStoreService = jasmine.createSpyObj("GlobalStoreService", ["zoomLevel"]);

        TestBed.configureTestingModule({
            providers: [
                { provide: TranslateService, useValue: translateService },
                { provide: Router, useValue: router },
                { provide: UserService, useValue: userService },
                { provide: WorkspaceService, useValue: workspaceService },
                { provide: GlobalStoreService, useValue: globalStoreService },
            ],
        });

        component = new TopHeaderComponent(workspaceService);
        component["userService"] = userService;
        component["globalStore"] = globalStoreService;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize selectedLanguage and setSelectedPage", () => {
        translateService.currentLang = "en";
        spyOn(component, "setSelectedPage");

        component.ngOnInit();

        expect(component.selectedLanguage).toBe("en");
        expect(component.setSelectedPage).toHaveBeenCalled();
    });

    it("should subscribe to router events and call setSelectedPage on NavigationEnd", () => {
        spyOn(component, "setSelectedPage");

        component.ngOnInit();
        routerEvents$.next(new NavigationEnd(0, "", ""));

        expect(component.setSelectedPage).toHaveBeenCalled();
    });

    it("should subscribe to currentSubscriber$ and update currentSubscriber", () => {
        const mockSubscriber: Subscriber = {
            name: "Test Subscriber",
            organizations: [],
        } as any;
        userService.currentSubscriber$ = of(mockSubscriber);

        component.ngOnInit();

        expect(component.currentSubscriber).toEqual(mockSubscriber);
    });
});
