import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { OutVirtualEquipmentRest } from "src/app/core/interfaces/output.interface";
import { Constants } from "src/constants";
import { OutVirtualEquipmentsService } from "./out-virtual-equipments.service";

describe("OutVirtualEquipmentsService", () => {
    let service: OutVirtualEquipmentsService;
    let httpMock: HttpTestingController;

    const mockVirtualEquipment: OutVirtualEquipmentRest = {
        name: "VM-Test-1",
        criterion: "climate-change",
        lifecycleStep: "use",
        datacenterName: "DC-Paris",
        physicalEquipmentName: "Server-01",
        infrastructureType: "CLOUD",
        instanceType: "t2.medium",
        type: "compute",
        provider: "AWS",
        equipmentType: "Virtual Machine",
        location: "EU-West-1",
        engineName: "TestEngine",
        engineVersion: "1.0.0",
        referentialVersion: "1.0",
        statusIndicator: "OK",
        countValue: 100,
        quantity: 5,
        unitImpact: 10.5,
        peopleEqImpact: 2.3,
        electricityConsumption: 500,
        unit: "kgCO2eq",
        usageDuration: 8760,
        workload: 80,
    };

    const mockVirtualEquipmentList: OutVirtualEquipmentRest[] = [
        mockVirtualEquipment,
        {
            ...mockVirtualEquipment,
            name: "VM-Test-2",
            quantity: 3,
            unitImpact: 8.2,
        },
        {
            ...mockVirtualEquipment,
            name: "VM-Test-3",
            provider: "Azure",
            instanceType: "Standard_D2s_v3",
        },
    ];

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [OutVirtualEquipmentsService],
        });
        service = TestBed.inject(OutVirtualEquipmentsService);
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

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("GET");
            req.flush(mockVirtualEquipmentList);
        });

        it("should return empty array when no virtual equipments exist", () => {
            const digitalServiceUid = "ds-empty-uid";
            const emptyList: OutVirtualEquipmentRest[] = [];

            service.getByDigitalService(digitalServiceUid).subscribe((equipments) => {
                expect(equipments).toEqual([]);
                expect(equipments.length).toBe(0);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("GET");
            req.flush(emptyList);
        });

        it("should retrieve virtual equipments with different providers", () => {
            const digitalServiceUid = "ds-providers-uid";
            const equipmentList: OutVirtualEquipmentRest[] = [
                { ...mockVirtualEquipment, provider: "AWS" },
                { ...mockVirtualEquipment, provider: "Azure" },
                { ...mockVirtualEquipment, provider: "GCP" },
            ];

            service.getByDigitalService(digitalServiceUid).subscribe((equipments) => {
                expect(equipments.length).toBe(3);
                expect(equipments[0].provider).toBe("AWS");
                expect(equipments[1].provider).toBe("Azure");
                expect(equipments[2].provider).toBe("GCP");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(equipmentList);
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

            const expectedUrl = `${Constants.ENDPOINTS.inventories}/${inventoryId}/outputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("GET");
            req.flush(mockVirtualEquipmentList);
        });

        it("should return empty array when no virtual equipments exist", () => {
            const inventoryId = 456;
            const emptyList: OutVirtualEquipmentRest[] = [];

            service.getByInventory(inventoryId).subscribe((equipments) => {
                expect(equipments).toEqual([]);
                expect(equipments.length).toBe(0);
            });

            const expectedUrl = `${Constants.ENDPOINTS.inventories}/${inventoryId}/outputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("GET");
            req.flush(emptyList);
        });

        it("should handle HTTP errors gracefully", () => {
            const inventoryId = 789;
            const errorMessage = "Unauthorized";

            service.getByInventory(inventoryId).subscribe(
                () => fail("should have failed with 401 error"),
                (error) => {
                    expect(error.status).toBe(401);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.inventories}/${inventoryId}/outputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, { status: 401, statusText: "Unauthorized" });
        });

        it("should handle network errors", () => {
            const inventoryId = 999;

            service.getByInventory(inventoryId).subscribe(
                () => fail("should have failed with network error"),
                (error) => {
                    expect(error.error.type).toBe("error");
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.inventories}/${inventoryId}/outputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.error(new ErrorEvent("error", { message: "Network error" }));
        });

        it("should handle 404 not found errors", () => {
            const inventoryId = 404;

            service.getByInventory(inventoryId).subscribe(
                () => fail("should have failed with 404 error"),
                (error) => {
                    expect(error.status).toBe(404);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.inventories}/${inventoryId}/outputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush("Inventory not found", { status: 404, statusText: "Not Found" });
        });

        it("should handle different inventory IDs", () => {
            const inventoryId1 = 100;
            const inventoryId2 = 200;

            service.getByInventory(inventoryId1).subscribe();
            const req1 = httpMock.expectOne(
                `${Constants.ENDPOINTS.inventories}/${inventoryId1}/outputs/virtual-equipments`,
            );
            req1.flush([mockVirtualEquipment]);

            service.getByInventory(inventoryId2).subscribe();
            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.inventories}/${inventoryId2}/outputs/virtual-equipments`,
            );
            req2.flush([]);
        });

        it("should retrieve virtual equipments with complete data", () => {
            const inventoryId = 555;
            const completeEquipment: OutVirtualEquipmentRest = {
                name: "Complete-VM",
                criterion: "climate-change",
                lifecycleStep: "use",
                datacenterName: "DC-Complete",
                physicalEquipmentName: "Server-Complete",
                infrastructureType: "CLOUD",
                instanceType: "m5.large",
                type: "compute",
                provider: "AWS",
                equipmentType: "Virtual Machine",
                location: "US-East-1",
                engineName: "CompleteEngine",
                engineVersion: "2.0.0",
                referentialVersion: "2.0",
                statusIndicator: "OK",
                countValue: 200,
                quantity: 10,
                unitImpact: 15.7,
                peopleEqImpact: 3.5,
                electricityConsumption: 750,
                unit: "kgCO2eq",
                usageDuration: 8760,
                workload: 90,
                commonFilters: ["common1", "common2"],
                filters: ["location:US"],
                filtersPhysicalEquipment: ["type:compute"],
                errors: [],
            };

            service.getByInventory(inventoryId).subscribe((equipments) => {
                expect(equipments[0]).toEqual(completeEquipment);
                expect(equipments[0].name).toBe("Complete-VM");
                expect(equipments[0].quantity).toBe(10);
                expect(equipments[0].commonFilters).toEqual(["common1", "common2"]);
            });

            const expectedUrl = `${Constants.ENDPOINTS.inventories}/${inventoryId}/outputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush([completeEquipment]);
        });

        it("should handle zero inventory ID", () => {
            const inventoryId = 0;

            service.getByInventory(inventoryId).subscribe();

            const expectedUrl = `${Constants.ENDPOINTS.inventories}/${inventoryId}/outputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush([]);
        });

        it("should handle large inventory ID", () => {
            const inventoryId = 999999999;

            service.getByInventory(inventoryId).subscribe();

            const expectedUrl = `${Constants.ENDPOINTS.inventories}/${inventoryId}/outputs/virtual-equipments`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush([mockVirtualEquipment]);
        });
    });

    describe("URL construction", () => {
        it("should construct correct URL for digital service request", () => {
            const digitalServiceUid = "test-ds-uid";

            service.getByDigitalService(digitalServiceUid).subscribe();

            const req = httpMock.expectOne(
                (request) =>
                    request.url.includes(Constants.ENDPOINTS.digitalServicesVersions) &&
                    request.url.includes(digitalServiceUid) &&
                    request.url.includes("outputs/virtual-equipments"),
            );
            expect(req.request.url).toContain(digitalServiceUid);
            req.flush([]);
        });

        it("should construct correct URL for inventory request", () => {
            const inventoryId = 777;

            service.getByInventory(inventoryId).subscribe();

            const req = httpMock.expectOne(
                (request) =>
                    request.url.includes(Constants.ENDPOINTS.inventories) &&
                    request.url.includes(inventoryId.toString()) &&
                    request.url.includes("outputs/virtual-equipments"),
            );
            expect(req.request.url).toContain(inventoryId.toString());
            req.flush([]);
        });
    });

    describe("Multiple concurrent requests", () => {
        it("should handle multiple concurrent digital service requests", () => {
            const uid1 = "ds-1";
            const uid2 = "ds-2";
            const uid3 = "ds-3";

            service.getByDigitalService(uid1).subscribe((equipments) => {
                expect(equipments.length).toBe(1);
            });
            service.getByDigitalService(uid2).subscribe((equipments) => {
                expect(equipments.length).toBe(2);
            });
            service.getByDigitalService(uid3).subscribe((equipments) => {
                expect(equipments.length).toBe(0);
            });

            const req1 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${uid1}/outputs/virtual-equipments`,
            );
            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${uid2}/outputs/virtual-equipments`,
            );
            const req3 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${uid3}/outputs/virtual-equipments`,
            );

            req1.flush([mockVirtualEquipment]);
            req2.flush([mockVirtualEquipment, { ...mockVirtualEquipment, name: "VM-2" }]);
            req3.flush([]);
        });

        it("should handle multiple concurrent inventory requests", () => {
            const id1 = 1;
            const id2 = 2;
            const id3 = 3;

            service.getByInventory(id1).subscribe((equipments) => {
                expect(equipments.length).toBe(1);
            });
            service.getByInventory(id2).subscribe((equipments) => {
                expect(equipments.length).toBe(2);
            });
            service.getByInventory(id3).subscribe((equipments) => {
                expect(equipments.length).toBe(0);
            });

            const req1 = httpMock.expectOne(
                `${Constants.ENDPOINTS.inventories}/${id1}/outputs/virtual-equipments`,
            );
            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.inventories}/${id2}/outputs/virtual-equipments`,
            );
            const req3 = httpMock.expectOne(
                `${Constants.ENDPOINTS.inventories}/${id3}/outputs/virtual-equipments`,
            );

            req1.flush([mockVirtualEquipment]);
            req2.flush([mockVirtualEquipment, { ...mockVirtualEquipment, name: "VM-2" }]);
            req3.flush([]);
        });

        it("should handle mixed digital service and inventory requests", () => {
            const digitalServiceUid = "mixed-ds";
            const inventoryId = 888;

            service.getByDigitalService(digitalServiceUid).subscribe();
            service.getByInventory(inventoryId).subscribe();

            const reqDs = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/outputs/virtual-equipments`,
            );
            const reqInv = httpMock.expectOne(
                `${Constants.ENDPOINTS.inventories}/${inventoryId}/outputs/virtual-equipments`,
            );

            reqDs.flush([mockVirtualEquipment]);
            reqInv.flush(mockVirtualEquipmentList);
        });
    });
});
