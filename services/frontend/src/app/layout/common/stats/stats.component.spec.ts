import { ComponentFixture, TestBed } from "@angular/core/testing";
import { StatsComponent } from "./stats.component";

describe("StatsComponent", () => {
    let component: StatsComponent;
    let fixture: ComponentFixture<StatsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [StatsComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(StatsComponent);
        component = fixture.componentInstance;
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should accept input values", () => {
        component.title = "My stats";
        component.icon = "eco";
        component.stats = [
            { label: "CO2", value: 10 },
            { label: "Water", value: 20 },
        ] as any;

        fixture.detectChanges();

        expect(component.title).toBe("My stats");
        expect(component.icon).toBe("eco");
        expect(component.stats.length).toBe(2);
    });
});
