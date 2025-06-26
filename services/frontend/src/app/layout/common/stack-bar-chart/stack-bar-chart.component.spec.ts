import { TestBed } from "@angular/core/testing";
import { TranslateModule } from "@ngx-translate/core";
import { SharedModule } from "src/app/core/shared/shared.module";
import { StackBarChartComponent } from "./stack-bar-chart.component";

describe("StackBarChartComponent", () => {
    let component: StackBarChartComponent;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [StackBarChartComponent],
            imports: [TranslateModule.forRoot(), SharedModule],
        }).compileComponents();

        const fixture = TestBed.createComponent(StackBarChartComponent);
        component = fixture.componentInstance;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
