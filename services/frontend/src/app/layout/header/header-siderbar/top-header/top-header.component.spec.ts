import { TestBed } from "@angular/core/testing";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { KeycloakService } from "keycloak-angular";
import { Subject } from "rxjs";
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
                {
                    provide: KeycloakService,
                    useValue: jasmine.createSpyObj("KeycloakService", [
                        "method1",
                        "method2",
                    ]),
                },
            ],
        });

        const fixture = TestBed.createComponent(TopHeaderComponent);
        component = fixture.componentInstance;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
