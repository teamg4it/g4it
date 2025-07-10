import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormGroup, ReactiveFormsModule } from "@angular/forms";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule } from "@ngx-translate/core";
import { SharedModule } from "src/app/core/shared/shared.module";
import { FormNavComponent } from "./form-nav.component";

describe("FormNavComponent", () => {
    let component: FormNavComponent;
    let fixture: ComponentFixture<FormNavComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FormNavComponent],
            imports: [
                HttpClientTestingModule,
                RouterTestingModule,
                ReactiveFormsModule,
                SharedModule,
                TranslateModule.forRoot(),
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(FormNavComponent);
        component = fixture.componentInstance;
        component.spaceDetails = {
            menu: [
                {
                    title: "Tab 1",
                    subTitle: "Optional",
                    description: "Description 1",
                    iconClass: "pi pi-home",
                    optional: true,
                },
            ],
        };
        component.spaceForm = new FormGroup({});

        fixture.detectChanges();
    });

    it("should create component", () => {
        expect(component).toBeTruthy();
    });

    it("should emit index when selectTab is called", () => {
        spyOn(component.tabSelected, "emit");
        component.selectTab(2);
        expect(component.tabSelected.emit).toHaveBeenCalledWith(2);
    });

    it("should call focusElement for ArrowRight", () => {
        const event = new KeyboardEvent("keydown", { key: "ArrowRight" });
        spyOn(component, "focusElement");
        component.handleKeydown(event, 0);
        expect(component.focusElement).toHaveBeenCalledWith("space-menu-item-1");
    });

    it("should call focusElement for ArrowLeft", () => {
        const event = new KeyboardEvent("keydown", { key: "ArrowLeft" });
        spyOn(component, "focusElement");
        component.handleKeydown(event, 1);
        expect(component.focusElement).toHaveBeenCalledWith("space-menu-item-0");
    });

    it("should call selectTab on Enter key", () => {
        const event = new KeyboardEvent("keydown", { key: "Enter" });
        spyOn(event, "preventDefault");
        spyOn(component, "selectTab");
        component.handleKeydown(event, 1);
        expect(component.selectTab).toHaveBeenCalledWith(1);
        expect(event.preventDefault).toHaveBeenCalled();
    });

    it("should call selectTab on Space key", () => {
        const event = new KeyboardEvent("keydown", { key: " " });
        spyOn(event, "preventDefault");
        spyOn(component, "selectTab");
        component.handleKeydown(event, 1);
        expect(component.selectTab).toHaveBeenCalledWith(1);
        expect(event.preventDefault).toHaveBeenCalled();
    });

    it("should focus element if not disabled", () => {
        const mockElement = document.createElement("div");
        mockElement.id = "space-menu-item-1";
        mockElement.classList.remove("disabled");
        spyOn(mockElement, "focus");
        document.body.appendChild(mockElement);

        component.focusElement("space-menu-item-1");
        expect(mockElement.focus).toHaveBeenCalled();

        document.body.removeChild(mockElement);
    });

    it("should not focus element if disabled", () => {
        const mockElement = document.createElement("div");
        mockElement.id = "space-menu-item-2";
        mockElement.classList.add("disabled");
        spyOn(mockElement, "focus");
        document.body.appendChild(mockElement);

        component.focusElement("space-menu-item-2");
        expect(mockElement.focus).not.toHaveBeenCalled();

        document.body.removeChild(mockElement);
    });
});
