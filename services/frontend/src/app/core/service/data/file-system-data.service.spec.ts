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
    let organization: string = "SSG";
    let inventoryDate = 2;

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

    it("postFilepostFileSystemUploadCSV(any(),any()) should work", () => {
        const formData = new FormData();
        fileSystemService.postFileSystemUploadFile(inventoryDate, formData).subscribe();

        const req = httpMock.expectOne(`inventories/${inventoryDate}/files`);
        expect(req.request.method).toEqual("POST");
        req.flush(formData);
        httpMock.verify();
    });
});
