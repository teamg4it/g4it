/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { WorkspaceReferenceDataService } from "./workspace-reference-data.service";

describe("WorkspaceReferenceDataService", () => {
    let service: WorkspaceReferenceDataService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [WorkspaceReferenceDataService],
        });
        service = TestBed.inject(WorkspaceReferenceDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it("should create", () => {
        expect(service).toBeTruthy();
    });

    it("should return all supported CSV import endpoints", () => {
        expect(service.getWorkspaceCsvEndpoints()).toEqual([
            {
                name: "itemType",
                url: "referential-workspace/itemType/csv",
                label: "Item Type",
            },
            {
                name: "itemImpact",
                url: "referential-workspace/itemImpact/csv",
                label: "Item Impact",
            },
            {
                name: "matchingItem",
                url: "referential-workspace/matchingItem/csv",
                label: "Matching Item",
            },
        ]);
    });

    it("should upload a CSV file to the selected workspace endpoint", () => {
        const file = new File(["id,name"], "items.csv", { type: "text/csv" });

        service.workspaceUploadCsvFile("itemImpact", file, 12, "G4IT").subscribe();

        const request = httpMock.expectOne(
            "organizations/G4IT/workspaces/12/referential-workspace/itemImpact/csv",
        );
        expect(request.request.method).toBe("POST");
        expect(request.request.body instanceof FormData).toBeTrue();
        expect((request.request.body as FormData).get("file")).toBe(file);
        request.flush({});
    });

    it("should reject uploads to an unknown endpoint", () => {
        const file = new File(["id,name"], "items.csv", { type: "text/csv" });

        expect(() =>
            service.workspaceUploadCsvFile("unknown", file, 12, "G4IT"),
        ).toThrowError("Endpoint unknown not found");
    });

    it("should download workspace reference data as a ZIP blob", () => {
        const zip = new Blob(["zip"], { type: "application/zip" });

        service.workspaceDownloadZipFile(12, "G4IT").subscribe((result) => {
            expect(result).toBe(zip);
        });

        const request = httpMock.expectOne(
            "organizations/G4IT/workspaces/12/referential-workspace/csv",
        );
        expect(request.request.method).toBe("GET");
        expect(request.request.responseType).toBe("blob");
        request.flush(zip);
    });

    it("should create a ZIP filename from the workspace name", () => {
        expect(service.getZipFileName("Main workspace")).toBe(
            "workspace_reference_data_Main workspace.zip",
        );
    });
});
