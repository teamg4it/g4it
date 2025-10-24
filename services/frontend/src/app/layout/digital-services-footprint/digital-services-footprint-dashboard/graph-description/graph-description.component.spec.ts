import { ComponentFixture, TestBed } from "@angular/core/testing";

import { TranslateModule } from "@ngx-translate/core";
import { GraphDescriptionComponent } from "./graph-description.component";

describe("GraphDescriptionComponent", () => {
    let component: GraphDescriptionComponent;
    let fixture: ComponentFixture<GraphDescriptionComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [GraphDescriptionComponent],
            imports: [TranslateModule.forRoot()],
        }).compileComponents();

        fixture = TestBed.createComponent(GraphDescriptionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should toggle contentVisible from false to true", () => {
        expect(component.contentVisible).toBeFalse();
        component.toggleContentVisibility();
        expect(component.contentVisible).toBeTrue();
    });

    it("should toggle contentVisible from true to false", () => {
        component.contentVisible = true;
        component.toggleContentVisibility();
        expect(component.contentVisible).toBeFalse();
    });
});
