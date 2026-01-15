/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    HttpTestingController,
    provideHttpClientTesting,
} from "@angular/common/http/testing";
import { provideHttpClient } from "@angular/common/http";
import { TestBed } from "@angular/core/testing";
import { Constants } from "src/constants";
import { CsvImportDataService, CsvImportEndpoint } from "./api-route-referential.service";

describe("CsvImportDataService", () => {
    let service: CsvImportDataService;
    let httpMock: HttpTestingController;

    const endpoint = Constants.ENDPOINTS.referential;

    const expectedEndpoints: CsvImportEndpoint[] = [
        { name: "itemImpact", url: `${endpoint}/itemImpact/csv`, label: "Item Impact" },
        { name: "criterion", url: `${endpoint}/criterion/csv`, label: "Criterion" },
        {
            name: "lifecycleStep",
            url: `${endpoint}/lifecycleStep/csv`,
            label: "Lifecycle Step",
        },
        { name: "hypothesis", url: `${endpoint}/hypothesis/csv`, label: "Hypothesis" },
        { name: "itemType", url: `${endpoint}/itemType/csv`, label: "Item Type" },
        {
            name: "matchingItem",
            url: `${endpoint}/matchingItem/csv`,
            label: "Matching Item",
        },
    ];

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                provideHttpClient(),
                provideHttpClientTesting(),
                CsvImportDataService,
            ],
        });

        service = TestBed.inject(CsvImportDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        // Vérifie qu'il n'y a pas de requêtes HTTP en attente
        httpMock.verify();
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    describe("getCsvEndpoints", () => {
        it("should return all 6 CSV endpoints", () => {
            const endpoints = service.getCsvEndpoints();

            expect(endpoints.length).toBe(6);
            expect(endpoints).toEqual(expectedEndpoints);
        });

        it("should return itemImpact endpoint", () => {
            const endpoints = service.getCsvEndpoints();
            const itemImpact = endpoints.find((e) => e.name === "itemImpact");

            expect(itemImpact).toBeDefined();
            expect(itemImpact?.url).toBe(`${endpoint}/itemImpact/csv`);
            expect(itemImpact?.label).toBe("Item Impact");
        });

        it("should return criterion endpoint", () => {
            const endpoints = service.getCsvEndpoints();
            const criterion = endpoints.find((e) => e.name === "criterion");

            expect(criterion).toBeDefined();
            expect(criterion?.url).toBe(`${endpoint}/criterion/csv`);
            expect(criterion?.label).toBe("Criterion");
        });

        it("should return lifecycleStep endpoint", () => {
            const endpoints = service.getCsvEndpoints();
            const lifecycleStep = endpoints.find((e) => e.name === "lifecycleStep");

            expect(lifecycleStep).toBeDefined();
            expect(lifecycleStep?.url).toBe(`${endpoint}/lifecycleStep/csv`);
            expect(lifecycleStep?.label).toBe("Lifecycle Step");
        });

        it("should return hypothesis endpoint", () => {
            const endpoints = service.getCsvEndpoints();
            const hypothesis = endpoints.find((e) => e.name === "hypothesis");

            expect(hypothesis).toBeDefined();
            expect(hypothesis?.url).toBe(`${endpoint}/hypothesis/csv`);
            expect(hypothesis?.label).toBe("Hypothesis");
        });

        it("should return itemType endpoint", () => {
            const endpoints = service.getCsvEndpoints();
            const itemType = endpoints.find((e) => e.name === "itemType");

            expect(itemType).toBeDefined();
            expect(itemType?.url).toBe(`${endpoint}/itemType/csv`);
            expect(itemType?.label).toBe("Item Type");
        });

        it("should return matchingItem endpoint", () => {
            const endpoints = service.getCsvEndpoints();
            const matchingItem = endpoints.find((e) => e.name === "matchingItem");

            expect(matchingItem).toBeDefined();
            expect(matchingItem?.url).toBe(`${endpoint}/matchingItem/csv`);
            expect(matchingItem?.label).toBe("Matching Item");
        });
    });

    describe("uploadCsvFile", () => {
        const mockFile = new File(["test,data"], "test.csv", { type: "text/csv" });
        const mockResponse = { importedLineNumber: 10, errors: [] };

        it("should upload file to itemImpact endpoint", () => {
            service.uploadCsvFile("itemImpact", mockFile).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            const req = httpMock.expectOne(`${endpoint}/itemImpact/csv`);
            expect(req.request.method).toBe("POST");
            expect(req.request.body instanceof FormData).toBeTrue();
            req.flush(mockResponse);
        });

        it("should upload file to criterion endpoint", () => {
            service.uploadCsvFile("criterion", mockFile).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            const req = httpMock.expectOne(`${endpoint}/criterion/csv`);
            expect(req.request.method).toBe("POST");
            expect(req.request.body instanceof FormData).toBeTrue();
            req.flush(mockResponse);
        });

        it("should upload file to lifecycleStep endpoint", () => {
            service.uploadCsvFile("lifecycleStep", mockFile).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            const req = httpMock.expectOne(`${endpoint}/lifecycleStep/csv`);
            expect(req.request.method).toBe("POST");
            expect(req.request.body instanceof FormData).toBeTrue();
            req.flush(mockResponse);
        });

        it("should upload file to hypothesis endpoint", () => {
            service.uploadCsvFile("hypothesis", mockFile).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            const req = httpMock.expectOne(`${endpoint}/hypothesis/csv`);
            expect(req.request.method).toBe("POST");
            expect(req.request.body instanceof FormData).toBeTrue();
            req.flush(mockResponse);
        });

        it("should upload file to itemType endpoint", () => {
            service.uploadCsvFile("itemType", mockFile).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            const req = httpMock.expectOne(`${endpoint}/itemType/csv`);
            expect(req.request.method).toBe("POST");
            expect(req.request.body instanceof FormData).toBeTrue();
            req.flush(mockResponse);
        });

        it("should upload file to matchingItem endpoint", () => {
            service.uploadCsvFile("matchingItem", mockFile).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            const req = httpMock.expectOne(`${endpoint}/matchingItem/csv`);
            expect(req.request.method).toBe("POST");
            expect(req.request.body instanceof FormData).toBeTrue();
            req.flush(mockResponse);
        });

        it("should throw error for unknown endpoint", () => {
            expect(() => service.uploadCsvFile("unknownEndpoint", mockFile)).toThrowError(
                "Endpoint unknownEndpoint not found",
            );
        });

        it("should handle server error response", () => {
            const errorResponse = { status: 500, statusText: "Internal Server Error" };

            service.uploadCsvFile("itemImpact", mockFile).subscribe({
                next: () => fail("Expected an error"),
                error: (error) => {
                    expect(error.status).toBe(500);
                },
            });

            const req = httpMock.expectOne(`${endpoint}/itemImpact/csv`);
            req.flush("Server error", errorResponse);
        });

        it("should handle validation errors in response", () => {
            const responseWithErrors = {
                importedLineNumber: 5,
                errors: ["Line 2: Invalid format", "Line 5: Missing field"],
            };

            service.uploadCsvFile("criterion", mockFile).subscribe((response) => {
                expect(response.importedLineNumber).toBe(5);
                expect(response.errors.length).toBe(2);
                expect(response.errors).toContain("Line 2: Invalid format");
            });

            const req = httpMock.expectOne(`${endpoint}/criterion/csv`);
            req.flush(responseWithErrors);
        });

        it("should send file as FormData with correct field name", () => {
            service.uploadCsvFile("itemImpact", mockFile).subscribe();

            const req = httpMock.expectOne(`${endpoint}/itemImpact/csv`);
            const formData = req.request.body as FormData;

            expect(formData.has("file")).toBeTrue();
            req.flush(mockResponse);
        });
    });
});
