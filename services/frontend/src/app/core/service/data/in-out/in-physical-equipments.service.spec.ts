import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Constants } from "src/constants";
import { InPhysicalEquipmentRest } from "../../../interfaces/input.interface";
import { InPhysicalEquipmentsService } from "./in-physical-equipments.service";

describe("InPhysicalEquipmentsService", () => {
    let service: InPhysicalEquipmentsService;
    let httpMock: HttpTestingController;

    const mockPhysicalEquipment: InPhysicalEquipmentRest = {
        id: 1,
        name: "Server-01",
        digitalServiceUid: "ds-123",
        digitalServiceVersionUid: "dsv-456",
        datacenterName: "DC-Paris",
        quantity: 10,
        location: "EU-West-1",
        type: "Dedicated Server",
        model: "Dell PowerEdge R740",
        manufacturer: "Dell",
        datePurchase: "2020-01-01",
        dateWithdrawal: "2025-01-01",
        cpuType: "Intel Xeon",
        cpuCoreNumber: 16,
        sizeMemoryGb: 128,
        sizeDiskGb: 2000,
        electricityConsumption: 1200,
        durationHour: 8760,
        numberOfUsers: 500,
    };

    const mockPhysicalEquipmentList: InPhysicalEquipmentRest[] = [
        mockPhysicalEquipment,
        {
            ...mockPhysicalEquipment,
            id: 2,
            name: "Server-02",
            quantity: 8,
        },
        {
            ...mockPhysicalEquipment,
            id: 3,
            name: "Server-03",
            datacenterName: "DC-London",
        },
    ];

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [InPhysicalEquipmentsService],
        });
        service = TestBed.inject(InPhysicalEquipmentsService);
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
                expect(equipments[0].quantity).toBe(10);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("GET");
            req.flush(mockPhysicalEquipmentList);
        });

        it("should return empty array when no physical equipments exist", () => {
            const digitalServiceUid = "ds-empty-uid";
            const emptyList: InPhysicalEquipmentRest[] = [];

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments).toEqual([]);
                expect(equipments.length).toBe(0);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
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

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, {
                status: 500,
                statusText: "Internal Server Error",
            });
        });

        it("should handle HTTP 404 errors", () => {
            const digitalServiceUid = "ds-notfound-uid";

            service.get(digitalServiceUid).subscribe(
                () => fail("should have failed with 404 error"),
                (error) => {
                    expect(error.status).toBe(404);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush("Not Found", { status: 404, statusText: "Not Found" });
        });

        it("should handle network errors", () => {
            const digitalServiceUid = "ds-network-uid";

            service.get(digitalServiceUid).subscribe(
                () => fail("should have failed with network error"),
                (error) => {
                    expect(error.error.type).toBe("error");
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.error(new ErrorEvent("error", { message: "Network error" }));
        });

        it("should retrieve equipments with different types", () => {
            const digitalServiceUid = "ds-types-uid";
            const equipmentList: InPhysicalEquipmentRest[] = [
                { ...mockPhysicalEquipment, type: "Dedicated Server" },
                { ...mockPhysicalEquipment, type: "Shared Server" },
            ];

            service.get(digitalServiceUid).subscribe((equipments) => {
                expect(equipments.length).toBe(2);
                expect(equipments[0].type).toBe("Dedicated Server");
                expect(equipments[1].type).toBe("Shared Server");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(equipmentList);
        });

        it("should handle different digital service UIDs", () => {
            const digitalServiceUid1 = "ds-uid-1";
            const digitalServiceUid2 = "ds-uid-2";

            service.get(digitalServiceUid1).subscribe((equipments) => {
                expect(equipments.length).toBe(1);
            });

            const req1 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid1}/inputs/physical-equipments`,
            );
            req1.flush([mockPhysicalEquipment]);

            service.get(digitalServiceUid2).subscribe((equipments) => {
                expect(equipments.length).toBe(0);
            });

            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid2}/inputs/physical-equipments`,
            );
            req2.flush([]);
        });
    });

    describe("update", () => {
        it("should update a physical equipment", () => {
            const updatedEquipment = {
                ...mockPhysicalEquipment,
                name: "Server-Updated",
                quantity: 15,
                cpuCoreNumber: 32,
            };

            service.update(updatedEquipment).subscribe((equipment) => {
                expect(equipment).toEqual(updatedEquipment);
                expect(equipment.name).toBe("Server-Updated");
                expect(equipment.quantity).toBe(15);
                expect(equipment.cpuCoreNumber).toBe(32);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockPhysicalEquipment.digitalServiceVersionUid}/inputs/physical-equipments/${mockPhysicalEquipment.id}`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("PUT");
            expect(req.request.headers.get("content-type")).toBe("application/json");
            expect(req.request.body).toEqual(updatedEquipment);
            req.flush(updatedEquipment);
        });

        it("should send correct headers with update request", () => {
            service.update(mockPhysicalEquipment).subscribe();

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockPhysicalEquipment.digitalServiceVersionUid}/inputs/physical-equipments/${mockPhysicalEquipment.id}`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.headers.has("content-type")).toBe(true);
            expect(req.request.headers.get("content-type")).toBe("application/json");
            req.flush(mockPhysicalEquipment);
        });

        it("should handle HTTP 400 errors on update", () => {
            const errorMessage = "Validation error";

            service.update(mockPhysicalEquipment).subscribe(
                () => fail("should have failed with 400 error"),
                (error) => {
                    expect(error.status).toBe(400);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockPhysicalEquipment.digitalServiceVersionUid}/inputs/physical-equipments/${mockPhysicalEquipment.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, { status: 400, statusText: "Bad Request" });
        });

        it("should handle network errors on update", () => {
            service.update(mockPhysicalEquipment).subscribe(
                () => fail("should have failed with network error"),
                (error) => {
                    expect(error.error.type).toBe("error");
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockPhysicalEquipment.digitalServiceVersionUid}/inputs/physical-equipments/${mockPhysicalEquipment.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.error(new ErrorEvent("error", { message: "Network error" }));
        });

        it("should update equipment with modified properties", () => {
            const modifiedEquipment = {
                ...mockPhysicalEquipment,
                sizeMemoryGb: 256,
                sizeDiskGb: 4000,
                electricityConsumption: 1500,
            };

            service.update(modifiedEquipment).subscribe((equipment) => {
                expect(equipment.sizeMemoryGb).toBe(256);
                expect(equipment.sizeDiskGb).toBe(4000);
                expect(equipment.electricityConsumption).toBe(1500);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${modifiedEquipment.digitalServiceVersionUid}/inputs/physical-equipments/${modifiedEquipment.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(modifiedEquipment);
        });

        it("should handle unauthorized errors on update", () => {
            service.update(mockPhysicalEquipment).subscribe(
                () => fail("should have failed with 401 error"),
                (error) => {
                    expect(error.status).toBe(401);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockPhysicalEquipment.digitalServiceVersionUid}/inputs/physical-equipments/${mockPhysicalEquipment.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush("Unauthorized", { status: 401, statusText: "Unauthorized" });
        });
    });

    describe("create", () => {
        it("should create a new physical equipment", () => {
            const newEquipment: InPhysicalEquipmentRest = {
                ...mockPhysicalEquipment,
                id: undefined,
                name: "Server-New",
            };
            const createdEquipment = { ...newEquipment, id: 999 };

            service.create(newEquipment).subscribe((equipment) => {
                expect(equipment).toEqual(createdEquipment);
                expect(equipment.id).toBe(999);
                expect(equipment.name).toBe("Server-New");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${newEquipment.digitalServiceVersionUid}/inputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("POST");
            expect(req.request.headers.get("content-type")).toBe("application/json");
            expect(req.request.body).toEqual(newEquipment);
            req.flush(createdEquipment);
        });

        it("should send correct headers with create request", () => {
            service.create(mockPhysicalEquipment).subscribe();

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockPhysicalEquipment.digitalServiceVersionUid}/inputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.headers.has("content-type")).toBe(true);
            expect(req.request.headers.get("content-type")).toBe("application/json");
            req.flush(mockPhysicalEquipment);
        });

        it("should handle HTTP 409 conflict errors on create", () => {
            const errorMessage = "Duplicate name";

            service.create(mockPhysicalEquipment).subscribe(
                () => fail("should have failed with 409 error"),
                (error) => {
                    expect(error.status).toBe(409);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockPhysicalEquipment.digitalServiceVersionUid}/inputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, { status: 409, statusText: "Conflict" });
        });

        it("should handle network errors on create", () => {
            service.create(mockPhysicalEquipment).subscribe(
                () => fail("should have failed with network error"),
                (error) => {
                    expect(error.error.type).toBe("error");
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockPhysicalEquipment.digitalServiceVersionUid}/inputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.error(new ErrorEvent("error", { message: "Network error" }));
        });

        it("should create equipment with all properties", () => {
            const completeEquipment: InPhysicalEquipmentRest = {
                id: undefined,
                name: "Complete-Server",
                inventoryId: 123,
                digitalServiceUid: "ds-123",
                digitalServiceVersionUid: "dsv-456",
                datacenterName: "DC-Complete",
                quantity: 20,
                location: "US-East-1",
                type: "Dedicated Server",
                model: "HP ProLiant DL380",
                manufacturer: "HP",
                datePurchase: "2021-01-01",
                dateWithdrawal: "2026-01-01",
                cpuType: "AMD EPYC",
                cpuCoreNumber: 64,
                sizeMemoryGb: 512,
                sizeDiskGb: 8000,
                source: "Manual",
                description: "High performance server",
                electricityConsumption: 2000,
                durationHour: 8760,
                creationDate: "2024-01-01",
                lastUpdatedDate: "2024-03-03",
                numberOfUsers: 1000,
            };

            service.create(completeEquipment).subscribe((equipment) => {
                expect(equipment.name).toBe("Complete-Server");
                expect(equipment.quantity).toBe(20);
                expect(equipment.cpuCoreNumber).toBe(64);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${completeEquipment.digitalServiceVersionUid}/inputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.body).toEqual(completeEquipment);
            req.flush({ ...completeEquipment, id: 100 });
        });

        it("should handle validation errors on create", () => {
            const invalidEquipment = { ...mockPhysicalEquipment, name: "" };

            service.create(invalidEquipment).subscribe(
                () => fail("should have failed with validation error"),
                (error) => {
                    expect(error.status).toBe(400);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${invalidEquipment.digitalServiceVersionUid}/inputs/physical-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush("Validation error", { status: 400, statusText: "Bad Request" });
        });
    });

    describe("delete", () => {
        it("should delete a physical equipment", () => {
            const mockResponse = mockPhysicalEquipment;

            service.delete(mockPhysicalEquipment).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockPhysicalEquipment.digitalServiceVersionUid}/inputs/physical-equipments/${mockPhysicalEquipment.id}`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("DELETE");
            req.flush(mockResponse);
        });

        it("should handle HTTP 404 errors on delete", () => {
            const errorMessage = "Equipment not found";

            service.delete(mockPhysicalEquipment).subscribe(
                () => fail("should have failed with 404 error"),
                (error) => {
                    expect(error.status).toBe(404);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockPhysicalEquipment.digitalServiceVersionUid}/inputs/physical-equipments/${mockPhysicalEquipment.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, { status: 404, statusText: "Not Found" });
        });

        it("should handle network errors on delete", () => {
            service.delete(mockPhysicalEquipment).subscribe(
                () => fail("should have failed with network error"),
                (error) => {
                    expect(error.error.type).toBe("error");
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockPhysicalEquipment.digitalServiceVersionUid}/inputs/physical-equipments/${mockPhysicalEquipment.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.error(new ErrorEvent("error", { message: "Network error" }));
        });

        it("should delete equipment with different IDs", () => {
            const equipment1 = { ...mockPhysicalEquipment, id: 1 };
            const equipment2 = { ...mockPhysicalEquipment, id: 2 };

            service.delete(equipment1).subscribe();
            const req1 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${equipment1.digitalServiceVersionUid}/inputs/physical-equipments/${equipment1.id}`,
            );
            req1.flush(equipment1);

            service.delete(equipment2).subscribe();
            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${equipment2.digitalServiceVersionUid}/inputs/physical-equipments/${equipment2.id}`,
            );
            req2.flush(equipment2);
        });

        it("should handle forbidden errors on delete", () => {
            service.delete(mockPhysicalEquipment).subscribe(
                () => fail("should have failed with 403 error"),
                (error) => {
                    expect(error.status).toBe(403);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockPhysicalEquipment.digitalServiceVersionUid}/inputs/physical-equipments/${mockPhysicalEquipment.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush("Forbidden", { status: 403, statusText: "Forbidden" });
        });

        it("should handle deletion with undefined id gracefully", () => {
            const equipmentWithoutId = { ...mockPhysicalEquipment, id: undefined };

            service.delete(equipmentWithoutId).subscribe();

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${equipmentWithoutId.digitalServiceVersionUid}/inputs/physical-equipments/${equipmentWithoutId.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(equipmentWithoutId);
        });
    });

    describe("URL construction", () => {
        it("should construct correct URL for get request", () => {
            const digitalServiceUid = "test-ds-uid-123";

            service.get(digitalServiceUid).subscribe();

            const req = httpMock.expectOne(
                (request) =>
                    request.url.includes(Constants.ENDPOINTS.digitalServicesVersions) &&
                    request.url.includes(digitalServiceUid) &&
                    request.url.includes("inputs/physical-equipments"),
            );
            expect(req.request.url).toContain(digitalServiceUid);
            req.flush([]);
        });

        it("should use correct endpoint from Constants", () => {
            const digitalServiceUid = "endpoint-test-uid";

            service.get(digitalServiceUid).subscribe();

            const req = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/physical-equipments`,
            );
            expect(req.request.url).toContain(
                Constants.ENDPOINTS.digitalServicesVersions,
            );
            req.flush([]);
        });
    });

    describe("Multiple concurrent operations", () => {
        it("should handle multiple concurrent GET requests", () => {
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
                `${Constants.ENDPOINTS.digitalServicesVersions}/${uid1}/inputs/physical-equipments`,
            );
            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${uid2}/inputs/physical-equipments`,
            );
            const req3 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${uid3}/inputs/physical-equipments`,
            );

            req1.flush([mockPhysicalEquipment]);
            req2.flush([
                mockPhysicalEquipment,
                { ...mockPhysicalEquipment, id: 2, name: "Server-02" },
            ]);
            req3.flush([]);
        });

        it("should handle mixed CRUD operations", () => {
            const digitalServiceUid = "ds-mixed";
            const newEquipment = {
                ...mockPhysicalEquipment,
                id: undefined,
                name: "New Server",
            };

            service.get(digitalServiceUid).subscribe();
            service.create(newEquipment).subscribe();
            service.update(mockPhysicalEquipment).subscribe();
            service.delete(mockPhysicalEquipment).subscribe();

            const reqGet = httpMock.expectOne((req) => req.method === "GET");
            const reqCreate = httpMock.expectOne((req) => req.method === "POST");
            const reqUpdate = httpMock.expectOne((req) => req.method === "PUT");
            const reqDelete = httpMock.expectOne((req) => req.method === "DELETE");

            reqGet.flush([mockPhysicalEquipment]);
            reqCreate.flush({ ...newEquipment, id: 100 });
            reqUpdate.flush(mockPhysicalEquipment);
            reqDelete.flush(mockPhysicalEquipment);
        });
    });

    describe("Observable behavior", () => {
        it("should return an Observable for all methods", () => {
            const digitalServiceUid = "obs-test";
            const equipment = mockPhysicalEquipment;

            expect(typeof service.get(digitalServiceUid).subscribe).toBe("function");
            expect(typeof service.update(equipment).subscribe).toBe("function");
            expect(typeof service.create(equipment).subscribe).toBe("function");
            expect(typeof service.delete(equipment).subscribe).toBe("function");

            httpMock.match(() => true).forEach((req) => req.flush({}));
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
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/physical-equipments`,
            );
            req.flush([mockPhysicalEquipment]);
        });
    });
});
