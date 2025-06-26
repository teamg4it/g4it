import { TestBed } from "@angular/core/testing";
import { StackBarChartComponent } from "./stack-bar-chart.component";

describe("StackBarChartComponent", () => {
    let component: StackBarChartComponent;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [StackBarChartComponent],
        }).compileComponents();

        const fixture = TestBed.createComponent(StackBarChartComponent);
        component = fixture.componentInstance;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
