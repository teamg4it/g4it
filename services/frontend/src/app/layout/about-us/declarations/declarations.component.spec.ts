import { ComponentFixture, TestBed } from "@angular/core/testing";
import { Title } from "@angular/platform-browser";
import { TranslateService } from "@ngx-translate/core";
import { BehaviorSubject, of } from "rxjs";
import { Organization, Workspace } from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { DeclarationsComponent } from "./declarations.component";

describe("DeclarationsComponent", () => {
    let component: DeclarationsComponent;
    let fixture: ComponentFixture<DeclarationsComponent>;
    let currentOrganization$: BehaviorSubject<Organization>;
    let currentWorkspace$: BehaviorSubject<Workspace>;
    let translateService: jasmine.SpyObj<TranslateService>;
    let titleService: jasmine.SpyObj<Title>;
    let userService: jasmine.SpyObj<UserService>;
    let digitalServiceStore: jasmine.SpyObj<DigitalServiceStoreService>;

    const organization = { id: 1, name: "G4IT" } as Organization;
    const workspace = { id: 2, name: "Workspace" } as Workspace;

    beforeEach(async () => {
        currentOrganization$ = new BehaviorSubject(organization);
        currentWorkspace$ = new BehaviorSubject(workspace);
        translateService = jasmine.createSpyObj("TranslateService", ["get"], {
            currentLang: "en",
        });
        translateService.get.and.returnValue(of("Declarations"));
        titleService = jasmine.createSpyObj("Title", ["setTitle"]);
        userService = jasmine.createSpyObj("UserService", ["composeEmail"], {
            currentOrganization$,
            currentWorkspace$,
            ecoDesignPercent: 85,
        });
        digitalServiceStore = jasmine.createSpyObj("DigitalServiceStoreService", [
            "isSharedDS",
        ]);
        digitalServiceStore.isSharedDS.and.returnValue(false);

        await TestBed.configureTestingModule({
            imports: [DeclarationsComponent],
            providers: [
                { provide: TranslateService, useValue: translateService },
                { provide: Title, useValue: titleService },
                { provide: UserService, useValue: userService },
                { provide: DigitalServiceStoreService, useValue: digitalServiceStore },
            ],
        })
            .overrideComponent(DeclarationsComponent, { set: { template: "" } })
            .compileComponents();

        fixture = TestBed.createComponent(DeclarationsComponent);
        component = fixture.componentInstance;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize the title, English PDF size, shared state, and user context", () => {
        fixture.detectChanges();

        expect(titleService.setTitle).toHaveBeenCalledWith("Declarations");
        expect(component.pdfSize).toBe(139);
        expect(component.isShared).toBeFalse();
        expect(component.currentOrganization).toEqual(organization);
        expect(component.selectedWorkspace).toEqual(workspace);
        expect(component.ecoDesignPercent).toBe(85);
    });

    it("should use the French PDF size and shared state for a non-English language", async () => {
        Object.defineProperty(translateService, "currentLang", { value: "fr" });
        digitalServiceStore.isSharedDS.and.returnValue(true);

        fixture.detectChanges();

        expect(component.pdfSize).toBe(428);
        expect(component.isShared).toBeTrue();
    });

    it("should update the user context when its streams emit", () => {
        fixture.detectChanges();
        const updatedOrganization = { id: 3, name: "Updated" } as Organization;
        const updatedWorkspace = { id: 4, name: "Updated workspace" } as Workspace;

        currentOrganization$.next(updatedOrganization);
        currentWorkspace$.next(updatedWorkspace);

        expect(component.currentOrganization).toEqual(updatedOrganization);
        expect(component.selectedWorkspace).toEqual(updatedWorkspace);
    });

    it("should smoothly scroll to an existing element", () => {
        const element = document.createElement("div");
        element.id = "section";
        document.body.appendChild(element);
        const scrollIntoView = jasmine.createSpy("scrollIntoView");
        element.scrollIntoView = scrollIntoView;

        component.scrollTo("section");

        expect(scrollIntoView).toHaveBeenCalledWith({ behavior: "smooth" });
        element.remove();
    });

    it("should not scroll when the target element does not exist", () => {
        spyOn(document, "getElementById").and.returnValue(null);

        component.scrollTo("missing-section");

        expect(document.getElementById).toHaveBeenCalledWith("missing-section");
    });
});
