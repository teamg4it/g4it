import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";

import { Constants } from "src/constants";
import { BusinessHours } from "../../interfaces/business-hours.interface";
import { VersionRest } from "../../interfaces/version.interfaces";
import { ShareUsefulInformationDataService } from "./share-useful-information-service";

describe("ShareUsefulInformationDataService", () => {
    let service: ShareUsefulInformationDataService;
    let httpMock: HttpTestingController;

    const sharedToken = "shared-token-123";
    const dsvId = "dsv-id-456";

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [ShareUsefulInformationDataService],
        });

        service = TestBed.inject(ShareUsefulInformationDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify(); // ensures no pending HTTP calls
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    describe("getVersion", () => {
        it("should call GET version API and return VersionRest", () => {
            const mockResponse: VersionRest = {
                id: "v1",
                name: "Version 1",
            } as VersionRest;

            service.getVersion(sharedToken, dsvId).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            const expectedUrl = `${Constants.ENDPOINTS.sharedDs}/${sharedToken}/${Constants.ENDPOINTS.dsv}/${dsvId}/${Constants.ENDPOINTS.version}`;

            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("GET");

            req.flush(mockResponse);
        });
    });

    describe("getBusinessHours", () => {
        it("should call GET business hours API and return BusinessHours[]", () => {
            const mockResponse: BusinessHours[] = [
                {
                    day: "MONDAY",
                    start_time: "09:00",
                    end_time: "18:00",
                },
            ] as BusinessHours[];

            service.getBusinessHours(sharedToken, dsvId).subscribe((response) => {
                expect(response.length).toBe(1);
                expect(response).toEqual(mockResponse);
            });

            const expectedUrl = `${Constants.ENDPOINTS.sharedDs}/${sharedToken}/${Constants.ENDPOINTS.dsv}/${dsvId}/${Constants.ENDPOINTS.businessHours}`;

            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("GET");

            req.flush(mockResponse);
        });
    });
});
