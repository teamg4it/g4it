import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Constants } from "src/constants";
import { InVirtualEquipmentRest } from "../../../interfaces/input.interface";
import { InVirtualEquipmentsService } from "./in-virtual-equipments.service";

describe("InVirtualEquipmentsService", () => {
    let service: InVirtualEquipmentsService;
    let httpMock: HttpTestingController;

    const mockVirtualEquipment: InVirtualEquipmentRest = {
        id: 1,
        name: "VM-Test-1",
        digitalServiceUid: "ds-123",
        digitalServiceVersionUid: "dsv-456",
        datacenterName: "DC-Paris",
        physicalEquipmentName: "Server-01",
        quantity: 5,
        infrastructureType: "CLOUD",
        instanceType: "t2.medium",
        type: "compute",
        provider: "AWS",
        location: "EU-West-1",
        durationHour: 8760,
        workload: 80,
        electricityConsumption: 500,
        vcpuCoreNumber: 4,
        sizeMemoryMb: 8192,
        sizeDiskGb: 100,
        allocationFactor: 0.75,
    };

    const mockVirtualEquipmentList: InVirtualEquipmentRest[] = [
        mockVirtualEquipment,
        {
            ...mockVirtualEquipment,
            id: 2,
            name: "VM-Test-2",
            quantity: 3,
        },
        {
            ...mockVirtualEquipment,
            id: 3,
            name: "VM-Test-3",
            provider: "Azure",
        },
    ];

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [InVirtualEquipmentsService],
        });
        service = TestBed.inject(InVirtualEquipmentsService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    describe("getByDigitalService", () => {
        it("should retrieve virtual equipments for a digital service", () => {
            const digitalServiceUid = "ds-123-uid";

            service.getByDigitalService(digitalServiceUid).subscribe((equipments) => {
                expect(equipments).toEqual(mockVirtualEquipmentList);
                expect(equipments.length).toBe(3);
                expect(equipments[0].name).toBe("VM-Test-1");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("GET");
            req.flush(mockVirtualEquipmentList);
        });

        it("should return empty array when no virtual equipments exist", () => {
            const digitalServiceUid = "ds-empty-uid";
            const emptyList: InVirtualEquipmentRest[] = [];

            service.getByDigitalService(digitalServiceUid).subscribe((equipments) => {
                expect(equipments).toEqual([]);
                expect(equipments.length).toBe(0);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(emptyList);
        });

        it("should handle HTTP errors gracefully", () => {
            const digitalServiceUid = "ds-error-uid";
            const errorMessage = "Server error";

            service.getByDigitalService(digitalServiceUid).subscribe(
                () => fail("should have failed with 500 error"),
                (error) => {
                    expect(error.status).toBe(500);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, { status: 500, statusText: "Server Error" });
        });

        it("should handle network errors", () => {
            const digitalServiceUid = "ds-network-uid";

            service.getByDigitalService(digitalServiceUid).subscribe(
                () => fail("should have failed with network error"),
                (error) => {
                    expect(error.error.type).toBe("error");
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.error(new ErrorEvent("error", { message: "Network error" }));
        });
    });

    describe("getByInventory", () => {
        it("should retrieve virtual equipments for an inventory", () => {
            const inventoryId = 123;

            service.getByInventory(inventoryId).subscribe((equipments) => {
                expect(equipments).toEqual(mockVirtualEquipmentList);
                expect(equipments.length).toBe(3);
                expect(equipments[0].name).toBe("VM-Test-1");
            });

            const expectedUrl = `${Constants.ENDPOINTS.inventories}/${inventoryId}/inputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("GET");
            req.flush(mockVirtualEquipmentList);
        });

        it("should return empty array when no virtual equipments exist", () => {
            const inventoryId = 456;
            const emptyList: InVirtualEquipmentRest[] = [];

            service.getByInventory(inventoryId).subscribe((equipments) => {
                expect(equipments).toEqual([]);
                expect(equipments.length).toBe(0);
            });

            const expectedUrl = `${Constants.ENDPOINTS.inventories}/${inventoryId}/inputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(emptyList);
        });

        it("should handle HTTP errors gracefully", () => {
            const inventoryId = 789;
            const errorMessage = "Unauthorized";

            service.getByInventory(inventoryId).subscribe(
                () => fail("should have failed with 401 error"),
                (error) => {
                    expect(error.status).toBe(401);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.inventories}/${inventoryId}/inputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, { status: 401, statusText: "Unauthorized" });
        });

        it("should handle different inventory IDs", () => {
            const inventoryId1 = 100;
            const inventoryId2 = 200;

            service.getByInventory(inventoryId1).subscribe();
            const req1 = httpMock.expectOne(
                `${Constants.ENDPOINTS.inventories}/${inventoryId1}/inputs/virtual-equipments`,
            );
            req1.flush([mockVirtualEquipment]);

            service.getByInventory(inventoryId2).subscribe();
            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.inventories}/${inventoryId2}/inputs/virtual-equipments`,
            );
            req2.flush([]);
        });
    });

    describe("update", () => {
        it("should update a virtual equipment", () => {
            const updatedEquipment = {
                ...mockVirtualEquipment,
                name: "VM-Updated",
                quantity: 10,
            };

            service.update(updatedEquipment).subscribe((equipment) => {
                expect(equipment).toEqual(updatedEquipment);
                expect(equipment.name).toBe("VM-Updated");
                expect(equipment.quantity).toBe(10);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockVirtualEquipment.digitalServiceVersionUid}/inputs/virtual-equipments/${mockVirtualEquipment.id}`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("PUT");
            expect(req.request.headers.get("content-type")).toBe("application/json");
            expect(req.request.body).toEqual(updatedEquipment);
            req.flush(updatedEquipment);
        });

        it("should send correct headers with update request", () => {
            service.update(mockVirtualEquipment).subscribe();

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockVirtualEquipment.digitalServiceVersionUid}/inputs/virtual-equipments/${mockVirtualEquipment.id}`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.headers.has("content-type")).toBe(true);
            expect(req.request.headers.get("content-type")).toBe("application/json");
            req.flush(mockVirtualEquipment);
        });

        it("should handle HTTP errors on update", () => {
            const errorMessage = "Validation error";

            service.update(mockVirtualEquipment).subscribe(
                () => fail("should have failed with 400 error"),
                (error) => {
                    expect(error.status).toBe(400);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockVirtualEquipment.digitalServiceVersionUid}/inputs/virtual-equipments/${mockVirtualEquipment.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, { status: 400, statusText: "Bad Request" });
        });

        it("should update equipment with modified properties", () => {
            const modifiedEquipment = {
                ...mockVirtualEquipment,
                vcpuCoreNumber: 8,
                sizeMemoryMb: 16384,
                workload: 90,
            };

            service.update(modifiedEquipment).subscribe((equipment) => {
                expect(equipment.vcpuCoreNumber).toBe(8);
                expect(equipment.sizeMemoryMb).toBe(16384);
                expect(equipment.workload).toBe(90);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${modifiedEquipment.digitalServiceVersionUid}/inputs/virtual-equipments/${modifiedEquipment.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(modifiedEquipment);
        });
    });

    describe("updateAllVms", () => {
        it("should update all VMs for a physical equipment", () => {
            const digitalServiceVersionId = "dsv-789";
            const physicalEquipmentId = 42;
            const vms = [mockVirtualEquipment, { ...mockVirtualEquipment, id: 2 }];
            const mockResponse = mockVirtualEquipment;

            service
                .updateAllVms(vms, digitalServiceVersionId, physicalEquipmentId)
                .subscribe((response) => {
                    expect(response).toEqual(mockResponse);
                });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceVersionId}/inputs/virtual-equipments`;
            const req = httpMock.expectOne(
                (request) =>
                    request.url === expectedUrl &&
                    request.params.get("physicalEqpId") ===
                        physicalEquipmentId.toString(),
            );
            expect(req.request.method).toBe("PUT");
            expect(req.request.headers.get("content-type")).toBe("application/json");
            expect(req.request.body).toEqual(vms);
            expect(req.request.params.get("physicalEqpId")).toBe(
                physicalEquipmentId.toString(),
            );
            req.flush(mockResponse);
        });

        it("should include physical equipment ID as query parameter", () => {
            const digitalServiceVersionId = "dsv-456";
            const physicalEquipmentId = 123;
            const vms = [mockVirtualEquipment];

            service
                .updateAllVms(vms, digitalServiceVersionId, physicalEquipmentId)
                .subscribe();

            const req = httpMock.expectOne(
                (request) =>
                    request.params.get("physicalEqpId") ===
                    physicalEquipmentId.toString(),
            );
            expect(req.request.params.get("physicalEqpId")).toBe("123");
            req.flush(mockVirtualEquipment);
        });

        it("should update empty array of VMs", () => {
            const digitalServiceVersionId = "dsv-empty";
            const physicalEquipmentId = 99;
            const vms: InVirtualEquipmentRest[] = [];

            service
                .updateAllVms(vms, digitalServiceVersionId, physicalEquipmentId)
                .subscribe();

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceVersionId}/inputs/virtual-equipments`;
            const req = httpMock.expectOne((request) => request.url === expectedUrl);
            expect(req.request.body).toEqual([]);
            req.flush(mockVirtualEquipment);
        });

        it("should handle HTTP errors on updateAllVms", () => {
            const digitalServiceVersionId = "dsv-error";
            const physicalEquipmentId = 404;
            const vms = [mockVirtualEquipment];
            const errorMessage = "Not found";

            service
                .updateAllVms(vms, digitalServiceVersionId, physicalEquipmentId)
                .subscribe(
                    () => fail("should have failed with 404 error"),
                    (error) => {
                        expect(error.status).toBe(404);
                    },
                );

            const req = httpMock.expectOne(
                (request) => request.params.get("physicalEqpId") === "404",
            );
            req.flush(errorMessage, { status: 404, statusText: "Not Found" });
        });

        it("should update multiple VMs in one request", () => {
            const digitalServiceVersionId = "dsv-multi";
            const physicalEquipmentId = 55;
            const vms = [
                mockVirtualEquipment,
                { ...mockVirtualEquipment, id: 2, name: "VM-2" },
                { ...mockVirtualEquipment, id: 3, name: "VM-3" },
            ];

            service
                .updateAllVms(vms, digitalServiceVersionId, physicalEquipmentId)
                .subscribe();

            const req = httpMock.expectOne((request) =>
                request.url.includes(digitalServiceVersionId),
            );
            expect(req.request.body.length).toBe(3);
            expect(req.request.body).toEqual(vms);
            req.flush(mockVirtualEquipment);
        });
    });

    describe("create", () => {
        it("should create a new virtual equipment", () => {
            const newEquipment: InVirtualEquipmentRest = {
                ...mockVirtualEquipment,
                id: 0,
                name: "VM-New",
            };
            const createdEquipment = { ...newEquipment, id: 999 };

            service.create(newEquipment).subscribe((equipment) => {
                expect(equipment).toEqual(createdEquipment);
                expect(equipment.id).toBe(999);
                expect(equipment.name).toBe("VM-New");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${newEquipment.digitalServiceVersionUid}/inputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("POST");
            expect(req.request.headers.get("content-type")).toBe("application/json");
            expect(req.request.body).toEqual(newEquipment);
            req.flush(createdEquipment);
        });

        it("should send correct headers with create request", () => {
            service.create(mockVirtualEquipment).subscribe();

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockVirtualEquipment.digitalServiceVersionUid}/inputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.headers.has("content-type")).toBe(true);
            expect(req.request.headers.get("content-type")).toBe("application/json");
            req.flush(mockVirtualEquipment);
        });

        it("should handle HTTP errors on create", () => {
            const errorMessage = "Duplicate name";

            service.create(mockVirtualEquipment).subscribe(
                () => fail("should have failed with 409 error"),
                (error) => {
                    expect(error.status).toBe(409);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockVirtualEquipment.digitalServiceVersionUid}/inputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, { status: 409, statusText: "Conflict" });
        });

        it("should create equipment with all properties", () => {
            const completeEquipment: InVirtualEquipmentRest = {
                id: 0,
                name: "Complete-VM",
                inventoryId: 123,
                digitalServiceUid: "ds-123",
                digitalServiceVersionUid: "dsv-456",
                datacenterName: "DC-Complete",
                physicalEquipmentName: "Server-Complete",
                quantity: 10,
                infrastructureType: "ON_PREMISE",
                instanceType: "m5.large",
                type: "storage",
                provider: "Azure",
                location: "US-East-1",
                durationHour: 8760,
                workload: 95,
                electricityConsumption: 750,
                vcpuCoreNumber: 8,
                sizeMemoryMb: 16384,
                sizeDiskGb: 500,
                allocationFactor: 0.85,
            };

            service.create(completeEquipment).subscribe((equipment) => {
                expect(equipment.name).toBe("Complete-VM");
                expect(equipment.quantity).toBe(10);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${completeEquipment.digitalServiceVersionUid}/inputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.body).toEqual(completeEquipment);
            req.flush({ ...completeEquipment, id: 100 });
        });

        it("should handle network errors on create", () => {
            service.create(mockVirtualEquipment).subscribe(
                () => fail("should have failed with network error"),
                (error) => {
                    expect(error.error.type).toBe("error");
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockVirtualEquipment.digitalServiceVersionUid}/inputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.error(new ErrorEvent("error", { message: "Network error" }));
        });
    });

    describe("delete", () => {
        it("should delete a virtual equipment", () => {
            const id = 123;
            const digitalServiceVersionUid = "dsv-delete";
            const mockResponse = mockVirtualEquipment;

            service.delete(id, digitalServiceVersionUid).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceVersionUid}/inputs/virtual-equipments/${id}`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("DELETE");
            req.flush(mockResponse);
        });

        it("should handle HTTP errors on delete", () => {
            const id = 404;
            const digitalServiceVersionUid = "dsv-error";
            const errorMessage = "Not found";

            service.delete(id, digitalServiceVersionUid).subscribe(
                () => fail("should have failed with 404 error"),
                (error) => {
                    expect(error.status).toBe(404);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceVersionUid}/inputs/virtual-equipments/${id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, { status: 404, statusText: "Not Found" });
        });

        it("should delete equipment with different IDs", () => {
            const id1 = 1;
            const id2 = 2;
            const digitalServiceVersionUid = "dsv-123";

            service.delete(id1, digitalServiceVersionUid).subscribe();
            const req1 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceVersionUid}/inputs/virtual-equipments/${id1}`,
            );
            req1.flush(mockVirtualEquipment);

            service.delete(id2, digitalServiceVersionUid).subscribe();
            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceVersionUid}/inputs/virtual-equipments/${id2}`,
            );
            req2.flush(mockVirtualEquipment);
        });

        it("should handle unauthorized errors on delete", () => {
            const id = 99;
            const digitalServiceVersionUid = "dsv-unauthorized";

            service.delete(id, digitalServiceVersionUid).subscribe(
                () => fail("should have failed with 403 error"),
                (error) => {
                    expect(error.status).toBe(403);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceVersionUid}/inputs/virtual-equipments/${id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush("Forbidden", { status: 403, statusText: "Forbidden" });
        });
    });

    describe("URL construction", () => {
        it("should construct correct URL for getByDigitalService", () => {
            const digitalServiceUid = "test-ds-uid";

            service.getByDigitalService(digitalServiceUid).subscribe();

            const req = httpMock.expectOne(
                (request) =>
                    request.url.includes(Constants.ENDPOINTS.digitalServicesVersions) &&
                    request.url.includes(digitalServiceUid) &&
                    request.url.includes("inputs/virtual-equipments"),
            );
            expect(req.request.url).toContain(digitalServiceUid);
            req.flush([]);
        });

        it("should construct correct URL for getByInventory", () => {
            const inventoryId = 777;

            service.getByInventory(inventoryId).subscribe();

            const req = httpMock.expectOne(
                (request) =>
                    request.url.includes(Constants.ENDPOINTS.inventories) &&
                    request.url.includes(inventoryId.toString()) &&
                    request.url.includes("inputs/virtual-equipments"),
            );
            expect(req.request.url).toContain(inventoryId.toString());
            req.flush([]);
        });
    });

    describe("Multiple concurrent operations", () => {
        it("should handle multiple concurrent requests", () => {
            const uid1 = "ds-1";
            const uid2 = "ds-2";

            service.getByDigitalService(uid1).subscribe();
            service.getByDigitalService(uid2).subscribe();

            const req1 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${uid1}/inputs/virtual-equipments`,
            );
            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${uid2}/inputs/virtual-equipments`,
            );

            req1.flush([mockVirtualEquipment]);
            req2.flush([]);
        });

        it("should handle mixed CRUD operations", () => {
            const digitalServiceUid = "ds-mixed";
            const newEquipment = { ...mockVirtualEquipment, id: 0, name: "New VM" };

            service.getByDigitalService(digitalServiceUid).subscribe();
            service.create(newEquipment).subscribe();
            service.update(mockVirtualEquipment).subscribe();

            const reqGet = httpMock.expectOne(
                (req) => req.method === "GET" && req.url.includes(digitalServiceUid),
            );
            const reqCreate = httpMock.expectOne((req) => req.method === "POST");
            const reqUpdate = httpMock.expectOne(
                (req) =>
                    req.method === "PUT" &&
                    req.url.includes(mockVirtualEquipment.id.toString()),
            );

            reqGet.flush([mockVirtualEquipment]);
            reqCreate.flush(newEquipment);
            reqUpdate.flush(mockVirtualEquipment);
        });
    });

    describe("Observable behavior", () => {
        it("should return an Observable for all methods", () => {
            const digitalServiceUid = "obs-test";
            const inventoryId = 1;
            const equipment = mockVirtualEquipment;

            expect(typeof service.getByDigitalService(digitalServiceUid).subscribe).toBe(
                "function",
            );
            expect(typeof service.getByInventory(inventoryId).subscribe).toBe("function");
            expect(typeof service.update(equipment).subscribe).toBe("function");
            expect(typeof service.updateAllVms([equipment], "dsv-1", 1).subscribe).toBe(
                "function",
            );
            expect(typeof service.create(equipment).subscribe).toBe("function");
            expect(typeof service.delete(1, "dsv-1").subscribe).toBe("function");

            httpMock.match(() => true).forEach((req) => req.flush({}));
        });
    });
});
