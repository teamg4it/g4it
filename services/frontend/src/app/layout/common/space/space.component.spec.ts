import { CommonModule } from "@angular/common";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { of } from "rxjs";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { UserService } from "src/app/core/service/business/user.service";
import { SpaceComponent } from "./space.component";

describe("SpaceComponent", () => {
    let component: SpaceComponent;
    let fixture: ComponentFixture<SpaceComponent>;
    let mockAdministrationService: jasmine.SpyObj<AdministrationService>;
    let mockUserService: jasmine.SpyObj<UserService>;

    beforeEach(async () => {
        mockAdministrationService = jasmine.createSpyObj("AdministrationService", [
            "getUsers",
        ]);
        mockAdministrationService.getUsers.and.returnValue(of({})); // Ensure it returns an observable

        mockUserService = jasmine.createSpyObj("UserService", ["getRoles"]);
        mockUserService.getRoles.and.returnValue([]); // Ensure it returns a valid value

        await TestBed.configureTestingModule({
            declarations: [SpaceComponent],
            imports: [
                ReactiveFormsModule,
                CommonModule,
                FormsModule,
                DropdownModule,
                ButtonModule,
            ],
            providers: [
                { provide: AdministrationService, useValue: mockAdministrationService },
                { provide: UserService, useValue: mockUserService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(SpaceComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize with default spaceDetails and call getUsers", () => {
        spyOn(component, "getUsers");
        component.ngOnInit();
        expect(component.getUsers).toHaveBeenCalled();
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

    it("should log the form when createSpace is called", () => {
        spyOn(console, "log");
        component.createSpace();
        expect(console.log).toHaveBeenCalledWith(component.spaceForm);
    });

    it("should log the form when createInventory is called", () => {
        spyOn(console, "log");
        component.createInventory();
        expect(console.log).toHaveBeenCalledWith(component.spaceForm);
    });
});
