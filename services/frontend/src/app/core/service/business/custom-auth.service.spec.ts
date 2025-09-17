import { TestBed } from "@angular/core/testing";
import { Router, NavigationEnd } from "@angular/router";
import { Subject } from "rxjs";
import { CustomAuthService } from "./custom-auth.service";
import { KeycloakService } from "keycloak-angular";
import { DigitalServiceStoreService } from "../../store/digital-service.store";
import { Constants } from "src/constants";
import { environment } from "src/environments/environment";

// Simple mock store
const mockStore = {
    setIsSharedDS: jasmine.createSpy("setIsSharedDS"),
};

// Router events subject
const routerEvents$ = new Subject<any>();
const mockRouter = {
    events: routerEvents$.asObservable(),
};

// Keycloak mock
const mockKeycloak = {
    init: jasmine.createSpy("init").and.returnValue(Promise.resolve(true)),
    isLoggedIn: jasmine.createSpy("isLoggedIn"),
};

describe("CustomAuthService", () => {
    let service: CustomAuthService;

    beforeEach(() => {
        // Ensure keycloak feature enabled for tests needing it
        (environment as any).keycloak = {
            ...(environment.keycloak || {}),
            enabled: "true",
            issuer: "http://issuer",
            realm: "realm",
            clientId: "clientId",
        };

        TestBed.configureTestingModule({
            providers: [
                CustomAuthService,
                { provide: Router, useValue: mockRouter },
                { provide: KeycloakService, useValue: mockKeycloak },
                { provide: DigitalServiceStoreService, useValue: mockStore },
            ],
        });

        service = TestBed.inject(CustomAuthService);
        mockStore.setIsSharedDS.calls.reset();
        mockKeycloak.init.calls.reset();
        mockKeycloak.isLoggedIn.calls.reset();
    });

    function setPath(path: string) {
        // Use history API to change pathname (works in Karma browser)
        window.history.pushState({}, "", path);
    }

    it("isPublicRoute should return true for any path containing a public endpoint fragment", () => {
        const publicPath = `/some/${Constants.ENDPOINTS.sharedDs}/foo`;
        expect(service.isPublicRoute(publicPath)).toBeTrue();
    });

    it("isPublicRoute should return false for protected path", () => {
        expect(service.isPublicRoute("/secure/dashboard")).toBeFalse();
    });

    it("init should set shared flag true and skip keycloak.init on public route", async () => {
        setPath(`/${Constants.ENDPOINTS.sharedDs}/abc`);
        await service.init();
        expect(mockStore.setIsSharedDS).toHaveBeenCalledWith(true);
        expect(mockKeycloak.init).not.toHaveBeenCalled();
    });

    it("init should set shared flag false and call keycloak.init on protected route", async () => {
        setPath("/protected/area");
        await service.init();
        expect(mockStore.setIsSharedDS).toHaveBeenCalledWith(false);
        expect(mockKeycloak.init).toHaveBeenCalledWith(jasmine.objectContaining({
            config: jasmine.any(Object),
            initOptions: jasmine.objectContaining({ onLoad: "check-sso" }),
        }));
    });

    it("setupRouteGuard should call init when navigating to protected route and not logged in", async () => {
        setPath("/protected/start");
        mockKeycloak.isLoggedIn.and.returnValue(false);
        service.setupRouteGuard();

        routerEvents$.next(new NavigationEnd(1, "/protected/start", "/protected/start"));

        // init called (indirectly) => keycloak.init invoked
        await Promise.resolve();
        expect(mockKeycloak.isLoggedIn).toHaveBeenCalled();
        expect(mockKeycloak.init).toHaveBeenCalled();
    });

    it("setupRouteGuard should NOT call init when navigating to protected route and already logged in", async () => {
        setPath("/protected/start");
        mockKeycloak.isLoggedIn.and.returnValue(true);
        service.setupRouteGuard();

        routerEvents$.next(new NavigationEnd(1, "/protected/start", "/protected/start"));
        await Promise.resolve();

        expect(mockKeycloak.isLoggedIn).toHaveBeenCalled();
        expect(mockKeycloak.init).not.toHaveBeenCalled();
    });

    it("setupRouteGuard should NOT call init on public route navigation", async () => {
        setPath(`/${Constants.ENDPOINTS.ds}/xyz`);
        mockKeycloak.isLoggedIn.and.returnValue(false);
        service.setupRouteGuard();

        routerEvents$.next(
            new NavigationEnd(1, `/${Constants.ENDPOINTS.ds}/xyz`, `/${Constants.ENDPOINTS.ds}/xyz`),
        );
        await Promise.resolve();

        expect(mockKeycloak.isLoggedIn).not.toHaveBeenCalled();
        expect(mockKeycloak.init).not.toHaveBeenCalled();
    });
});
