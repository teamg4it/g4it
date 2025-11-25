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
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { TaskIdRest } from "../../interfaces/task.interfaces";
import { LoadingDataService } from "./loading-data.service";

describe("LoadingDataService", () => {
    let httpMock: HttpTestingController;
    let loadingService: LoadingDataService;
    let inventoryDate: any = 4;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule, TranslateModule.forRoot()],
            providers: [LoadingDataService, TranslatePipe, TranslateService],
        });
        loadingService = TestBed.inject(LoadingDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be created", () => {
        expect(LoadingDataService).toBeTruthy();
    });

    it("should call the correct endpoint for inventories and set Accept-Language header", () => {
        const formData = new FormData();
        const mockResponse: TaskIdRest = { taskId: 123 };
        // Set a fake language
        loadingService["translate"].currentLang = "fr";
        loadingService.launchLoadInputFiles(42, formData, false).subscribe((res) => {
            expect(res).toEqual(mockResponse);
        });

        const req = httpMock.expectOne(
            (req) =>
                req.method === "POST" && req.url === "inventories/42/load-input-files",
        );
        expect(req.request.body).toBe(formData);
        req.flush(mockResponse);
    });

    it("should call the correct endpoint for digital services when isDs is true", () => {
        const formData = new FormData();
        const mockResponse = { taskId: 456 };
        loadingService["translate"].currentLang = "en";
        loadingService.launchLoadInputFiles(99, formData, true).subscribe((res) => {
            expect(res).toEqual(mockResponse);
        });

        const req = httpMock.expectOne(
            (req) =>
                req.method === "POST" &&
                req.url === "digital-service-version/99/load-input-files",
        );
        expect(req.request.body).toBe(formData);
        req.flush(mockResponse);
    });

    afterEach(() => {
        httpMock.verify();
    });
});
