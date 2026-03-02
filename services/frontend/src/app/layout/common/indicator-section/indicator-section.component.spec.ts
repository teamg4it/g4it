import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule } from "@ngx-translate/core";

import { IndicatorSectionComponent } from "./indicator-section.component";

describe("IndicatorSectionComponent", () => {
    let component: IndicatorSectionComponent;
    let fixture: ComponentFixture<IndicatorSectionComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [IndicatorSectionComponent, TranslateModule.forRoot()],
        }).compileComponents();

        fixture = TestBed.createComponent(IndicatorSectionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
