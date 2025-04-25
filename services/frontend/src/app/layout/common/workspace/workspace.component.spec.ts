import { CommonModule } from "@angular/common";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { TranslateModule, TranslateService } from "@ngx-translate/core"; // Import TranslateStore
import { MessageService } from "primeng/api"; // Import MessageService and Message interface
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
        component.selectTab(0);
        component.nextTab(0);
        expect(component.selectedMenuIndex).toBe(1);
    });

    it("should emit sidebarVisibleChange when closeSidebar is called", () => {
        spyOn(component.sidebarVisibleChange, "emit");
        component.closeSidebar();
        expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
    });
});
