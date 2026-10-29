import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateService } from "@ngx-translate/core";
import { of } from "rxjs";
import { ApplicationIndicatorsPage } from "src/app/core/interfaces/footprint.interface";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { ApplicationTableViewComponent } from "./application-table-view.component";

describe("ApplicationTableViewComponent", () => {
    let component: ApplicationTableViewComponent;
    let fixture: ComponentFixture<ApplicationTableViewComponent>;

    let mockTranslateService: jasmine.SpyObj<TranslateService>;
    let mockFootprintStore: any;
    let mockFilterService: jasmine.SpyObj<FilterService>;
    let mockFootprintService: jasmine.SpyObj<FootprintService>;
    let mockFootprintDataService: jasmine.SpyObj<FootprintDataService>;

    beforeEach(async () => {
        mockTranslateService = jasmine.createSpyObj("TranslateService", ["instant"]);
        mockFilterService = jasmine.createSpyObj("FilterService", ["getFilterincludes"]);
        mockFootprintService = jasmine.createSpyObj("FootprintService", [
            "mapApplicationSummaryFilters",
        ]);
        mockFootprintService.mapApplicationSummaryFilters.and.returnValue({});
        mockFootprintDataService = jasmine.createSpyObj("FootprintDataService", [
            "getApplicationIndicatorsPage",
        ]);
        mockFootprintDataService.getApplicationIndicatorsPage.and.returnValue(
            of({
                content: [],
                pageNumber: 0,
                pageSize: 10,
                totalElements: 0,
                totalPages: 0,
            }),
        );

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
                { provide: FootprintService, useValue: mockFootprintService },
                { provide: FootprintDataService, useValue: mockFootprintDataService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ApplicationTableViewComponent);
        component = fixture.componentInstance;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("loads server pages using the selected criterion and displays page totals", () => {
        const page: ApplicationIndicatorsPage = {
            content: [
                {
                    applicationName: "App1",
                    criteria: "CRITERIA_1",
                    sip: 10,
                    impact: 100,
                    unit: "kg",
                },
            ],
            pageNumber: 0,
            pageSize: 10,
            totalElements: 25,
            totalPages: 3,
        };
        mockFootprintDataService.getApplicationIndicatorsPage.and.returnValue(of(page));
        mockTranslateService.instant.and.callFake((key: string) => {
            if (key === "criteria.CRITERIA_1") {
                return { title: "Criteria 1", unite: "kg" };
            }
            return key;
        });
        fixture.componentRef.setInput("inventoryId", 42);
        fixture.componentRef.setInput("isMulticriteria", false);
        fixture.componentRef.setInput("filterMetadata", {
            environment: [],
            equipmentType: [],
            lifeCycle: [],
            domains: [],
        });
        fixture.detectChanges();

        expect(
            mockFootprintDataService.getApplicationIndicatorsPage,
        ).toHaveBeenCalledWith(42, { criterion: ["CRITERIA_1"] }, 0, 10, "sip", "desc");
        expect(component.tableView()[0].criteria).toBe("Criteria 1");
        expect(component.totalRecords()).toBe(25);

        component.onPageChange({ first: 10, rows: 10 });
        fixture.detectChanges();
        expect(
            mockFootprintDataService.getApplicationIndicatorsPage,
        ).toHaveBeenCalledWith(42, { criterion: ["CRITERIA_1"] }, 1, 10, "sip", "desc");
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

        expect(result).toHaveSize(1);
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

        expect(result).toHaveSize(0);
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
