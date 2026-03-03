import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { OutPhysicalEquipmentRest } from "src/app/core/interfaces/output.interface";
import { Constants } from "src/constants";
import { OutPhysicalEquipmentsService } from "./out-physical-equipments.service";

describe("OutPhysicalEquipmentsService", () => {
    let service: OutPhysicalEquipmentsService;
    let httpMock: HttpTestingController;

    const mockPhysicalEquipment: OutPhysicalEquipmentRest = {
        name: "Server-01",
        criterion: "climate-change",
        lifecycleStep: "use",
        statusIndicator: "OK",
        datacenterName: "DC-Paris",
        location: "EU-West-1",
        equipmentType: "Server",
        reference: "Dell PowerEdge R740",
        hostingEfficiency: "0.85",
        engineName: "TestEngine",
        engineVersion: "1.0.0",
        referentialVersion: "1.0",
        unit: "kgCO2eq",
        countValue: 100,
        unitImpact: 25.5,
        peopleEqImpact: 5.2,
        electricityConsumption: 1200,
        quantity: 10,
        lifespan: 5,
        numberOfUsers: 500,
    };

    const mockPhysicalEquipmentList: OutPhysicalEquipmentRest[] = [
        mockPhysicalEquipment,
        {
            ...mockPhysicalEquipment,
            name: "Server-02",
            quantity: 8,
            unitImpact: 22.3,
        },
        {
            ...mockPhysicalEquipment,
            name: "Server-03",
            datacenterName: "DC-London",
            location: "EU-West-2",
        },
    ];

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [OutPhysicalEquipmentsService],
        });
        service = TestBed.inject(OutPhysicalEquipmentsService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    describe("get", () => {
        it("should retrieve physical equipments for a digital service", () => {
            const digitalServiceUid = "ds-123-uid";

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments).toEqual(mockPhysicalEquipmentList);
                expect(equipments.length).toBe(3);
                expect(equipments[0].name).toBe("Server-01");
                expect(equipments[0].unitImpact).toBe(25.5);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("GET");
            req.flush(mockPhysicalEquipmentList);
        });

        it("should return empty array when no physical equipments exist", () => {
            const digitalServiceUid = "ds-empty-uid";
            const emptyList: OutPhysicalEquipmentRest[] = [];

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments).toEqual([]);
                expect(equipments.length).toBe(0);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("GET");
            req.flush(emptyList);
        });

        it("should handle HTTP 500 errors gracefully", () => {
            const digitalServiceUid = "ds-error-uid";
            const errorMessage = "Internal Server Error";

            service.get(digitalServiceUid).subscribe(
                () => fail("should have failed with 500 error"),
                (error) => {
                    expect(error.status).toBe(500);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, { status: 500, statusText: "Internal Server Error" });
        });

        it("should handle HTTP 404 errors", () => {
            const digitalServiceUid = "ds-notfound-uid";

            service.get(digitalServiceUid).subscribe(
                () => fail("should have failed with 404 error"),
                (error) => {
                    expect(error.status).toBe(404);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush("Not Found", { status: 404, statusText: "Not Found" });
        });

        it("should handle HTTP 401 unauthorized errors", () => {
            const digitalServiceUid = "ds-unauthorized-uid";

            service.get(digitalServiceUid).subscribe(
                () => fail("should have failed with 401 error"),
                (error) => {
                    expect(error.status).toBe(401);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush("Unauthorized", { status: 401, statusText: "Unauthorized" });
        });

        it("should handle network errors", () => {
            const digitalServiceUid = "ds-network-uid";

            service.get(digitalServiceUid).subscribe(
                () => fail("should have failed with network error"),
                (error) => {
                    expect(error.error.type).toBe("error");
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.error(new ErrorEvent("error", { message: "Network error" }));
        });

        it("should retrieve physical equipments with filters", () => {
            const digitalServiceUid = "ds-filtered-uid";
            const equipmentWithFilters: OutPhysicalEquipmentRest[] = [
                {
                    ...mockPhysicalEquipment,
                    commonFilters: ["filter1", "filter2"],
                    filters: ["datacenter:Paris", "type:server"],
                },
            ];

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments[0].commonFilters).toEqual(["filter1", "filter2"]);
                expect(equipments[0].filters).toEqual([
                    "datacenter:Paris",
                    "type:server",
                ]);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(equipmentWithFilters);
        });

        it("should retrieve physical equipments with errors", () => {
            const digitalServiceUid = "ds-errors-uid";
            const equipmentWithErrors: OutPhysicalEquipmentRest[] = [
                {
                    ...mockPhysicalEquipment,
                    errors: ["Calculation error", "Missing reference data"],
                },
            ];

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments[0].errors).toEqual([
                    "Calculation error",
                    "Missing reference data",
                ]);
                expect(equipments[0].errors?.length).toBe(2);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(equipmentWithErrors);
        });

        it("should handle different digital service UIDs", () => {
            const digitalServiceUid1 = "ds-uid-1";
            const digitalServiceUid2 = "ds-uid-2";

            service.get(digitalServiceUid1).subscribe((equipments) => {
                expect(equipments.length).toBe(1);
            });

            const req1 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid1}/outputs/physical-equipments`,
            );
            req1.flush([mockPhysicalEquipment]);

            service.get(digitalServiceUid2).subscribe((equipments) => {
                expect(equipments.length).toBe(0);
            });

            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid2}/outputs/physical-equipments`,
            );
            req2.flush([]);
        });

        it("should retrieve physical equipments with different equipment types", () => {
            const digitalServiceUid = "ds-types-uid";
            const equipmentList: OutPhysicalEquipmentRest[] = [
                { ...mockPhysicalEquipment, equipmentType: "Server" },
                { ...mockPhysicalEquipment, equipmentType: "Storage" },
                { ...mockPhysicalEquipment, equipmentType: "Network" },
            ];

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments.length).toBe(3);
                expect(equipments[0].equipmentType).toBe("Server");
                expect(equipments[1].equipmentType).toBe("Storage");
                expect(equipments[2].equipmentType).toBe("Network");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(equipmentList);
        });

        it("should retrieve physical equipments with different lifecycle steps", () => {
            const digitalServiceUid = "ds-lifecycle-uid";
            const equipmentList: OutPhysicalEquipmentRest[] = [
                { ...mockPhysicalEquipment, lifecycleStep: "manufacturing" },
                { ...mockPhysicalEquipment, lifecycleStep: "distribution" },
                { ...mockPhysicalEquipment, lifecycleStep: "use" },
                { ...mockPhysicalEquipment, lifecycleStep: "end-of-life" },
            ];

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments.length).toBe(4);
                expect(equipments[0].lifecycleStep).toBe("manufacturing");
                expect(equipments[1].lifecycleStep).toBe("distribution");
                expect(equipments[2].lifecycleStep).toBe("use");
                expect(equipments[3].lifecycleStep).toBe("end-of-life");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(equipmentList);
        });

        it("should retrieve physical equipments with different criteria", () => {
            const digitalServiceUid = "ds-criteria-uid";
            const equipmentList: OutPhysicalEquipmentRest[] = [
                { ...mockPhysicalEquipment, criterion: "climate-change" },
                { ...mockPhysicalEquipment, criterion: "resource-use" },
                { ...mockPhysicalEquipment, criterion: "acidification" },
            ];

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments.length).toBe(3);
                expect(equipments[0].criterion).toBe("climate-change");
                expect(equipments[1].criterion).toBe("resource-use");
                expect(equipments[2].criterion).toBe("acidification");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(equipmentList);
        });

        it("should retrieve physical equipments with complete data including all optional fields", () => {
            const digitalServiceUid = "ds-complete-uid";
            const completeEquipment: OutPhysicalEquipmentRest = {
                name: "Complete-Server",
                criterion: "climate-change",
                lifecycleStep: "use",
                statusIndicator: "OK",
                datacenterName: "DC-Complete",
                location: "US-East-1",
                equipmentType: "Server",
                reference: "HP ProLiant DL380",
                hostingEfficiency: "0.90",
                engineName: "CompleteEngine",
                engineVersion: "2.0.0",
                referentialVersion: "2.0",
                unit: "kgCO2eq",
                countValue: 250,
                unitImpact: 30.5,
                peopleEqImpact: 6.8,
                electricityConsumption: 1500,
                quantity: 15,
                lifespan: 6,
                numberOfUsers: 1000,
                commonFilters: ["common1", "common2"],
                filters: ["location:US", "datacenter:Complete"],
                errors: [],
            };

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments[0]).toEqual(completeEquipment);
                expect(equipments[0].name).toBe("Complete-Server");
                expect(equipments[0].quantity).toBe(15);
                expect(equipments[0].lifespan).toBe(6);
                expect(equipments[0].numberOfUsers).toBe(1000);
                expect(equipments[0].commonFilters).toEqual(["common1", "common2"]);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush([completeEquipment]);
        });

        it("should retrieve physical equipments with different status indicators", () => {
            const digitalServiceUid = "ds-status-uid";
            const equipmentList: OutPhysicalEquipmentRest[] = [
                { ...mockPhysicalEquipment, statusIndicator: "OK" },
                { ...mockPhysicalEquipment, statusIndicator: "WARNING" },
                { ...mockPhysicalEquipment, statusIndicator: "ERROR" },
            ];

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments.length).toBe(3);
                expect(equipments[0].statusIndicator).toBe("OK");
                expect(equipments[1].statusIndicator).toBe("WARNING");
                expect(equipments[2].statusIndicator).toBe("ERROR");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(equipmentList);
        });

        it("should retrieve physical equipments with various hosting efficiencies", () => {
            const digitalServiceUid = "ds-efficiency-uid";
            const equipmentList: OutPhysicalEquipmentRest[] = [
                { ...mockPhysicalEquipment, hostingEfficiency: "0.75" },
                { ...mockPhysicalEquipment, hostingEfficiency: "0.85" },
                { ...mockPhysicalEquipment, hostingEfficiency: "0.95" },
            ];

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments.length).toBe(3);
                expect(equipments[0].hostingEfficiency).toBe("0.75");
                expect(equipments[1].hostingEfficiency).toBe("0.85");
                expect(equipments[2].hostingEfficiency).toBe("0.95");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(equipmentList);
        });

        it("should retrieve physical equipments with different lifespans", () => {
            const digitalServiceUid = "ds-lifespan-uid";
            const equipmentList: OutPhysicalEquipmentRest[] = [
                { ...mockPhysicalEquipment, lifespan: 3 },
                { ...mockPhysicalEquipment, lifespan: 5 },
                { ...mockPhysicalEquipment, lifespan: 7 },
            ];

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments[0].lifespan).toBe(3);
                expect(equipments[1].lifespan).toBe(5);
                expect(equipments[2].lifespan).toBe(7);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(equipmentList);
        });

        it("should retrieve physical equipments with high electricity consumption", () => {
            const digitalServiceUid = "ds-consumption-uid";
            const equipmentList: OutPhysicalEquipmentRest[] = [
                { ...mockPhysicalEquipment, electricityConsumption: 5000 },
                { ...mockPhysicalEquipment, electricityConsumption: 10000 },
            ];

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments[0].electricityConsumption).toBe(5000);
                expect(equipments[1].electricityConsumption).toBe(10000);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(equipmentList);
        });
    });

    describe("URL construction", () => {
        it("should construct correct URL with digital service UID", () => {
            const digitalServiceUid = "test-ds-uid-123";

            service.get(digitalServiceUid).subscribe();

            const req = httpMock.expectOne(
                (request) =>
                    request.url.includes(Constants.ENDPOINTS.digitalServicesVersions) &&
                    request.url.includes(digitalServiceUid) &&
                    request.url.includes("outputs/physical-equipments"),
            );
            expect(req.request.url).toContain(digitalServiceUid);
            req.flush([]);
        });

        it("should use correct endpoint from Constants", () => {
            const digitalServiceUid = "endpoint-test-uid";

            service.get(digitalServiceUid).subscribe();

            const req = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`,
            );
            expect(req.request.url).toContain(
                Constants.ENDPOINTS.digitalServicesVersions,
            );
            req.flush([]);
        });
    });

    describe("Multiple concurrent requests", () => {
        it("should handle multiple concurrent requests for different digital services", () => {
            const uid1 = "ds-1";
            const uid2 = "ds-2";
            const uid3 = "ds-3";

            service.get(uid1).subscribe((equipments) => {
                expect(equipments.length).toBe(1);
            });
            service.get(uid2).subscribe((equipments) => {
                expect(equipments.length).toBe(2);
            });
            service.get(uid3).subscribe((equipments) => {
                expect(equipments.length).toBe(0);
            });

            const req1 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${uid1}/outputs/physical-equipments`,
            );
            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${uid2}/outputs/physical-equipments`,
            );
            const req3 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${uid3}/outputs/physical-equipments`,
            );

            req1.flush([mockPhysicalEquipment]);
            req2.flush([
                mockPhysicalEquipment,
                { ...mockPhysicalEquipment, name: "Server-02" },
            ]);
            req3.flush([]);
        });

        it("should handle sequential requests for the same digital service", () => {
            const digitalServiceUid = "sequential-ds";

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments.length).toBe(1);
            });

            const req1 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`,
            );
            req1.flush([mockPhysicalEquipment]);

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments.length).toBe(2);
            });

            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`,
            );
            req2.flush([
                mockPhysicalEquipment,
                { ...mockPhysicalEquipment, name: "Server-02" },
            ]);
        });
    });

    describe("Observable behavior", () => {
        it("should return an Observable", () => {
            const digitalServiceUid = "observable-test-uid";
            const result = service.get(digitalServiceUid);

            expect(result).toBeDefined();
            expect(typeof result.subscribe).toBe("function");

            result.subscribe();
            const req = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`,
            );
            req.flush([]);
        });

        it("should complete the observable after receiving response", (done) => {
            const digitalServiceUid = "complete-test-uid";

            service.get(digitalServiceUid).subscribe({
                next: () => {},
                complete: () => {
                    expect(true).toBe(true);
                    done();
                },
            });

            const req = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/physical-equipments`,
            );
            req.flush([mockPhysicalEquipment]);
        });
    });
});
