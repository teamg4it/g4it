import { TestBed } from "@angular/core/testing";
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from "@angular/router";

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
import { DigitalServiceStoreService } from "../core/store/digital-service.store";
import { authGuard } from "./auth.gard";

describe("authGuard", () => {
    let customAuthService: CustomAuthService;
    let mockRoute: ActivatedRouteSnapshot;
    let mockState: RouterStateSnapshot;

    // Mock dependencies
    const mockRouter = {
        events: {
            pipe: jasmine
                .createSpy("pipe")
                .and.returnValue({ subscribe: jasmine.createSpy() }),
        },
    };

    const mockDigitalServiceStore = {
        setIsSharedDS: jasmine.createSpy("setIsSharedDS"),
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                CustomAuthService,
                { provide: Router, useValue: mockRouter },
                {
                    provide: DigitalServiceStoreService,
                    useValue: mockDigitalServiceStore,
                },
            ],
        });

        customAuthService = TestBed.inject(CustomAuthService);

        // Create mock route and state
        mockRoute = {} as ActivatedRouteSnapshot;
        mockState = {
            url: "/dashboard",
        } as RouterStateSnapshot;

        // Reset keycloak properties before each test
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

        // Clear localStorage
        localStorage.clear();
    });

    afterEach(() => {
        // Clean up keycloak properties
        delete (keycloak as any).authenticated;
        delete (keycloak as any).token;
    });

    describe("Public Routes", () => {
        it("should allow access to public routes regardless of authentication", async () => {
            spyOn(customAuthService, "isPublicRoute").and.returnValue(true);

            const result = await TestBed.runInInjectionContext(() =>
                authGuard(mockRoute, mockState),
            );

            expect(result).toBe(true);
            expect(customAuthService.isPublicRoute).toHaveBeenCalledWith("/dashboard");
        });

        it("should allow access to public routes when Keycloak is enabled", async () => {
            spyOn(customAuthService, "isPublicRoute").and.returnValue(true);
            const originalEnabled = environment.keycloak.enabled;
            environment.keycloak.enabled = "true";

            const result = await TestBed.runInInjectionContext(() =>
                authGuard(mockRoute, mockState),
            );

            expect(result).toBe(true);
            environment.keycloak.enabled = originalEnabled;
        });

        it("should allow access to share landing page route", async () => {
            mockState.url = "/share/123-456-789";
            spyOn(customAuthService, "isPublicRoute").and.returnValue(true);

            const result = await TestBed.runInInjectionContext(() =>
                authGuard(mockRoute, mockState),
            );

            expect(result).toBe(true);
        });
    });

    describe("Protected Routes - Keycloak Disabled", () => {
        it("should allow access when Keycloak is disabled", async () => {
            spyOn(customAuthService, "isPublicRoute").and.returnValue(false);
            const originalEnabled = environment.keycloak.enabled;
            environment.keycloak.enabled = "false";

            const result = await TestBed.runInInjectionContext(() =>
                authGuard(mockRoute, mockState),
            );

            expect(result).toBe(true);
            environment.keycloak.enabled = originalEnabled;
        });
    });

    describe("Protected Routes - Keycloak Enabled", () => {
        beforeEach(() => {
            spyOn(customAuthService, "isPublicRoute").and.returnValue(false);
        });

        it("should allow access when user is authenticated", async () => {
            const originalEnabled = environment.keycloak.enabled;
            environment.keycloak.enabled = "true";

            Object.defineProperty(keycloak, "authenticated", {
                value: true,
                writable: true,
                configurable: true,
            });

            const result = await TestBed.runInInjectionContext(() =>
                authGuard(mockRoute, mockState),
            );

            expect(result).toBe(true);
            environment.keycloak.enabled = originalEnabled;
        });

        it("should redirect to login when user is not authenticated", async () => {
            const originalEnabled = environment.keycloak.enabled;
            environment.keycloak.enabled = "true";

            Object.defineProperty(keycloak, "authenticated", {
                value: false,
                writable: true,
                configurable: true,
            });

            const loginSpy = spyOn(keycloak, "login" as any).and.returnValue(
                Promise.resolve(),
            );

            const result = await TestBed.runInInjectionContext(() =>
                authGuard(mockRoute, mockState),
            );

            expect(loginSpy).toHaveBeenCalledWith({
                redirectUri: globalThis.location.origin + "/dashboard",
                loginHint: "",
            });
            expect(result).toBe(false);
            environment.keycloak.enabled = originalEnabled;
        });

        it("should use stored username as login hint when available", async () => {
            const originalEnabled = environment.keycloak.enabled;
            environment.keycloak.enabled = "true";
            localStorage.setItem("username", "test-user@example.com");

            Object.defineProperty(keycloak, "authenticated", {
                value: false,
                writable: true,
                configurable: true,
            });

            const loginSpy = spyOn(keycloak, "login" as any).and.returnValue(
                Promise.resolve(),
            );

            const result = await TestBed.runInInjectionContext(() =>
                authGuard(mockRoute, mockState),
            );

            expect(loginSpy).toHaveBeenCalledWith({
                redirectUri: globalThis.location.origin + "/dashboard",
                loginHint: "test-user@example.com",
            });
            expect(result).toBe(false);
            environment.keycloak.enabled = originalEnabled;
        });

        it("should preserve deep link URL in redirectUri", async () => {
            const originalEnabled = environment.keycloak.enabled;
            environment.keycloak.enabled = "true";
            mockState.url = "/inventories/123/equipment/456";

            Object.defineProperty(keycloak, "authenticated", {
                value: false,
                writable: true,
                configurable: true,
            });

            const loginSpy = spyOn(keycloak, "login" as any).and.returnValue(
                Promise.resolve(),
            );

            const result = await TestBed.runInInjectionContext(() =>
                authGuard(mockRoute, mockState),
            );

            expect(loginSpy).toHaveBeenCalledWith({
                redirectUri:
                    globalThis.location.origin + "/inventories/123/equipment/456",
                loginHint: "",
            });
            expect(result).toBe(false);
            environment.keycloak.enabled = originalEnabled;
        });

        it("should preserve query parameters in redirectUri", async () => {
            const originalEnabled = environment.keycloak.enabled;
            environment.keycloak.enabled = "true";
            mockState.url = "/dashboard?tab=overview&filter=active";

            Object.defineProperty(keycloak, "authenticated", {
                value: false,
                writable: true,
                configurable: true,
            });

            const loginSpy = spyOn(keycloak, "login" as any).and.returnValue(
                Promise.resolve(),
            );

            const result = await TestBed.runInInjectionContext(() =>
                authGuard(mockRoute, mockState),
            );

            expect(loginSpy).toHaveBeenCalledWith({
                redirectUri:
                    globalThis.location.origin + "/dashboard?tab=overview&filter=active",
                loginHint: "",
            });
            expect(result).toBe(false);
            environment.keycloak.enabled = originalEnabled;
        });
    });

    describe("Edge Cases", () => {
        it("should handle root path correctly", async () => {
            spyOn(customAuthService, "isPublicRoute").and.returnValue(false);
            mockState.url = "/";

            const originalEnabled = environment.keycloak.enabled;
            environment.keycloak.enabled = "true";

            Object.defineProperty(keycloak, "authenticated", {
                value: true,
                writable: true,
                configurable: true,
            });

            const result = await TestBed.runInInjectionContext(() =>
                authGuard(mockRoute, mockState),
            );

            expect(result).toBe(true);
            environment.keycloak.enabled = originalEnabled;
        });

        it("should handle empty string as loginHint when username is empty", async () => {
            const originalEnabled = environment.keycloak.enabled;
            environment.keycloak.enabled = "true";
            localStorage.setItem("username", "");

            spyOn(customAuthService, "isPublicRoute").and.returnValue(false);

            Object.defineProperty(keycloak, "authenticated", {
                value: false,
                writable: true,
                configurable: true,
            });

            const loginSpy = spyOn(keycloak, "login" as any).and.returnValue(
                Promise.resolve(),
            );

            const result = await TestBed.runInInjectionContext(() =>
                authGuard(mockRoute, mockState),
            );

            expect(loginSpy).toHaveBeenCalledWith({
                redirectUri: globalThis.location.origin + "/dashboard",
                loginHint: "",
            });
            environment.keycloak.enabled = originalEnabled;
        });

        it("should check isPublicRoute for every guard invocation", async () => {
            const isPublicSpy = spyOn(customAuthService, "isPublicRoute").and.returnValue(
                true,
            );

            await TestBed.runInInjectionContext(() => authGuard(mockRoute, mockState));

            expect(isPublicSpy).toHaveBeenCalledTimes(1);
            expect(isPublicSpy).toHaveBeenCalledWith("/dashboard");
        });
    });

    describe("Integration Scenarios", () => {
        it("should work correctly when transitioning from authenticated to unauthenticated", async () => {
            spyOn(customAuthService, "isPublicRoute").and.returnValue(false);
            const originalEnabled = environment.keycloak.enabled;
            environment.keycloak.enabled = "true";

            // First call - authenticated
            Object.defineProperty(keycloak, "authenticated", {
                value: true,
                writable: true,
                configurable: true,
            });

            const result1 = await TestBed.runInInjectionContext(() =>
                authGuard(mockRoute, mockState),
            );

            expect(result1).toBe(true);

            // Second call - not authenticated (session expired)
            Object.defineProperty(keycloak, "authenticated", {
                value: false,
                writable: true,
                configurable: true,
            });

            const loginSpy = spyOn(keycloak, "login" as any).and.returnValue(
                Promise.resolve(),
            );

            const result2 = await TestBed.runInInjectionContext(() =>
                authGuard(mockRoute, mockState),
            );

            expect(result2).toBe(false);
            expect(loginSpy).toHaveBeenCalled();

            environment.keycloak.enabled = originalEnabled;
        });

        it("should allow multiple public routes without calling Keycloak", async () => {
            const isPublicSpy = spyOn(customAuthService, "isPublicRoute").and.returnValue(
                true,
            );
            const loginSpy = spyOn(keycloak, "login" as any);

            const publicRoutes = ["/share/123", "/share/456", "/share/789"];

            for (const route of publicRoutes) {
                mockState.url = route;
                const result = await TestBed.runInInjectionContext(() =>
                    authGuard(mockRoute, mockState),
                );
                expect(result).toBe(true);
            }

            expect(isPublicSpy).toHaveBeenCalledTimes(3);
            expect(loginSpy).not.toHaveBeenCalled();
        });
    });
});
