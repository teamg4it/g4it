import { TestBed } from "@angular/core/testing";
import { ActivatedRouteSnapshot, RouterStateSnapshot } from "@angular/router";

import { environment } from "src/environments/environment";

// Configure environment BEFORE importing the service to prevent Keycloak initialization errors
if (!environment.keycloak) {
    (environment as any).keycloak = {
        enabled: "false",
        issuer: "http://test-issuer",
        realm: "test-realm",
        clientId: "test-client",
    };
}

import {
    CustomAuthService,
    keycloak,
} from "../core/service/business/custom-auth.service";
import { authGuard } from "./auth.gard";

describe("authGuard", () => {
    let customAuthServiceSpy: jasmine.SpyObj<CustomAuthService>;

    const mockRoute = {} as ActivatedRouteSnapshot;

    const mockState = {
        url: "/dashboard",
    } as RouterStateSnapshot;

    beforeEach(() => {
        // Initialize keycloak mock properties
        Object.defineProperty(keycloak, "authenticated", {
            value: false,
            writable: true,
            configurable: true,
        });

        Object.defineProperty(keycloak, "token", {
            value: null,
            writable: true,
            configurable: true,
        });

        customAuthServiceSpy = jasmine.createSpyObj("CustomAuthService", [
            "isPublicRoute",
        ]);

        TestBed.configureTestingModule({
            providers: [
                {
                    provide: CustomAuthService,
                    useValue: customAuthServiceSpy,
                },
            ],
        });
    });

    afterEach(() => {
        localStorage.clear();
        // Clean up property descriptors
        delete (keycloak as any).authenticated;
        delete (keycloak as any).token;
    });

    it("should allow access for public routes", async () => {
        customAuthServiceSpy.isPublicRoute.and.returnValue(true);

        const result = await TestBed.runInInjectionContext(() =>
            authGuard(mockRoute, mockState),
        );

        expect(result).toBeTrue();
        expect(customAuthServiceSpy.isPublicRoute).toHaveBeenCalledWith("/dashboard");
    });

    it("should allow access when keycloak is authenticated", async () => {
        customAuthServiceSpy.isPublicRoute.and.returnValue(false);

        environment.keycloak.enabled = "true";
        Object.defineProperty(keycloak, "authenticated", {
            value: true,
            writable: true,
            configurable: true,
        });

        const result = await TestBed.runInInjectionContext(() =>
            authGuard(mockRoute, mockState),
        );

        expect(result).toBeTrue();
    });

    it("should redirect to login when user is not authenticated", async () => {
        customAuthServiceSpy.isPublicRoute.and.returnValue(false);

        environment.keycloak.enabled = "true";
        Object.defineProperty(keycloak, "authenticated", {
            value: false,
            writable: true,
            configurable: true,
        });

        localStorage.setItem("username", "testuser");

        const loginSpy = spyOn(keycloak, "login").and.returnValue(Promise.resolve());

        const result = await TestBed.runInInjectionContext(() =>
            authGuard(mockRoute, mockState),
        );

        expect(loginSpy).toHaveBeenCalledWith({
            redirectUri: globalThis.location.origin + "/dashboard",
            loginHint: "testuser",
        });

        expect(result).toBeFalse();
    });

    it("should redirect to login with empty loginHint when username is not in localStorage", async () => {
        customAuthServiceSpy.isPublicRoute.and.returnValue(false);

        environment.keycloak.enabled = "true";
        Object.defineProperty(keycloak, "authenticated", {
            value: false,
            writable: true,
            configurable: true,
        });

        const loginSpy = spyOn(keycloak, "login").and.returnValue(Promise.resolve());

        const result = await TestBed.runInInjectionContext(() =>
            authGuard(mockRoute, mockState),
        );

        expect(loginSpy).toHaveBeenCalledWith({
            redirectUri: globalThis.location.origin + "/dashboard",
            loginHint: "",
        });

        expect(result).toBeFalse();
    });

    it("should allow access when keycloak is disabled", async () => {
        customAuthServiceSpy.isPublicRoute.and.returnValue(false);

        environment.keycloak.enabled = "false";

        const result = await TestBed.runInInjectionContext(() =>
            authGuard(mockRoute, mockState),
        );

        expect(result).toBeTrue();
    });
});
