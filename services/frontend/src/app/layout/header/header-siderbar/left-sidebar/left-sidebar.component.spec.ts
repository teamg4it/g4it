import { ElementRef } from "@angular/core";
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

    it("getTitle should return correct title when selectedPage matches", () => {
        const mockTranslate = TestBed.inject(TranslateService) as any;
        mockTranslate.instant = jasmine.createSpy().and.callFake((key: string) => {
            if (key === "welcome-page.title") return "Welcome";
            if (key === "common.active-page") return "Active";
            return key;
        });
        component.selectedWorkspace = { name: "Org" } as any;
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
        component.selectedWorkspace = { name: "Org" } as any;
        component.selectedPage.set("other-page");
        const result = component.getTitle("welcome-page.title", "welcome-page");
        expect(result).toBe("Welcome");
    });
});
