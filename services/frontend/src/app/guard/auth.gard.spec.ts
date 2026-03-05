import { TestBed } from "@angular/core/testing";
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from "@angular/router";
import { KeycloakService } from "keycloak-angular";
import { AuthGuard } from "./auth.gard";

describe("AuthGuard", () => {
    let guard: AuthGuard;
    let mockRouter: jasmine.SpyObj<Router>;
    let mockKeycloakService: jasmine.SpyObj<KeycloakService>;
    let mockRoute: ActivatedRouteSnapshot;
    let mockState: RouterStateSnapshot;

    beforeEach(() => {
        mockRouter = jasmine.createSpyObj("Router", ["navigate", "parseUrl"]);
        mockKeycloakService = jasmine.createSpyObj("KeycloakService", [
            "isLoggedIn",
            "login",
            "logout",
            "getKeycloakInstance",
        ]);

        TestBed.configureTestingModule({
            providers: [
                AuthGuard,
                { provide: Router, useValue: mockRouter },
                { provide: KeycloakService, useValue: mockKeycloakService },
            ],
        });

        guard = TestBed.inject(AuthGuard);
        mockRoute = {} as ActivatedRouteSnapshot;
        mockState = { url: "/test-url" } as RouterStateSnapshot;
    });

    it("should be created", () => {
        expect(guard).toBeTruthy();
    });

    it("should extend KeycloakAuthGuard", () => {
        expect(guard).toBeDefined();
        expect(guard.isAccessAllowed).toBeDefined();
    });

    it("should inject Router", () => {
        expect(guard["router"]).toBe(mockRouter);
    });

    it("should inject KeycloakService", () => {
        expect(guard["keycloak"]).toBe(mockKeycloakService);
    });

    describe("isAccessAllowed", () => {
        it("should return true when user is authenticated", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const result = await guard.isAccessAllowed(mockRoute, mockState);

            expect(result).toBe(true);
        });

        it("should return false when user is not authenticated", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => false,
                configurable: true,
            });

            const result = await guard.isAccessAllowed(mockRoute, mockState);

            expect(result).toBe(false);
        });

        it("should be called with ActivatedRouteSnapshot", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const testRoute = {
                params: { id: "123" },
                queryParams: { query: "test" },
            } as any;

            const result = await guard.isAccessAllowed(testRoute, mockState);

            expect(result).toBe(true);
        });

        it("should be called with RouterStateSnapshot", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const testState = {
                url: "/dashboard",
                root: {} as any,
            } as RouterStateSnapshot;

            const result = await guard.isAccessAllowed(mockRoute, testState);

            expect(result).toBe(true);
        });

        it("should handle different route configurations", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const routes: ActivatedRouteSnapshot[] = [
                { data: { roles: ["admin"] } } as any,
                { data: { roles: ["user"] } } as any,
                { data: {} } as any,
                {} as any,
            ];

            for (const route of routes) {
                const result = await guard.isAccessAllowed(route, mockState);
                expect(result).toBe(true);
            }
        });

        it("should handle different state URLs", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const states: RouterStateSnapshot[] = [
                { url: "/" } as any,
                { url: "/home" } as any,
                { url: "/admin/users" } as any,
                { url: "/dashboard?id=123" } as any,
            ];

            for (const state of states) {
                const result = await guard.isAccessAllowed(mockRoute, state);
                expect(result).toBe(true);
            }
        });

        it("should return boolean or UrlTree type", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const result = await guard.isAccessAllowed(mockRoute, mockState);

            expect(typeof result === "boolean" || result instanceof Object).toBe(true);
        });

        it("should handle async authentication check", async () => {
            let authValue = false;
            Object.defineProperty(guard, "authenticated", {
                get: () => authValue,
                configurable: true,
            });

            const result1 = await guard.isAccessAllowed(mockRoute, mockState);
            expect(result1).toBe(false);

            authValue = true;
            const result2 = await guard.isAccessAllowed(mockRoute, mockState);
            expect(result2).toBe(true);
        });

        it("should return promise that resolves to boolean", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const promise = guard.isAccessAllowed(mockRoute, mockState);

            expect(promise).toBeInstanceOf(Promise);
            const result = await promise;
            expect(typeof result).toBe("boolean");
        });

        it("should check authenticated property for access decision", async () => {
            const authenticatedSpy = jasmine.createSpy("authenticated");
            Object.defineProperty(guard, "authenticated", {
                get: authenticatedSpy.and.returnValue(true),
                configurable: true,
            });

            await guard.isAccessAllowed(mockRoute, mockState);

            expect(authenticatedSpy).toHaveBeenCalled();
        });
    });

    describe("Constructor", () => {
        it("should call super constructor with router and keycloak", () => {
            const newGuard = new AuthGuard(mockRouter, mockKeycloakService);
            expect(newGuard).toBeTruthy();
            expect(newGuard["router"]).toBe(mockRouter);
            expect(newGuard["keycloak"]).toBe(mockKeycloakService);
        });

        it("should be injectable", () => {
            const injectedGuard = TestBed.inject(AuthGuard);
            expect(injectedGuard).toBeTruthy();
            expect(injectedGuard).toBeInstanceOf(AuthGuard);
        });

        it("should be provided in root", () => {
            const guard1 = TestBed.inject(AuthGuard);
            const guard2 = TestBed.inject(AuthGuard);
            expect(guard1).toBe(guard2);
        });
    });

    describe("Integration scenarios", () => {
        it("should deny access when not authenticated", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => false,
                configurable: true,
            });

            const protectedRoute = {
                data: { requiresAuth: true },
            } as any;
            const state = { url: "/protected" } as any;

            const result = await guard.isAccessAllowed(protectedRoute, state);

            expect(result).toBe(false);
        });

        it("should allow access when authenticated", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const protectedRoute = {
                data: { requiresAuth: true },
            } as any;
            const state = { url: "/protected" } as any;

            const result = await guard.isAccessAllowed(protectedRoute, state);

            expect(result).toBe(true);
        });

        it("should handle multiple consecutive access checks", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const result1 = await guard.isAccessAllowed(mockRoute, mockState);
            const result2 = await guard.isAccessAllowed(mockRoute, mockState);
            const result3 = await guard.isAccessAllowed(mockRoute, mockState);

            expect(result1).toBe(true);
            expect(result2).toBe(true);
            expect(result3).toBe(true);
        });

        it("should handle authentication state changes", async () => {
            let isAuthenticated = true;
            Object.defineProperty(guard, "authenticated", {
                get: () => isAuthenticated,
                configurable: true,
            });

            const result1 = await guard.isAccessAllowed(mockRoute, mockState);
            expect(result1).toBe(true);

            isAuthenticated = false;
            const result2 = await guard.isAccessAllowed(mockRoute, mockState);
            expect(result2).toBe(false);

            isAuthenticated = true;
            const result3 = await guard.isAccessAllowed(mockRoute, mockState);
            expect(result3).toBe(true);
        });
    });

    describe("Edge cases", () => {
        it("should handle null route snapshot", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const result = await guard.isAccessAllowed(null as any, mockState);

            expect(result).toBe(true);
        });

        it("should handle null state snapshot", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const result = await guard.isAccessAllowed(mockRoute, null as any);

            expect(result).toBe(true);
        });

        it("should handle empty route data", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const emptyRoute = { data: {} } as any;

            const result = await guard.isAccessAllowed(emptyRoute, mockState);

            expect(result).toBe(true);
        });

        it("should handle complex route hierarchies", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const complexRoute = {
                parent: {
                    parent: {
                        data: { auth: true },
                    },
                    data: { roles: ["admin"] },
                },
                data: { specific: "value" },
            } as any;

            const result = await guard.isAccessAllowed(complexRoute, mockState);

            expect(result).toBe(true);
        });

        it("should handle routes with query parameters", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const routeWithQuery = {
                queryParams: { redirect: "/home", id: "123" },
            } as any;

            const result = await guard.isAccessAllowed(routeWithQuery, mockState);

            expect(result).toBe(true);
        });

        it("should handle routes with path parameters", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const routeWithParams = {
                params: { userId: "456", orgId: "789" },
            } as any;

            const result = await guard.isAccessAllowed(routeWithParams, mockState);

            expect(result).toBe(true);
        });

        it("should handle state with fragments", async () => {
            Object.defineProperty(guard, "authenticated", {
                get: () => true,
                configurable: true,
            });

            const stateWithFragment = {
                url: "/page#section",
                root: {} as any,
            } as RouterStateSnapshot;

            const result = await guard.isAccessAllowed(mockRoute, stateWithFragment);

            expect(result).toBe(true);
        });
    });
});
