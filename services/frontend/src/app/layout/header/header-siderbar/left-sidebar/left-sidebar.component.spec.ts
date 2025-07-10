import { ElementRef, signal } from "@angular/core";
import { TestBed } from "@angular/core/testing";
import { Router } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { UserService } from "src/app/core/service/business/user.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { LeftSidebarComponent } from "./left-sidebar.component";

const mockElementRef = {
    nativeElement: document.createElement("div"),
};

describe("LeftSidebarComponent", () => {
    let component: LeftSidebarComponent;
    let userServiceMock: jasmine.SpyObj<UserService>;

    beforeEach(() => {
        userServiceMock = jasmine.createSpyObj("UserService", ["getSelectedPage"]);

        TestBed.configureTestingModule({
            providers: [
                LeftSidebarComponent,
                { provide: UserService, useValue: userServiceMock },
                { provide: GlobalStoreService, useValue: {} },
                { provide: TranslateService, useValue: {} },
                { provide: Router, useValue: {} },
                { provide: ElementRef, useValue: mockElementRef },
            ],
            imports: [SharedModule, TranslateModule.forRoot()],
        });

        component = TestBed.inject(LeftSidebarComponent);
    });

    it("should set the selected page correctly", () => {
        const mockSelectedPage = "mock-page";
        userServiceMock.getSelectedPage.and.returnValue(mockSelectedPage);

        component.setSelectedPage();

        expect(component.selectedPage()).toBe(mockSelectedPage);
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should return correct aria-current value", () => {
        component.selectedPage.set("digital-services");
        expect(component.getAriaCurrent("digital-services")).toBe("page");
        expect(component.getAriaCurrent("inventories")).toBeNull();
    });

    it("should call togglePlusMenu(false) when documentClick is outside", () => {
        spyOn(component, "togglePlusMenu");
        const outsideTarget = document.createElement("span");
        component.documentClick(outsideTarget);
        expect(component.togglePlusMenu).toHaveBeenCalledWith(false);
    });

    it("should not call togglePlusMenu(false) when documentClick is inside", () => {
        spyOn(component, "togglePlusMenu");
        component.documentClick(component["el"].nativeElement);
        expect(component.togglePlusMenu).not.toHaveBeenCalled();
    });

    it("should toggle plus menu open/close on mobile", () => {
        component.isMobile = signal(true);
        component.isPlusMenuEnabled = true;
        spyOn(document, "querySelector").and.returnValue({
            classList: {
                add: jasmine.createSpy("add"),
                remove: jasmine.createSpy("remove"),
            },
        } as any);

        component.isPlusMenuOpen = false;
        component.togglePlusMenu();
        expect(component.isPlusMenuOpen).toBeTrue();

        component.togglePlusMenu();
        expect(component.isPlusMenuOpen).toBeFalse();
    });

    it("should set plus menu open state based on argument", () => {
        component.isMobile = signal(true);
        component.isPlusMenuEnabled = true;
        spyOn(document, "querySelector").and.returnValue({
            classList: {
                add: jasmine.createSpy("add"),
                remove: jasmine.createSpy("remove"),
            },
        } as any);

        component.togglePlusMenu(true);
        expect(component.isPlusMenuOpen).toBeTrue();

        component.togglePlusMenu(false);
        expect(component.isPlusMenuOpen).toBeFalse();
    });

    it("getTitle should return correct title when selectedPage matches", () => {
        const mockTranslate = TestBed.inject(TranslateService) as any;
        mockTranslate.instant = jasmine.createSpy().and.callFake((key: string) => {
            if (key === "welcome-page.title") return "Welcome";
            if (key === "common.active-page") return "Active";
            return key;
        });
        component.selectedOrganization = { name: "Org" } as any;
        component.selectedPage.set("welcome-page");
        const result = component.getTitle("welcome-page.title", "welcome-page");
        expect(result).toContain("Welcome");
        expect(result).toContain("Active");
    });

    it("getTitle should return correct title when selectedPage does not match", () => {
        const mockTranslate = TestBed.inject(TranslateService) as any;
        mockTranslate.instant = jasmine.createSpy().and.callFake((key: string) => {
            if (key === "welcome-page.title") return "Welcome";
            return key;
        });
        component.selectedOrganization = { name: "Org" } as any;
        component.selectedPage.set("other-page");
        const result = component.getTitle("welcome-page.title", "welcome-page");
        expect(result).toBe("Welcome");
    });
});
