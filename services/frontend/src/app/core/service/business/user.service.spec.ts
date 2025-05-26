/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { TestBed } from "@angular/core/testing";

import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { NavigationEnd, Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { ToastModule } from "primeng/toast";
import { of, ReplaySubject } from "rxjs";
import { BasicRoles, Role } from "../../interfaces/roles.interfaces";
import { Organization, Subscriber, User } from "../../interfaces/user.interfaces";
import { UserDataService } from "../data/user-data.service";
import { UserService } from "./user.service";

describe("UserService", () => {
    let httpMock: HttpTestingController;
    let service: UserService;
    let router: Router;
    let userDataService: UserDataService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [
                RouterTestingModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
                ToastModule,
            ],
            providers: [UserService, MessageService, TranslateService, UserDataService],
        });
        userDataService = TestBed.inject(UserDataService);
        service = TestBed.inject(UserService);
        router = TestBed.inject(Router);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be create", () => {
        expect(UserService).toBeTruthy();
    });

    it("should check if subscriber admin is allowed to see page admnistration ", () => {
        const subscriber = {
            roles: [Role.SubscriberAdmin],
        } as Subscriber;

        const organization = {} as Organization;

        var result = service.checkIfAllowed(subscriber, organization, "administration");

        expect(result).toBeTrue();
    });

    it("should check if organization admin is allowed to see page admnistration ", () => {
        const subscriber = {
            roles: [Role.DigitalServiceRead],
        } as Subscriber;

        const organization = {
            roles: [Role.OrganizationAdmin],
        } as Organization;

        var result = service.checkIfAllowed(subscriber, organization, "administration");

        expect(result).toBeTrue();
    });

    it("should check if read only user is allowed to see page admnistration ", () => {
        const subscriber = {
            roles: [Role.DigitalServiceRead],
        } as Subscriber;

        const organization = {
            roles: [Role.DigitalServiceRead],
        } as Organization;

        var result = service.checkIfAllowed(subscriber, organization, "administration");

        expect(result).toBeFalse();
    });

    it("should check if subscriber administrator is allowed to see page inventories and digital-services page", () => {
        const subscriber = {
            roles: [Role.SubscriberAdmin],
        } as Subscriber;

        const organization = {
            roles: [] as any,
        } as Organization;

        var resultIS = service.checkIfAllowed(subscriber, organization, "inventories");
        var resultDS = service.checkIfAllowed(
            subscriber,
            organization,
            "digital-services",
        );

        expect(resultIS).toBeTrue();
        expect(resultDS).toBeTrue();
    });

    describe("checkRouterEvents", () => {
        it("should call subscriberOrganizationHandling on router events", () => {
            const navigationEnd = new NavigationEnd(
                1,
                "/subscribers/test/organizations/1/inventories",
                "/",
            );
            spyOnProperty(router, "events", "get").and.returnValue(of(navigationEnd));
            userDataService.userSubject = new ReplaySubject<User>(1);
            const user: User = {
                subscribers: [{ name: "test", organizations: [{ id: 1 }] }],
            } as User;
            userDataService.userSubject.next(user);

            spyOn(service, "subscriberOrganizationHandling");

            service.checkRouterEvents();

            expect(service.subscriberOrganizationHandling).toHaveBeenCalledWith(user, "");
        });
    });

    describe("checkIfAllowed", () => {
        it("should check if user is allowed to see page correctly", () => {
            const subscriber = {
                roles: [Role.SubscriberAdmin],
            } as Subscriber;

            const organization = {
                roles: [Role.OrganizationAdmin],
            } as Organization;

            expect(
                service.checkIfAllowed(subscriber, organization, "administration"),
            ).toBeTrue();
        });
    });

    describe("handleRoutingEvents", () => {
        it('should return if subscribers is "something-went-wrong"', () => {
            const user: User = { subscribers: [] } as any;

            spyOn(service, "errorMessage");
            spyOn(router, "navigateByUrl");

            service.handleRoutingEvents(
                "something-went-wrong",
                user,
                "test",
                "1",
                "inventories",
            );

            expect(service.errorMessage).not.toHaveBeenCalled();
            expect(router.navigateByUrl).not.toHaveBeenCalled();
        });

        it("should show error message and navigate to 403 if currentUser has no subscribers", () => {
            const user: User = { subscribers: [] } as any;

            spyOn(service, "errorMessage");
            spyOn(router, "navigateByUrl");

            service.handleRoutingEvents("subscribers", user, "test", "1", "inventories");

            expect(router.navigateByUrl).toHaveBeenCalledWith("something-went-wrong/403");
        });

        it('should call handlePageRouting if page is "inventories" or "digital-services"', () => {
            const user: User = {
                subscribers: [{ name: "test", organizations: [{ id: 1 }] }],
            } as User;

            spyOn(service, "handlePageRouting");

            service.handleRoutingEvents("subscribers", user, "test", "1", "inventories");
            expect(service.handlePageRouting).toHaveBeenCalledWith(
                user,
                "test",
                "1",
                "inventories",
            );

            service.handleRoutingEvents(
                "subscribers",
                user,
                "test",
                "1",
                "digital-services",
            );
            expect(service.handlePageRouting).toHaveBeenCalledWith(
                user,
                "test",
                "1",
                "digital-services",
            );
        });

        it("should call subscriberOrganizationHandling for other pages", () => {
            const user: User = {
                subscribers: [{ name: "test", organizations: [{ id: 1 }] }],
            } as User;

            spyOn(service, "subscriberOrganizationHandling");

            service.handleRoutingEvents("subscribers", user, "test", "1", "other-page");
            expect(service.subscriberOrganizationHandling).toHaveBeenCalledWith(
                user,
                "subscribers",
            );
        });

        it("should handle case when subscriber is not found", () => {
            const user: User = {
                subscribers: [{ name: "test", organizations: [{ id: 1 }] }],
            } as User;

            spyOn(service, "errorMessage");
            spyOn(router, "navigateByUrl");

            service.handleRoutingEvents(
                "subscribers",
                user,
                "non-existent-subscriber",
                "1",
                "inventories",
            );

            expect(service.errorMessage).toHaveBeenCalledWith(
                "insuffisant-right-subscriber",
            );
            expect(router.navigateByUrl).toHaveBeenCalledWith("/");
        });

        it("should handle case when organization is not found", () => {
            const user: User = {
                subscribers: [{ name: "test", organizations: [{ id: 1 }] }],
            } as User;

            spyOn(service, "errorMessage");
            spyOn(router, "navigateByUrl");

            service.handleRoutingEvents(
                "subscribers",
                user,
                "test",
                "non-existent-organization",
                "inventories",
            );

            expect(service.errorMessage).toHaveBeenCalledWith(
                "insuffisant-right-organization",
            );
            expect(router.navigateByUrl).toHaveBeenCalledWith("/");
        });

        it("should call setSubscriberAndOrganization if checkIfAllowed returns true", () => {
            const user: User = {
                subscribers: [{ name: "test", organizations: [{ id: 1 }] }],
            } as any;
            const subscriber = user.subscribers[0];
            const organization = subscriber.organizations[0];

            spyOn(service, "checkIfAllowed").and.returnValue(true);
            spyOn(service, "setSubscriberAndOrganization");

            service.handleRoutingEvents("subscribers", user, "test", "1", "inventories");

            expect(service.checkIfAllowed).toHaveBeenCalledWith(
                subscriber,
                organization,
                "inventories",
            );
            expect(service.setSubscriberAndOrganization).toHaveBeenCalledWith(
                subscriber,
                organization,
            );
        });
    });

    it("should navigate to the specified page if user is allowed", () => {
        const subscriber = {
            roles: [Role.SubscriberAdmin],
        } as Subscriber;

        const organization = {
            roles: [Role.OrganizationAdmin],
        } as Organization;

        spyOn(service, "checkIfAllowed").and.returnValue(true);
        spyOn(service, "setSubscriberAndOrganization");
        spyOn(router, "navigateByUrl");

        service.checkAndRedirect(subscriber, organization, "inventories");

        expect(service.checkIfAllowed).toHaveBeenCalledWith(
            subscriber,
            organization,
            "inventories",
        );
        expect(service.setSubscriberAndOrganization).toHaveBeenCalledWith(
            subscriber,
            organization,
        );
        expect(router.navigateByUrl).toHaveBeenCalledWith(
            `subscribers/${subscriber.name}/organizations/${organization.id}/inventories`,
        );
    });

    it("should navigate to the 403 page if user is not allowed", () => {
        const subscriber = {
            name: "testSubscriber",
            roles: [Role.DigitalServiceRead],
        } as Subscriber;

        const organization = {
            id: 1,
            roles: [Role.DigitalServiceRead],
        } as Organization;

        spyOn(service, "checkIfAllowed").and.returnValue(false);
        spyOn(router, "navigateByUrl");

        service.checkAndRedirect(subscriber, organization, "administration");

        expect(service.checkIfAllowed).toHaveBeenCalledWith(
            subscriber,
            organization,
            "administration",
        );
        expect(router.navigateByUrl).toHaveBeenCalledWith("welcome-page");
    });

    it("should set the subscriber, organization, and roles", () => {
        const subscriber = {
            name: "testSubscriber",
            roles: [Role.SubscriberAdmin],
            organizations: [
                {
                    id: 1,
                    roles: [Role.OrganizationAdmin],
                },
            ],
        } as Subscriber;

        const organization = {
            id: 1,
            roles: [Role.OrganizationAdmin],
        } as Organization;

        spyOn(service.subscriberSubject, "next");
        spyOn(service.organizationSubject, "next");
        spyOn(localStorage, "setItem");
        spyOn(service["rolesSubject"], "next");

        service.setSubscriberAndOrganization(subscriber, organization);

        expect(service.subscriberSubject.next).toHaveBeenCalledWith(subscriber);
        expect(service.organizationSubject.next).toHaveBeenCalledWith(organization);
        expect(localStorage.setItem).toHaveBeenCalledWith(
            "currentSubscriber",
            subscriber.name,
        );
        expect(localStorage.setItem).toHaveBeenCalledWith(
            "currentOrganization",
            organization.id.toString(),
        );
        expect(service["rolesSubject"].next).toHaveBeenCalledWith([
            Role.SubscriberAdmin,
            Role.OrganizationAdmin,
            ...BasicRoles,
        ]);
    });

    describe("hasAnyOrganizationAdminRole", () => {
        it("should return true if the user has any organization admin role", () => {
            const user: User = {
                subscribers: [
                    {
                        name: "test",
                        organizations: [
                            {
                                id: 1,
                                roles: [Role.OrganizationAdmin],
                            },
                        ],
                    },
                ],
            } as User;

            const result = service.hasAnyOrganizationAdminRole(user);

            expect(result).toBeTrue();
        });

        it("should return false if the user does not have any organization admin role", () => {
            const user: User = {
                subscribers: [
                    {
                        name: "test",
                        organizations: [
                            {
                                id: 1,
                                roles: [Role.DigitalServiceRead],
                            },
                        ],
                    },
                ],
            } as User;

            const result = service.hasAnyOrganizationAdminRole(user);

            expect(result).toBeFalse();
        });
    });

    describe("hasAnySubscriberAdminRole", () => {
        it("should return true if the user has a subscriber with SubscriberAdmin role", () => {
            const user: User = {
                subscribers: [
                    {
                        roles: [Role.SubscriberAdmin],
                    },
                    {
                        roles: [Role.DigitalServiceRead],
                    },
                ],
            } as User;

            const result = service.hasAnySubscriberAdminRole(user);

            expect(result).toBeTrue();
        });

        it("should return false if the user does not have a subscriber with SubscriberAdmin role", () => {
            const user: User = {
                subscribers: [
                    {
                        roles: [Role.DigitalServiceRead],
                    },
                    {
                        roles: [Role.DigitalServiceRead],
                    },
                ],
            } as User;

            const result = service.hasAnySubscriberAdminRole(user);

            expect(result).toBeFalse();
        });
    });

    describe("errorMessage", () => {
        it("should add a warning message to the message service", () => {
            const messageServiceSpy = spyOn(service["messageService"], "add");

            service.errorMessage("test-key");

            expect(messageServiceSpy).toHaveBeenCalledWith({
                severity: "warn",
                summary: jasmine.any(String),
                detail: jasmine.any(String),
            });
        });
    });

    describe("hasAnyAdminRole", () => {
        it("should return true if the user has any admin role", () => {
            const user: User = {
                subscribers: [
                    {
                        roles: [Role.SubscriberAdmin],
                        organizations: [
                            {
                                roles: [Role.OrganizationAdmin],
                            },
                        ],
                    },
                ],
            } as User;

            const result = service.hasAnyAdminRole(user);

            expect(result).toBeTrue();
        });

        it("should return false if the user does not have any admin role", () => {
            const user: User = {
                subscribers: [
                    {
                        roles: [Role.DigitalServiceRead],
                        organizations: [
                            {
                                roles: [Role.DigitalServiceRead],
                            },
                        ],
                    },
                ],
            } as User;

            const result = service.hasAnyAdminRole(user);

            expect(result).toBeFalse();
        });
    });
    describe("subscriberOrganizationHandling", () => {
        it("should set the default subscriber and organization if the URL is unknown", () => {
            const currentUser: User = {
                subscribers: [
                    {
                        name: "testSubscriber",
                        organizations: [
                            {
                                id: 1,
                                roles: [Role.OrganizationAdmin],
                            },
                        ],
                    },
                ],
            } as User;

            spyOn(service, "getSubscriber").and.returnValue(currentUser.subscribers[0]);
            spyOn(service, "getOrganization").and.returnValue(
                currentUser.subscribers[0].organizations[0],
            );
            spyOn(service, "checkIfAllowed").and.returnValue(true);
            spyOn(service, "setSubscriberAndOrganization");

            service.subscriberOrganizationHandling(currentUser, "unknown-url");

            expect(service.getSubscriber).toHaveBeenCalledWith(currentUser);
            expect(service.getOrganization).toHaveBeenCalledWith(
                currentUser.subscribers[0],
            );
            expect(service.setSubscriberAndOrganization).toHaveBeenCalledWith(
                currentUser.subscribers[0],
                currentUser.subscribers[0].organizations[0],
            );
        });

        it("should navigate to the 403 page if the current user is not allowed", () => {
            const currentUser: User = {
                subscribers: [
                    {
                        name: "testSubscriber",
                        organizations: [
                            {
                                id: 1,
                                roles: [Role.DigitalServiceRead],
                            },
                        ],
                    },
                ],
            } as User;

            spyOn(service, "checkIfAllowed").and.returnValue(false);
            spyOn(router, "navigateByUrl");

            service.subscriberOrganizationHandling(currentUser, "inventories");

            expect(service.checkIfAllowed).toHaveBeenCalledWith(
                currentUser.subscribers[0],
                currentUser.subscribers[0].organizations[0],
                "inventories",
            );
        });
    });
});
