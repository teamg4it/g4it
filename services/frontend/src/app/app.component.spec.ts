import { HttpClientTestingModule } from "@angular/common/http/testing";
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { ComponentFixture, fakeAsync, TestBed } from "@angular/core/testing";
import { Title } from "@angular/platform-browser";
import { ActivatedRoute, NavigationEnd, Router } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { KeycloakService } from "keycloak-angular";
import { of, Subject } from "rxjs";
import { environment } from "src/environments/environment";
import { AppComponent } from "./app.component";
import { MatomoScriptService } from "./core/service/business/matomo-script.service";
import { UserDataService } from "./core/service/data/user-data.service";
import { GlobalStoreService } from "./core/store/global.store";
describe("AppComponent", () => {
    let component: AppComponent;
    let fixture: ComponentFixture<AppComponent>;
    let mockKeycloak: any;
    let mockUserService: any;
    let mockTranslate: any;
    let mockGlobalStore: any;
    let mockRouter: any;
    let mockTitle: any;
    let mockMatomo: any;

    beforeEach(async () => {
        mockKeycloak = {
            getToken: jasmine.createSpy().and.returnValue(Promise.resolve("mock-token")),
            login: jasmine.createSpy(),
        };

        mockUserService = {
            fetchUserInfo: jasmine
                .createSpy()
                .and.returnValue(of({ email: "test@example.com" })),
        };

        mockTranslate = {
            currentLang: "en",
            translations: {
                en: {
                    criteria: ["criteria1", "criteria2"],
                },
            },
        };

        mockGlobalStore = {
            setcriteriaList: jasmine.createSpy(),
            setZoomLevel: jasmine.createSpy(),
            setIsMobile: jasmine.createSpy(),
        };

        mockRouter = {
            events: new Subject<NavigationEnd>(),
            navigate: jasmine.createSpy(),
        };

        mockTitle = {
            setTitle: jasmine.createSpy(),
        };

        mockMatomo = {
            appendScriptToHead: jasmine.createSpy(),
        };

        await TestBed.configureTestingModule({
            declarations: [AppComponent],
            imports: [HttpClientTestingModule, TranslateModule.forRoot()],
            providers: [
                { provide: KeycloakService, useValue: mockKeycloak },
                { provide: UserDataService, useValue: mockUserService },
                { provide: TranslateService, useValue: mockTranslate },
                { provide: GlobalStoreService, useValue: mockGlobalStore },
                { provide: Router, useValue: mockRouter },
                { provide: ActivatedRoute, useValue: { firstChild: null } },
                { provide: Title, useValue: mockTitle },
                { provide: MatomoScriptService, useValue: mockMatomo },
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).compileComponents();

        fixture = TestBed.createComponent(AppComponent);
        component = fixture.componentInstance;
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should store user email in localStorage and set criteria (async)", fakeAsync(async () => {
        spyOn(localStorage, "setItem");

        component.ngOnInit();

        // Resolve pending microtasks (e.g., Promises in ngOnInit)
        await component["initializeAsync"]();

        expect(mockUserService.fetchUserInfo).toHaveBeenCalled();

        expect(localStorage.setItem).toHaveBeenCalledWith("username", "test@example.com");

        expect(mockGlobalStore.setcriteriaList).toHaveBeenCalledWith([
            "criteria1",
            "criteria2",
        ]);
    }));

    it("should append Matomo script if URL is present", async () => {
        const mockUrl = "https://tagmanager.matomo.example";
        environment.matomo = {
            matomoTagManager: { containerUrl: mockUrl },
        };

        component.ngOnInit();
        await component["initializeAsync"]();

        expect(mockMatomo.appendScriptToHead).toHaveBeenCalledWith(mockUrl);
    });

    it("should detect zoom level and screen size in checkZoom()", () => {
        spyOnProperty(window, "devicePixelRatio").and.returnValue(2); // zoomed in
        spyOnProperty(window, "innerWidth").and.returnValue(600); // mobile

        component.checkZoom();

        expect(component.isZoomedIn).toBeTrue();
        expect(mockGlobalStore.setZoomLevel).toHaveBeenCalled();
        expect(mockGlobalStore.setIsMobile).toHaveBeenCalledWith(true);
    });

    it("should call checkZoom on window resize", () => {
        spyOn(component, "checkZoom");
        component.onResize();
        expect(component.checkZoom).toHaveBeenCalled();
    });
});
