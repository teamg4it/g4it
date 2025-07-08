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
});
