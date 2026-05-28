import { TestBed } from "@angular/core/testing";
import { NavigationEnd, Router } from "@angular/router";
import { Subject } from "rxjs";

import { Constants } from "src/constants";
import { environment } from "src/environments/environment";
import { DigitalServiceStoreService } from "../../store/digital-service.store";
import { CustomAuthService, keycloak } from "./custom-auth.service";

describe("CustomAuthService", () => {
    let service: CustomAuthService;

    let routerMock: {
        events: Subject<any>;
    };

    let digitalServiceStoreMock: {
        setIsSharedDS: jasmine.Spy;
    };

    beforeEach(() => {
        routerMock = {
            events: new Subject(),
        };

        digitalServiceStoreMock = {
            setIsSharedDS: jasmine.createSpy("setIsSharedDS"),
        };

        TestBed.configureTestingModule({
            providers: [
                CustomAuthService,
                {
                    provide: Router,
                    useValue: routerMock,
                },
                {
                    provide: DigitalServiceStoreService,
                    useValue: digitalServiceStoreMock,
                },
            ],
        });

        service = TestBed.inject(CustomAuthService);
    });

    afterEach(() => {
        jasmine.clock().uninstall();
    });

    describe("isPublicRoute", () => {
        it("should return true for sharedDs route", () => {
            const result = service.isPublicRoute(`/test/${Constants.ENDPOINTS.sharedDs}`);

            expect(result).toBeTrue();
        });

        it("should return true for dsv route", () => {
            const result = service.isPublicRoute(`/test/${Constants.ENDPOINTS.dsv}`);

            expect(result).toBeTrue();
        });

        it("should return false for protected route", () => {
            const result = service.isPublicRoute("/dashboard");

            expect(result).toBeFalse();
        });
    });

    describe("init", () => {
        it("should not initialize keycloak for public routes", async () => {
            spyOn(service, "isPublicRoute").and.returnValue(true);

            const keycloakSpy = spyOn(keycloak, "init");

            const result = await service.init();

            expect(result).toBeTrue();
            expect(digitalServiceStoreMock.setIsSharedDS).toHaveBeenCalledWith(true);

            expect(keycloakSpy).not.toHaveBeenCalled();
        });

        it("should initialize keycloak for protected routes when enabled", async () => {
            spyOn(service, "isPublicRoute").and.returnValue(false);

            environment.keycloak.enabled = "true";

            const keycloakInitSpy = spyOn(keycloak, "init").and.returnValue(
                Promise.resolve(true),
            );

            const result = await service.init();

            expect(result).toBeTrue();

            expect(digitalServiceStoreMock.setIsSharedDS).toHaveBeenCalledWith(false);

            expect(keycloakInitSpy).toHaveBeenCalledWith({
                onLoad: "check-sso",
                flow: "standard",
                checkLoginIframe: false,
            });
        });

        it("should not initialize keycloak when disabled", async () => {
            spyOn(service, "isPublicRoute").and.returnValue(false);

            environment.keycloak.enabled = "false";

            const keycloakInitSpy = spyOn(keycloak, "init");

            const result = await service.init();

            expect(result).toBeTrue();
            expect(keycloakInitSpy).not.toHaveBeenCalled();
        });
    });

    describe("setupRouteGuard", () => {
        beforeEach(() => {
            environment.keycloak.enabled = "true";
        });

        it("should call init when navigating to protected route and user not authenticated", () => {
            spyOn(service, "isPublicRoute").and.returnValue(false);

            spyOn(service, "init").and.returnValue(Promise.resolve(true));

            Object.defineProperty(keycloak, "authenticated", {
                value: false,
                writable: true,
            });

            service.setupRouteGuard();

            routerMock.events.next(new NavigationEnd(1, "/protected", "/protected"));

            expect(service.init).toHaveBeenCalled();
        });

        it("should not call init for public routes", () => {
            spyOn(service, "isPublicRoute").and.returnValue(true);

            spyOn(service, "init");

            service.setupRouteGuard();

            routerMock.events.next(new NavigationEnd(1, "/public", "/public"));

            expect(service.init).not.toHaveBeenCalled();
        });

        it("should not call init when user is already authenticated", () => {
            spyOn(service, "isPublicRoute").and.returnValue(false);

            spyOn(service, "init");

            Object.defineProperty(keycloak, "authenticated", {
                value: true,
                writable: true,
            });

            service.setupRouteGuard();

            routerMock.events.next(new NavigationEnd(1, "/dashboard", "/dashboard"));

            expect(service.init).not.toHaveBeenCalled();
        });
    });
});
