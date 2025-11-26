import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Constants } from "src/constants";
import { DigitalServiceVersionResponse } from "../../interfaces/digital-service-version.interface";
import { DigitalServiceVersionDataService } from "./digital-service-version-data-service";

describe("DigitalServiceVersionDataService", () => {
    let service: DigitalServiceVersionDataService;
    let httpMock: HttpTestingController;

    const endpoint = Constants.ENDPOINTS.digitalServicesVersions;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [DigitalServiceVersionDataService],
        });

        service = TestBed.inject(DigitalServiceVersionDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify(); // ensures no pending HTTP requests
    });

    it("should fetch versions by dsvUid", () => {
        const dsvUid = "12345";
        const mockResponse: DigitalServiceVersionResponse[] = [
            {
                digitalServiceUid: "1",
                digitalServiceVersionUid: "2",
                versionName: "Version 1",
                versionType: "Type 1",
            },
            {
                digitalServiceUid: "2",
                digitalServiceVersionUid: "3",
                versionName: "Version 2",
                versionType: "Type 2",
            },
        ];

        service.getVersions(dsvUid).subscribe((res) => {
            expect(res).toEqual(mockResponse);
        });

        const req = httpMock.expectOne(`${endpoint}/${dsvUid}/manage-versions`);
        expect(req.request.method).toBe("GET");

        req.flush(mockResponse);
    });
});
