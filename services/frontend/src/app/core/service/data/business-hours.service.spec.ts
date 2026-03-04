import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Constants } from "src/constants";
import { BusinessHours } from "../../interfaces/business-hours.interface";
import { BusinessHoursService } from "./business-hours.service";

describe("BusinessHoursService", () => {
    let service: BusinessHoursService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [BusinessHoursService],
        });
        service = TestBed.inject(BusinessHoursService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    describe("getBusinessHours", () => {
        it("should retrieve business hours from the API", () => {
            const mockBusinessHours: BusinessHours[] = [
                {
                    day: "Monday",
                    start_time: "09:00",
                    end_time: "17:00",
                },
                {
                    day: "Tuesday",
                    start_time: "09:00",
                    end_time: "17:00",
                },
                {
                    day: "Wednesday",
                    start_time: "09:00",
                    end_time: "17:00",
                },
            ];

            service.getBusinessHours().subscribe((businessHours) => {
                expect(businessHours).toEqual(mockBusinessHours);
                expect(businessHours.length).toBe(3);
                expect(businessHours[0].day).toBe("Monday");
                expect(businessHours[0].start_time).toBe("09:00");
                expect(businessHours[0].end_time).toBe("17:00");
            });

            const req = httpMock.expectOne(Constants.ENDPOINTS.businessHours);
            expect(req.request.method).toBe("GET");
            req.flush(mockBusinessHours);
        });

        it("should return an empty array when no business hours are available", () => {
            const mockBusinessHours: BusinessHours[] = [];

            service.getBusinessHours().subscribe((businessHours) => {
                expect(businessHours).toEqual([]);
                expect(businessHours.length).toBe(0);
            });

            const req = httpMock.expectOne(Constants.ENDPOINTS.businessHours);
            expect(req.request.method).toBe("GET");
            req.flush(mockBusinessHours);
        });

        it("should handle HTTP errors gracefully", () => {
            const errorMessage = "Server error";

            service.getBusinessHours().subscribe(
                () => fail("should have failed with 500 error"),
                (error) => {
                    expect(error.status).toBe(500);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const req = httpMock.expectOne(Constants.ENDPOINTS.businessHours);
            expect(req.request.method).toBe("GET");
            req.flush(errorMessage, { status: 500, statusText: "Server Error" });
        });

        it("should handle network errors", () => {
            const errorMessage = "Network error";

            service.getBusinessHours().subscribe(
                () => fail("should have failed with network error"),
                (error) => {
                    expect(error.error.type).toBe("error");
                },
            );

            const req = httpMock.expectOne(Constants.ENDPOINTS.businessHours);
            expect(req.request.method).toBe("GET");
            req.error(new ErrorEvent("error", { message: errorMessage }));
        });

        it("should return business hours for all weekdays", () => {
            const mockBusinessHours: BusinessHours[] = [
                { day: "Monday", start_time: "08:00", end_time: "18:00" },
                { day: "Tuesday", start_time: "08:00", end_time: "18:00" },
                { day: "Wednesday", start_time: "08:00", end_time: "18:00" },
                { day: "Thursday", start_time: "08:00", end_time: "18:00" },
                { day: "Friday", start_time: "08:00", end_time: "16:00" },
                { day: "Saturday", start_time: "10:00", end_time: "14:00" },
                { day: "Sunday", start_time: "closed", end_time: "closed" },
            ];

            service.getBusinessHours().subscribe((businessHours) => {
                expect(businessHours.length).toBe(7);
                expect(businessHours[4].day).toBe("Friday");
                expect(businessHours[4].end_time).toBe("16:00");
                expect(businessHours[6].day).toBe("Sunday");
                expect(businessHours[6].start_time).toBe("closed");
            });

            const req = httpMock.expectOne(Constants.ENDPOINTS.businessHours);
            req.flush(mockBusinessHours);
        });

        it("should use the correct endpoint URL", () => {
            const mockBusinessHours: BusinessHours[] = [];

            service.getBusinessHours().subscribe();

            const req = httpMock.expectOne(Constants.ENDPOINTS.businessHours);
            expect(req.request.url).toBe(Constants.ENDPOINTS.businessHours);
            req.flush(mockBusinessHours);
        });

        it("should handle different time formats", () => {
            const mockBusinessHours: BusinessHours[] = [
                {
                    day: "Monday",
                    start_time: "09:30:00",
                    end_time: "17:45:30",
                },
            ];

            service.getBusinessHours().subscribe((businessHours) => {
                expect(businessHours[0].start_time).toBe("09:30:00");
                expect(businessHours[0].end_time).toBe("17:45:30");
            });

            const req = httpMock.expectOne(Constants.ENDPOINTS.businessHours);
            req.flush(mockBusinessHours);
        });

        it("should return observable that can be subscribed multiple times", () => {
            const mockBusinessHours: BusinessHours[] = [
                { day: "Monday", start_time: "09:00", end_time: "17:00" },
            ];

            const observable = service.getBusinessHours();

            // First subscription
            observable.subscribe((businessHours) => {
                expect(businessHours).toEqual(mockBusinessHours);
            });

            const req1 = httpMock.expectOne(Constants.ENDPOINTS.businessHours);
            req1.flush(mockBusinessHours);

            // Second subscription
            observable.subscribe((businessHours) => {
                expect(businessHours).toEqual(mockBusinessHours);
            });

            const req2 = httpMock.expectOne(Constants.ENDPOINTS.businessHours);
            req2.flush(mockBusinessHours);
        });
    });
});
