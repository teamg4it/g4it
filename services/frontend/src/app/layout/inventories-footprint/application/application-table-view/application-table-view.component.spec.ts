import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateService } from "@ngx-translate/core";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { ApplicationTableViewComponent } from "./application-table-view.component";

describe("ApplicationTableViewComponent", () => {
    let component: ApplicationTableViewComponent;
    let fixture: ComponentFixture<ApplicationTableViewComponent>;

    let mockTranslateService: jasmine.SpyObj<TranslateService>;
    let mockFootprintStore: any;
    let mockFilterService: jasmine.SpyObj<FilterService>;

    beforeEach(async () => {
        mockTranslateService = jasmine.createSpyObj("TranslateService", ["instant"]);
        mockFilterService = jasmine.createSpyObj("FilterService", ["getFilterincludes"]);

        mockFootprintStore = {
            applicationSelectedFilters: jasmine.createSpy().and.returnValue({}),
            applicationCriteria: jasmine.createSpy().and.returnValue("CRITERIA_1"),
        };

        await TestBed.configureTestingModule({
            imports: [ApplicationTableViewComponent],
            providers: [
                { provide: TranslateService, useValue: mockTranslateService },
                { provide: FootprintStoreService, useValue: mockFootprintStore },
                { provide: FilterService, useValue: mockFilterService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ApplicationTableViewComponent);
        component = fixture.componentInstance;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    // ---------------------------
    // filterImpacts
    // ---------------------------
    it("should filter and aggregate impacts correctly", () => {
        const footprint = [
            {
                criteria: "CRITERIA_1",
                impacts: [
                    { applicationName: "App1", sip: 10, impact: 100 },
                    { applicationName: "App1", sip: 5, impact: 50 },
                ],
            },
        ] as any;

        mockFilterService.getFilterincludes.and.returnValue(true);

        mockTranslateService.instant.and.callFake((key: string) => {
            if (key.startsWith("criteria.")) {
                return { title: "Criteria 1", unite: "kg" };
            }
            return key;
        });

        const result = component.filterImpacts({}, footprint);

        expect(result.length).toBe(1);
        expect(result[0].sip).toBe(15);
        expect(result[0].impact).toBe(150);
        expect(result[0].criteria).toBe("Criteria 1");
        expect(result[0].unit).toBe("kg");
    });

    it("should exclude impacts when filter does not match", () => {
        const footprint = [
            {
                criteria: "CRITERIA_1",
                impacts: [{ applicationName: "App1", sip: 10, impact: 100 }],
            },
        ] as any;

        mockFilterService.getFilterincludes.and.returnValue(false);

        const result = component.filterImpacts({}, footprint);

        expect(result.length).toBe(0);
    });

    // ---------------------------
    // getContentText (single criteria)
    // ---------------------------
    it("should compute content text for single criteria", () => {
        mockFootprintStore.applicationCriteria.and.returnValue("CRITERIA_1");

        mockTranslateService.instant.and.callFake((key: string) => {
            if (key.includes("description")) return "Description";
            if (key.includes("scale")) return "Scale";
            if (key.includes("analysis")) return "Analysis";
            if (key.includes("to-go-further")) return "To Go Further";
            if (key.includes("module")) return "Inventory";
            return key;
        });

        const result = component.getContentText();

        expect(result.description).toBe("Description");
    });
});
