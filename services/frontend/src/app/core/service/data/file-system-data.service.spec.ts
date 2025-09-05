/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { TestBed } from "@angular/core/testing";

import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { FileSystemDataService } from "./file-system-data.service";

describe("FileSystemDataService", () => {
    let httpMock: HttpTestingController;
    let fileSystemService: FileSystemDataService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [FileSystemDataService],
        });
        fileSystemService = TestBed.inject(FileSystemDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be created", () => {
        expect(fileSystemService).toBeTruthy();
    });

    it("should call the correct endpoint with proper headers and responseType when downloadResultsFile is called", () => {
        const taskId = "12345";
        const mockBlob = new Blob(["test"], { type: "application/zip" });

        fileSystemService.downloadResultsFile(taskId).subscribe((response) => {
            expect(response).toEqual(mockBlob);
        });

        const req = httpMock.expectOne(
            (request) =>
                request.url.endsWith(`download-reject/${taskId}`) &&
                request.method === "GET",
        );
        expect(req.request.headers.get("Accept")).toBe("application/zip");
        expect(req.request.responseType).toBe("blob");

        req.flush(mockBlob);
        httpMock.verify();
    });
});
