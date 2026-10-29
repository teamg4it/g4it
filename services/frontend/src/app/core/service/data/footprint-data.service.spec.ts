import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Constants } from "src/constants";
import { FootprintDataService } from "./footprint-data.service";

describe("FootprintDataService application summaries", () => {
    let service: FootprintDataService;
    let http: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
        service = TestBed.inject(FootprintDataService);
        http = TestBed.inject(HttpTestingController);
    });

    afterEach(() => http.verify());

    it("posts supported filters as JSON and returns server aggregates unchanged", () => {
        const filters = {
            environment: ["PRODUCTION", "PREPRODUCTION"],
            equipmentType: ["Server"],
            lifeCycle: ["USING"],
            domain: ["Finance"],
            subDomain: ["Billing"],
        };
        const response = [
            {
                criteria: "climate-change",
                unit: "kg CO2 eq",
                impact: 12,
                sip: 3,
                countValue: 5,
            },
        ];
        service.getApplicationMultiCriteriaImpacts(42, filters).subscribe((result) => {
            expect(result).toEqual(response);
        });
        const request = http.expectOne(
            `${Constants.ENDPOINTS.inventories}/42/indicators/applications/multi-criteria-impacts`,
        );
        expect(request.request.method).toBe("POST");
        expect(request.request.body).toEqual(filters);
        expect(request.request.params.keys()).toEqual([]);
        request.flush(response);
    });

    it("posts an empty object when all filters are unrestricted", () => {
        service.getApplicationMultiCriteriaImpacts(42, {}).subscribe();
        const request = http.expectOne(
            `${Constants.ENDPOINTS.inventories}/42/indicators/applications/multi-criteria-impacts`,
        );
        expect(request.request.method).toBe("POST");
        expect(request.request.body).toEqual({});
        request.flush([]);
    });

    it("posts table filters and pagination parameters", () => {
        const filters = {
            environment: ["PRODUCTION"],
            criterion: ["climate-change"],
        };
        const response = {
            content: [
                {
                    applicationName: "App1",
                    criteria: "climate-change",
                    sip: 2,
                    impact: 12,
                    unit: "kg CO2 eq",
                },
            ],
            pageNumber: 1,
            pageSize: 20,
            totalElements: 21,
            totalPages: 2,
        };

        service
            .getApplicationIndicatorsPage(42, filters, 1, 20, "sip", "desc")
            .subscribe((result) => {
                expect(result).toEqual(response);
            });
        const request = http.expectOne(
            `${Constants.ENDPOINTS.inventories}/42/indicators/applications?page=1&size=20&sort=sip,desc`,
        );
        expect(request.request.method).toBe("POST");
        expect(request.request.body).toEqual(filters);
        request.flush(response);
    });

    it("posts the graph level, repartition, criteria, and filters", () => {
        const graphRequest = {
            criteria: ["climate-change", "acidification"],
            graphLevel: "global" as const,
            repartition: "equipmentType" as const,
            filters: {
                environment: ["PRODUCTION"],
                domain: ["Finance"],
                applicationName: ["AppX"],
            },
        };
        const response = [
            {
                criteria: "climate-change",
                unit: "kg CO2 eq",
                nodes: [
                    {
                        impact: 12,
                        sip: 3,
                        countValue: 5,
                        consistentCount: 4,
                        inconsistentCount: 1,
                        repartitions: [
                            {
                                label: "Server",
                                impact: 12,
                                sip: 3,
                                countValue: 5,
                                consistentCount: 4,
                                inconsistentCount: 1,
                            },
                        ],
                    },
                ],
            },
        ];

        service.getApplicationMultiCriteriaGraph(42, graphRequest).subscribe((result) => {
            expect(result).toEqual(response);
        });
        const request = http.expectOne(
            `${Constants.ENDPOINTS.inventories}/42/indicators/applications/multi-criteria`,
        );
        expect(request.request.method).toBe("POST");
        expect(request.request.body).toEqual(graphRequest);
        request.flush(response);
    });
});
