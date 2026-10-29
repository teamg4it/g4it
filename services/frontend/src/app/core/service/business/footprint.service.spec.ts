import { HttpClientTestingModule } from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { Constants } from "src/constants";
import {
    ApplicationFiltersRest,
    TransformedDomain,
} from "../../interfaces/filter.interface";
import { ApplicationImpact } from "../../interfaces/footprint.interface";
import { OutApplicationsRest } from "../../interfaces/output.interface";
import { FootprintService } from "./footprint.service";

describe("FootprintService", () => {
    let service: FootprintService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule, TranslateModule.forRoot()],
        });
        service = TestBed.inject(FootprintService);
    });

    describe("mapApplicationSummaryFilters", () => {
        const metadata: ApplicationFiltersRest = {
            environment: ["PRODUCTION", "PreProduction", ""],
            equipmentType: ["Server"],
            lifeCycle: ["FABRICATION", "USING"],
            domains: [
                { domain: "Finance", subDomains: ["Billing", "Accounts"] },
                { domain: "HR", subDomains: ["Payroll"] },
            ],
        };

        it("sends only selected values and omits the All sentinel", () => {
            expect(
                service.mapApplicationSummaryFilters(
                    {
                        environment: [Constants.ALL, "PRODUCTION"],
                        equipmentType: [Constants.ALL],
                        lifeCycle: [Constants.ALL],
                        domain: [
                            { label: Constants.ALL, checked: true } as TransformedDomain,
                        ],
                    },
                    metadata,
                ),
            ).toEqual({
                environment: ["PRODUCTION"],
                equipmentType: [],
                lifeCycle: [],
                domain: [],
                subDomain: [],
            });
        });

        it("maps translated lifecycle labels to exact metadata values", () => {
            spyOn(TestBed.inject(TranslateService), "instant").and.callFake(
                (key: string) =>
                    ({
                        "acvStep.MANUFACTURING": "Manufacturing",
                        "acvStep.USING": "Use",
                    })[key] ?? key,
            );
            expect(
                service.mapApplicationSummaryFilters(
                    {
                        environment: ["PreProduction"],
                        equipmentType: ["Server"],
                        lifeCycle: ["Manufacturing", "Use"],
                    },
                    metadata,
                ),
            ).toEqual({
                environment: ["PreProduction"],
                equipmentType: ["Server"],
                lifeCycle: ["FABRICATION", "USING"],
                domain: [],
                subDomain: [],
            });
        });

        it("keeps unselected dimensions empty when another dimension is selected", () => {
            expect(
                service.mapApplicationSummaryFilters(
                    {
                        environment: ["PRODUCTION"],
                        equipmentType: [Constants.ALL, "Server"],
                        lifeCycle: [Constants.ALL],
                        domain: [
                            { label: Constants.ALL, checked: true } as TransformedDomain,
                        ],
                    },
                    metadata,
                ),
            ).toEqual({
                environment: ["PRODUCTION"],
                equipmentType: ["Server"],
                lifeCycle: [],
                domain: [],
                subDomain: [],
            });
        });

        it("includes every filter key when filters are empty", () => {
            expect(service.mapApplicationSummaryFilters({}, metadata)).toEqual({
                environment: [],
                equipmentType: [],
                lifeCycle: [],
                domain: [],
                subDomain: [],
            });
        });

        it("preserves an empty environment selection when other filters are unrestricted", () => {
            expect(
                service.mapApplicationSummaryFilters(
                    {
                        environment: [],
                        equipmentType: [Constants.ALL],
                        lifeCycle: [Constants.ALL],
                        domain: [
                            { label: Constants.ALL, checked: true } as TransformedDomain,
                        ],
                    },
                    metadata,
                ),
            ).toEqual({
                environment: [],
                equipmentType: [],
                lifeCycle: [],
                domain: [],
                subDomain: [],
            });
        });

        it("maps selected domain children without sending tree properties", () => {
            expect(
                service.mapApplicationSummaryFilters(
                    {
                        domain: [
                            { label: Constants.ALL, checked: false },
                            {
                                label: "Finance",
                                checked: true,
                                children: [
                                    { label: "Billing", checked: true },
                                    { label: "Accounts", checked: false },
                                ],
                            },
                            { label: "HR", checked: false, children: [] },
                        ] as TransformedDomain[],
                    },
                    metadata,
                ),
            ).toEqual({
                environment: [],
                equipmentType: [],
                lifeCycle: [],
                domain: ["Finance"],
                subDomain: ["Billing"],
            });
        });

        it("retains supported drill scope and sends no application or criteria fields", () => {
            expect(
                service.mapApplicationSummaryFilters(
                    {
                        environment: ["PRODUCTION"],
                        criteria: ["climate-change"],
                        applicationName: ["AppX"],
                    },
                    metadata,
                    { domain: "Finance", subDomain: "Billing" },
                ),
            ).toEqual({
                environment: ["PRODUCTION"],
                equipmentType: [],
                lifeCycle: [],
                domain: ["Finance"],
                subDomain: ["Billing"],
            });
        });

        it("preserves the exact empty backend value behind its UI label", () => {
            expect(
                service.mapApplicationSummaryFilters(
                    {
                        environment: [Constants.UNSPECIFIED],
                    },
                    metadata,
                ),
            ).toEqual({
                environment: [""],
                equipmentType: [],
                lifeCycle: [],
                domain: [],
                subDomain: [],
            });
        });
    });

    describe("getTransformOutApplications", () => {
        it("should handle empty input array", () => {
            const outApplications: OutApplicationsRest[] = [];
            const expected: ApplicationImpact[] = [];
            const result = service.getTransformOutApplications(outApplications);
            expect(result).toEqual(expected);
        });
    });
});
