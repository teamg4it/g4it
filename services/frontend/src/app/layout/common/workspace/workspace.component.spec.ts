import { CommonModule } from "@angular/common";
import { HttpClientModule } from "@angular/common/http";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { of } from "rxjs";
import { UserService } from "src/app/core/service/business/user.service";
import { WorkspaceService } from "src/app/core/service/business/workspace.service";
import { WorkspaceComponent } from "./workspace.component";

describe("WorkspaceComponent", () => {
    let component: WorkspaceComponent;
    let fixture: ComponentFixture<WorkspaceComponent>;
    let mockWorkspaceService: jasmine.SpyObj<WorkspaceService>;
    let mockUserService: jasmine.SpyObj<UserService>;

    beforeEach(async () => {
        mockWorkspaceService = jasmine.createSpyObj("WorkspaceService", [
            "getDomainSubscribers",
            "setOpen", // Add setOpen to the mock
        ]);
        mockWorkspaceService.getDomainSubscribers.and.returnValue(of([])); // Ensure it returns an observable

        mockUserService = jasmine.createSpyObj("UserService", ["getRoles"]);
        mockUserService.getRoles.and.returnValue([]); // Ensure it returns a valid value

        await TestBed.configureTestingModule({
            declarations: [WorkspaceComponent],
            imports: [
                ReactiveFormsModule,
                CommonModule,
                FormsModule,
                DropdownModule,
                ButtonModule,
                TranslateModule.forRoot(),
                HttpClientModule, // Add this module
            ],
            providers: [
                { provide: WorkspaceService, useValue: mockWorkspaceService },
                { provide: UserService, useValue: mockUserService },
                MessageService,
                TranslateService,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(WorkspaceComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize with default spaceDetails and call getDomainSubscribersList", () => {
        spyOn(component, "getDomainSubscribersList");
        component.ngOnInit();
        expect(component.getDomainSubscribersList).toHaveBeenCalled();
        expect(component.selectedMenuIndex).toBe(0);
    });

    it("should select a menu and set active state correctly", () => {
        component.spaceDetails = {
            menu: [{ active: false }, { active: false }],
            form: [],
        }; // Ensure initial state is set
        component.selectTab(1);
        expect(component.selectedMenuIndex).toBe(1);
        expect(component.spaceDetails.menu[1].active).toBeTrue();
        expect(component.spaceDetails.menu[0].active).toBeFalse();
    });

    it("should navigate to the previous tab", () => {
        component.selectTab(1);
        component.previousTab(1);
        expect(component.selectedMenuIndex).toBe(0);
    });

    it("should navigate to the next tab", () => {
        component.spaceDetails = {
            menu: [{ active: true }, { active: false }],
            form: [],
        }; // Ensure initial state is set
        component.selectTab(0);
        component.nextTab(0);
        expect(component.selectedMenuIndex).toBe(1);
        expect(component.spaceDetails.menu[1].active).toBeTrue();
        expect(component.spaceDetails.menu[0].active).toBeFalse();
    });

    it("should emit sidebarVisibleChange when closeSidebar is called", () => {
        spyOn(component.sidebarVisibleChange, "emit");
        component.closeSidebar();
        expect(mockWorkspaceService.setOpen).toHaveBeenCalledWith(false); // Verify setOpen is called
        expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
    });
});
