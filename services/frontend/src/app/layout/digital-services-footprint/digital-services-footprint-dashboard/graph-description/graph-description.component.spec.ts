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
});
