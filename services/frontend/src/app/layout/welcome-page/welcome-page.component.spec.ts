/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { TestBed, fakeAsync, tick } from "@angular/core/testing";
import { Router } from "@angular/router";
import { BehaviorSubject, of } from "rxjs";
import { Organization, Workspace } from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { WorkspaceService } from "src/app/core/service/business/workspace.service";
import { WelcomePageComponent } from "./welcome-page.component";

describe("WelcomePageComponent", () => {
    let component: WelcomePageComponent;
    let userService: jasmine.SpyObj<UserService>;
    let workspaceService: jasmine.SpyObj<WorkspaceService>;
    let router: jasmine.SpyObj<Router>;
    let organization$: BehaviorSubject<Organization>;
    let workspace$: BehaviorSubject<Workspace>;
    let isOpen$: BehaviorSubject<boolean>;

    const organization = { name: "G4IT", ecomindai: true } as Organization;
    const workspace = { id: 42, name: "Main workspace" } as Workspace;

    beforeEach(async () => {
        organization$ = new BehaviorSubject(organization);
        workspace$ = new BehaviorSubject(workspace);
        isOpen$ = new BehaviorSubject(true);
        userService = jasmine.createSpyObj("UserService", ["composeEcoMindAccessEmail"], {
            user$: of({ firstName: "John", lastName: "Doe", email: "john@g4it.com" }),
            currentOrganization$: organization$,
            currentWorkspace$: workspace$,
            isAllowedInventoryRead$: of(true),
            isAllowedDigitalServiceRead$: of(false),
            isAllowedEcoMindAiRead$: of(false),
            ecoDesignPercent: 85,
        });
        workspaceService = jasmine.createSpyObj("WorkspaceService", [
            "getIsOpen",
            "setOpen",
        ]);
        workspaceService.getIsOpen.and.returnValue(isOpen$);
        router = jasmine.createSpyObj("Router", ["navigateByUrl"]);

        await TestBed.configureTestingModule({
            imports: [WelcomePageComponent],
            providers: [
                { provide: UserService, useValue: userService },
                { provide: WorkspaceService, useValue: workspaceService },
                { provide: Router, useValue: router },
            ],
        })
            .overrideComponent(WelcomePageComponent, { set: { template: "" } })
            .compileComponents();

        component = TestBed.createComponent(WelcomePageComponent).componentInstance;
    });

    it("should initialize user details, permissions, organization, workspace, and path", () => {
        component.ngOnInit();

        expect(component.userName).toBe("John Doe");
        expect(component.userEmail).toBe("john@g4it.com");
        expect(component.isAllowedInventory).toBeTrue();
        expect(component.isAllowedDigitalService).toBeFalse();
        expect(component.isAllowedEcoMindAi()).toBeFalse();
        expect(component.currentOrganization).toEqual(organization);
        expect(component.isEcoMindEnabledForCurrentOrganization).toBeTrue();
        expect(component.currentWorkspace).toEqual(workspace);
        expect(component.selectedPath).toBe("/organizations/G4IT/workspaces/42");
        expect(component.ecoDesignPercent).toBe(85);
    });

    it("should update organization and workspace state when streams emit", () => {
        component.ngOnInit();
        const updatedOrganization = { name: "Updated", ecomindai: false } as Organization;
        const updatedWorkspace = { id: 7, name: "Other workspace" } as Workspace;

        organization$.next(updatedOrganization);
        workspace$.next(updatedWorkspace);

        expect(component.currentOrganization).toEqual(updatedOrganization);
        expect(component.isEcoMindEnabledForCurrentOrganization).toBeFalse();
        expect(component.selectedPath).toBe("/organizations/Updated/workspaces/7");
    });

    it("should focus the create workspace button when the sidebar closes", fakeAsync(() => {
        const button = document.createElement("button");
        const focus = spyOn(button, "focus");
        component.createWorkspaceButton = {
            el: { nativeElement: { querySelector: () => button } },
        } as any;

        component.ngOnInit();
        isOpen$.next(false);
        tick(200);

        expect(focus).toHaveBeenCalled();
    }));

    it("should open the workspace sidebar", () => {
        component.openWorkspaceSidebar();

        expect(workspaceService.setOpen).toHaveBeenCalledWith(true);
    });

    it("should navigate to inventories when access is granted", () => {
        component.selectedPath = "/organizations/G4IT/workspaces/42";
        component.isAllowedInventory = true;

        component.inventories();

        expect(router.navigateByUrl).toHaveBeenCalledWith(
            "/organizations/G4IT/workspaces/42/inventories",
        );
    });

    it("should navigate to useful information when inventory access is denied", () => {
        component.isAllowedInventory = false;

        component.inventories();

        expect(router.navigateByUrl).toHaveBeenCalledWith("/useful-information");
    });

    it("should navigate to digital services with the expected state when access is granted", () => {
        component.selectedPath = "/organizations/G4IT/workspaces/42";
        component.isAllowedDigitalService = true;

        component.digitalServices();

        expect(router.navigateByUrl).toHaveBeenCalledWith(
            "/organizations/G4IT/workspaces/42/digital-services",
            { state: { isIa: false } },
        );
    });

    it("should navigate to useful information when digital service access is denied", () => {
        component.isAllowedDigitalService = false;

        component.digitalServices();

        expect(router.navigateByUrl).toHaveBeenCalledWith("/useful-information");
    });

    it("should navigate to EcoMind AI with the expected state", () => {
        component.selectedPath = "/organizations/G4IT/workspaces/42";

        component.ecoMindAi();

        expect(router.navigateByUrl).toHaveBeenCalledWith(
            "/organizations/G4IT/workspaces/42/eco-mind-ai",
            { state: { isIa: true } },
        );
    });

    it("should compose an EcoMind AI access email for the current context", () => {
        component.currentOrganization = organization;
        component.currentWorkspace = workspace;
        userService.composeEcoMindAccessEmail.and.returnValue("javascript:void(0)");

        component.requestAccessOfEcoMindAi();

        expect(userService.composeEcoMindAccessEmail).toHaveBeenCalledWith(
            "G4IT",
            "Main workspace",
        );
    });
});
