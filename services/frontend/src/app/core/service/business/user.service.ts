/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Injectable } from "@angular/core";
import { ReplaySubject, filter, map } from "rxjs";
import { UserDataService } from "../data/user-data.service";
import { Organization, Subscriber, User } from "./../../interfaces/user.interfaces";

import { NavigationEnd, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { Constants } from "src/constants";
import { BasicRoles, Role } from "../../interfaces/roles.interfaces";

@Injectable({
    providedIn: "root",
})
export class UserService {
    ecoDesignPercent = 77;
    public organizationSubject = new ReplaySubject<Organization>(1);

    public subscriberSubject = new ReplaySubject<Subscriber>(1);

    private rolesSubject = new ReplaySubject<Role[]>(1);

    roles$ = this.rolesSubject.asObservable();

    currentSubscriber$ = this.subscriberSubject.asObservable();

    currentOrganization$ = this.organizationSubject.asObservable();

    user$ = this.userDataService.userSubject.asObservable();

    isAllowedSubscriberAdmin$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.SubscriberAdmin)),
    );

    isAllowedOrganizationAdmin$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.OrganizationAdmin)),
    );

    isAllowedDigitalServiceRead$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.DigitalServiceRead)),
    );

    isAllowedInventoryRead$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.InventoryRead)),
    );

    isAllowedInventoryWrite$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.InventoryWrite)),
    );

    isAllowedDigitalServiceWrite$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.DigitalServiceWrite)),
    );

    constructor(
        private readonly router: Router,
        private readonly userDataService: UserDataService,
        private readonly messageService: MessageService,
        private readonly translate: TranslateService,
    ) {
        this.checkRouterEvents();
    }

    checkRouterEvents(): void {
        if (this.router?.events) {
            this.router.events
                .pipe(filter((event) => event instanceof NavigationEnd))
                .subscribe(() => {
                    this.userDataService.userSubject.subscribe((currentUser) => {
                        const [_, subscribers, subscriberName, _1, organizationId, page] =
                            this.router.url.split("/");
                        this.handleRoutingEvents(
                            subscribers,
                            currentUser,
                            subscriberName,
                            organizationId,
                            page,
                        );
                    });
                });
        }
    }

    handleRoutingEvents(
        subscribers: string,
        currentUser: User,
        subscriberName: string,
        organizationId: string,
        page: string,
    ): void {
        if (subscribers === "something-went-wrong") {
            return;
        }

        if (currentUser.subscribers.length === 0) {
            this.errorMessage("subscriber-or-organization-not-found");
            this.router.navigateByUrl(`something-went-wrong/403`);
            return;
        }

        if (page !== undefined && ["inventories", "digital-services"].includes(page)) {
            return this.handlePageRouting(
                currentUser,
                subscriberName,
                organizationId,
                page,
            );
        }

        return this.subscriberOrganizationHandling(currentUser, subscribers);
    }

    handlePageRouting(
        currentUser: User,
        subscriberName: string,
        organizationId: string,
        page: string,
    ): void {
        const subscriber = currentUser?.subscribers.find(
            (sub: any) => sub.name == subscriberName,
        );

        const organization = subscriber?.organizations.find(
            (org: any) => org.id === Number(organizationId),
        );

        if (subscriber === undefined) {
            this.errorMessage("insuffisant-right-subscriber");
            this.router.navigateByUrl("/");
            return;
        }
        if (organization === undefined) {
            this.errorMessage("insuffisant-right-organization");
            this.router.navigateByUrl("/");
            return;
        }
        this.setSubscriberAndOrganization(subscriber, organization);
        if (!this.checkIfAllowed(subscriber, organization, page)) {
            this.router.navigateByUrl(Constants.WELCOME_PAGE);
        }
    }

    subscriberOrganizationHandling(currentUser: User, subscribers: string): void {
        // If the url is unknown, we set the default subscriber and the default organization
        let subscriber: Subscriber | undefined = this.getSubscriber(currentUser);
        let organization: Organization | undefined;

        if (subscriber) {
            organization = this.getOrganization(subscriber);
        }

        if (Constants.VALID_PAGES.includes(subscribers)) {
            this.setSubscriberAndOrganization(subscriber, organization!);
            return;
        }
        if (subscribers === "administration") {
            if (this.hasAnyAdminRole(currentUser)) {
                this.setSubscriberAndOrganization(subscriber, organization!);
                return;
            } else {
                this.setSubscriberAndOrganization(subscriber, organization!);
                this.router.navigateByUrl(Constants.WELCOME_PAGE);
            }
        }

        if (subscriber && organization) {
            for (const type of ["inventories", "digital-services"]) {
                if (this.checkIfAllowed(subscriber, organization, type)) {
                    this.setSubscriberAndOrganization(subscriber, organization);
                    this.router.navigateByUrl(
                        `subscribers/${subscriber.name}/organizations/${organization.id}/${type}`,
                    );
                    break;
                }
            }
        }
    }

    getOrganization(subscriber: Subscriber): Organization {
        let organization: Organization | undefined;
        let organizationNameLS = localStorage.getItem("currentOrganization") ?? undefined;

        if (organizationNameLS && Number.isNaN(organizationNameLS)) {
            localStorage.removeItem("currentOrganization");
            organizationNameLS = undefined;
        }

        if (organizationNameLS) {
            const tmpOrgs = subscriber.organizations.filter(
                (o) => o.id === Number(organizationNameLS),
            );
            if (tmpOrgs.length > 0) {
                organization = tmpOrgs[0];
            }
        }
        if (organization === undefined) organization = subscriber.organizations[0];
        return organization;
    }

    getSubscriber(currentUser: User): Subscriber {
        let subscriber: Subscriber | undefined;
        const subscriberNameLS = localStorage.getItem("currentSubscriber") ?? undefined;

        if (subscriberNameLS) {
            const tmpSubs = currentUser.subscribers.filter(
                (s) => s.name === subscriberNameLS,
            );
            if (tmpSubs.length === 0) {
                subscriber = currentUser.subscribers[0];
            } else {
                subscriber = tmpSubs[0];
            }
        } else {
            subscriber = currentUser.subscribers[0];
        }
        return subscriber;
    }

    errorMessage(key: string): void {
        this.messageService.add({
            severity: "warn",
            summary: this.translate.instant(`toast-errors.${key}.title`),
            detail: this.translate.instant(`toast-errors.${key}.text`),
        });
    }

    hasAnyAdminRole(user: User): boolean {
        return (
            this.hasAnyOrganizationAdminRole(user) || this.hasAnySubscriberAdminRole(user)
        );
    }

    hasAnySubscriberAdminRole(user: User): boolean {
        return user.subscribers.some((subscriber) =>
            subscriber.roles.includes(Role.SubscriberAdmin),
        );
    }

    hasAnyOrganizationAdminRole(user: User): boolean {
        return user.subscribers.some((subscriber) =>
            subscriber.organizations.some((organization) =>
                organization.roles.includes(Role.OrganizationAdmin),
            ),
        );
    }

    getRoles(subscriber: Subscriber, organization: Organization): Role[] {
        if (subscriber.roles.includes(Role.SubscriberAdmin)) {
            return [Role.SubscriberAdmin, Role.OrganizationAdmin, ...BasicRoles];
        }

        if (organization.roles.includes(Role.OrganizationAdmin)) {
            return [Role.OrganizationAdmin, ...BasicRoles];
        }

        const roles = [...organization.roles];

        if (organization.roles.includes(Role.InventoryWrite)) {
            roles.push(Role.InventoryRead);
        }

        if (organization.roles.includes(Role.DigitalServiceWrite)) {
            roles.push(Role.DigitalServiceRead);
        }

        return roles;
    }

    checkIfAllowed(
        subscriber: Subscriber,
        organization: Organization,
        uri: string,
    ): boolean {
        let roles: Role[] = this.getRoles(subscriber, organization);

        if (Constants.VALID_PAGES.includes(uri)) {
            return true;
        }

        if (uri === "inventories" && roles.includes(Role.InventoryRead)) {
            return true;
        }

        if (uri === "digital-services" && roles.includes(Role.DigitalServiceRead)) {
            return true;
        }

        if (
            uri === "administration" &&
            (roles.includes(Role.SubscriberAdmin) ||
                roles.includes(Role.OrganizationAdmin))
        ) {
            return true;
        }

        return false;
    }

    setSubscriberAndOrganization(
        subscriber: Subscriber,
        organization: Organization,
    ): void {
        this.subscriberSubject.next(subscriber);
        this.organizationSubject.next(organization);
        localStorage.setItem("currentSubscriber", subscriber.name);
        localStorage.setItem("currentOrganization", organization.id.toString());
        this.rolesSubject.next(this.getRoles(subscriber, organization));
    }

    checkAndRedirect(
        subscriber: Subscriber,
        organization: Organization,
        page: string,
    ): void {
        this.setSubscriberAndOrganization(subscriber, organization);
        if (this.checkIfAllowed(subscriber, organization, page)) {
            if (page === "inventories" || page === "digital-services") {
                this.router.navigateByUrl(
                    `subscribers/${subscriber.name}/organizations/${organization.id}/${page}`,
                );
            }
        } else {
            this.router.navigateByUrl(Constants.WELCOME_PAGE);
        }
    }

    getSelectedPage(): string {
        let [_, subscribers, _1, _2, _3, page] = this.router.url.split("/");

        const validPages = ["administration", ...Constants.VALID_PAGES];
        return validPages.includes(subscribers) ? subscribers : page;
    }

    composeEmail(
        currentSubscriber: Subscriber,
        selectedOrganization: Organization,
    ): string {
        let subject = `[${currentSubscriber.name}/${selectedOrganization?.id}] ${Constants.SUBJECT_MAIL}`;
        return `mailto:${Constants.RECIPIENT_MAIL}?subject=${subject}`;
    }
}
