import { ComponentFixture, TestBed } from "@angular/core/testing";
import { NoopAnimationsModule } from "@angular/platform-browser/animations";
import { TranslateModule } from "@ngx-translate/core";

import { ConfigureViewFiltersComponent } from "./configure-view-filters.component";

describe("ConfigureViewFiltersComponent", () => {
    let component: ConfigureViewFiltersComponent;
    let fixture: ComponentFixture<ConfigureViewFiltersComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ConfigureViewFiltersComponent,
                TranslateModule.forRoot(),
                NoopAnimationsModule,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ConfigureViewFiltersComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    describe("ngOnInit", () => {
        it("should initialize formGroup with default values when inputs are not provided", () => {
            component.ngOnInit();

            expect(component.formGroup).toBeDefined();
            expect(component.formGroup.get("dataConsistencyCheckbox")?.value).toBe(false);
            expect(component.formGroup.get("unitType")?.value).toBe("Raw");
        });

        it("should initialize formGroup with enableDataInconsistency input value", () => {
            fixture.componentRef.setInput("enableDataInconsistency", true);
            component.ngOnInit();

            expect(component.formGroup.get("dataConsistencyCheckbox")?.value).toBe(true);
        });

        it("should initialize formGroup with selectedUnit input value", () => {
            fixture.componentRef.setInput("selectedUnit", "PeopleEq");
            component.ngOnInit();

            expect(component.formGroup.get("unitType")?.value).toBe("PeopleEq");
        });

        it("should initialize formGroup with both input values", () => {
            fixture.componentRef.setInput("enableDataInconsistency", true);
            fixture.componentRef.setInput("selectedUnit", "CustomUnit");
            component.ngOnInit();

            expect(component.formGroup.get("dataConsistencyCheckbox")?.value).toBe(true);
            expect(component.formGroup.get("unitType")?.value).toBe("CustomUnit");
        });
    });

    describe("apply", () => {
        beforeEach(() => {
            component.ngOnInit();
        });

        it("should update enableConsistency and unitType from form values", () => {
            component.formGroup.patchValue({
                dataConsistencyCheckbox: true,
                unitType: "PeopleEq",
            });

            component.apply();

            expect(component.enableConsistency).toBe(true);
            expect(component.unitType).toBe("PeopleEq");
        });

        it("should emit filtersApplied event with correct values", () => {
            spyOn(component.filtersApplied, "emit");

            component.formGroup.patchValue({
                dataConsistencyCheckbox: true,
                unitType: "CustomUnit",
            });

            component.apply();

            expect(component.filtersApplied.emit).toHaveBeenCalledWith({
                enableConsistency: true,
                unitType: "CustomUnit",
            });
        });

        it("should emit sidebarVisibleChange event with false", () => {
            spyOn(component.sidebarVisibleChange, "emit");

            component.apply();

            expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
        });

        it("should handle false checkbox value", () => {
            spyOn(component.filtersApplied, "emit");

            component.formGroup.patchValue({
                dataConsistencyCheckbox: false,
                unitType: "Raw",
            });

            component.apply();

            expect(component.enableConsistency).toBe(false);
            expect(component.filtersApplied.emit).toHaveBeenCalledWith({
                enableConsistency: false,
                unitType: "Raw",
            });
        });

        it("should emit both events when apply is called", () => {
            spyOn(component.filtersApplied, "emit");
            spyOn(component.sidebarVisibleChange, "emit");

            component.apply();

            expect(component.filtersApplied.emit).toHaveBeenCalled();
            expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
        });
    });

    describe("closeSidebar", () => {
        it("should emit sidebarVisibleChange event with false", () => {
            spyOn(component.sidebarVisibleChange, "emit");

            component.closeSidebar();

            expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
        });

        it("should only emit sidebarVisibleChange event", () => {
            spyOn(component.sidebarVisibleChange, "emit");
            spyOn(component.filtersApplied, "emit");

            component.closeSidebar();

            expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
            expect(component.filtersApplied.emit).not.toHaveBeenCalled();
        });
    });
});
