import { TestBed } from "@angular/core/testing";
import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { InDatacentersService } from "./in-datacenters.service";
import { InDatacenterRest } from "../../../interfaces/input.interface";
import { Constants } from "src/constants";

describe("InDatacentersService", () => {
    let service: InDatacentersService;
    let httpMock: HttpTestingController;

    const mockDatacenter: InDatacenterRest = {
        id: 1,
        inventoryId: 123,
        digitalServiceUid: "ds-123",
        name: "DC-Paris",
        pue: 1.5,
        location: "France",
        displayLabel: "Paris Data Center",
    };

    const mockDatacenterList: InDatacenterRest[] = [
        mockDatacenter,
        {
            id: 2,
            inventoryId: 124,
            digitalServiceUid: "ds-123",
            name: "DC-London",
            pue: 1.3,
            location: "United Kingdom",
            displayLabel: "London Data Center",
        },
        {
            id: 3,
            inventoryId: 125,
            digitalServiceUid: "ds-123",
            name: "DC-Frankfurt",
            pue: 1.4,
            location: "Germany",
            displayLabel: "Frankfurt Data Center",
        },
    ];

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [InDatacentersService],
        });
        service = TestBed.inject(InDatacentersService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    describe("get", () => {
        it("should retrieve datacenters for a digital service", () => {
            const digitalServiceUid = "ds-123-uid";

            service.get(digitalServiceUid).subscribe((datacenters) => {
                expect(datacenters).toEqual(mockDatacenterList);
                expect(datacenters.length).toBe(3);
                expect(datacenters[0].name).toBe("DC-Paris");
                expect(datacenters[0].pue).toBe(1.5);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/datacenters`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("GET");
            req.flush(mockDatacenterList);
        });

        it("should return empty array when no datacenters exist", () => {
            const digitalServiceUid = "ds-empty-uid";
            const emptyList: InDatacenterRest[] = [];

            service.get(digitalServiceUid).subscribe((datacenters) => {
                expect(datacenters).toEqual([]);
                expect(datacenters.length).toBe(0);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/datacenters`;
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

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/datacenters`;
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

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/datacenters`;
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

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/datacenters`;
            const req = httpMock.expectOne(expectedUrl);
            req.error(new ErrorEvent("error", { message: "Network error" }));
        });

        it("should retrieve datacenters with different PUE values", () => {
            const digitalServiceUid = "ds-pue-uid";
            const datacenterList: InDatacenterRest[] = [
                { ...mockDatacenter, pue: 1.2 },
                { ...mockDatacenter, pue: 1.5 },
                { ...mockDatacenter, pue: 2.0 },
            ];

            service.get(digitalServiceUid).subscribe((datacenters) => {
                expect(datacenters.length).toBe(3);
                expect(datacenters[0].pue).toBe(1.2);
                expect(datacenters[1].pue).toBe(1.5);
                expect(datacenters[2].pue).toBe(2.0);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/datacenters`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(datacenterList);
        });

        it("should handle different digital service UIDs", () => {
            const digitalServiceUid1 = "ds-uid-1";
            const digitalServiceUid2 = "ds-uid-2";

            service.get(digitalServiceUid1).subscribe((datacenters) => {
                expect(datacenters.length).toBe(1);
            });

            const req1 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid1}/inputs/datacenters`,
            );
            req1.flush([mockDatacenter]);

            service.get(digitalServiceUid2).subscribe((datacenters) => {
                expect(datacenters.length).toBe(0);
            });

            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid2}/inputs/datacenters`,
            );
            req2.flush([]);
        });

        it("should retrieve datacenters with different locations", () => {
            const digitalServiceUid = "ds-locations-uid";
            const datacenterList: InDatacenterRest[] = [
                { ...mockDatacenter, location: "France" },
                { ...mockDatacenter, location: "United States" },
                { ...mockDatacenter, location: "Japan" },
            ];

            service.get(digitalServiceUid).subscribe((datacenters) => {
                expect(datacenters.length).toBe(3);
                expect(datacenters[0].location).toBe("France");
                expect(datacenters[1].location).toBe("United States");
                expect(datacenters[2].location).toBe("Japan");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/datacenters`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(datacenterList);
        });
    });

    describe("update", () => {
        it("should update a datacenter", () => {
            const updatedDatacenter = {
                ...mockDatacenter,
                name: "DC-Paris-Updated",
                pue: 1.2,
                location: "France - Paris",
            };

            service.update(updatedDatacenter).subscribe((datacenter) => {
                expect(datacenter).toEqual(updatedDatacenter);
                expect(datacenter.name).toBe("DC-Paris-Updated");
                expect(datacenter.pue).toBe(1.2);
                expect(datacenter.location).toBe("France - Paris");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockDatacenter.digitalServiceUid}/inputs/datacenters/${mockDatacenter.id}`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("PUT");
            expect(req.request.headers.get("content-type")).toBe("application/json");
            expect(req.request.body).toEqual(updatedDatacenter);
            req.flush(updatedDatacenter);
        });

        it("should send correct headers with update request", () => {
            service.update(mockDatacenter).subscribe();

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockDatacenter.digitalServiceUid}/inputs/datacenters/${mockDatacenter.id}`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.headers.has("content-type")).toBe(true);
            expect(req.request.headers.get("content-type")).toBe("application/json");
            req.flush(mockDatacenter);
        });

        it("should handle HTTP 400 errors on update", () => {
            const errorMessage = "Validation error";

            service.update(mockDatacenter).subscribe(
                () => fail("should have failed with 400 error"),
                (error) => {
                    expect(error.status).toBe(400);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockDatacenter.digitalServiceUid}/inputs/datacenters/${mockDatacenter.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, { status: 400, statusText: "Bad Request" });
        });

        it("should handle network errors on update", () => {
            service.update(mockDatacenter).subscribe(
                () => fail("should have failed with network error"),
                (error) => {
                    expect(error.error.type).toBe("error");
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockDatacenter.digitalServiceUid}/inputs/datacenters/${mockDatacenter.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.error(new ErrorEvent("error", { message: "Network error" }));
        });

        it("should update datacenter with modified PUE", () => {
            const modifiedDatacenter = {
                ...mockDatacenter,
                pue: 1.1,
            };

            service.update(modifiedDatacenter).subscribe((datacenter) => {
                expect(datacenter.pue).toBe(1.1);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${modifiedDatacenter.digitalServiceUid}/inputs/datacenters/${modifiedDatacenter.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(modifiedDatacenter);
        });

        it("should handle unauthorized errors on update", () => {
            service.update(mockDatacenter).subscribe(
                () => fail("should have failed with 401 error"),
                (error) => {
                    expect(error.status).toBe(401);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockDatacenter.digitalServiceUid}/inputs/datacenters/${mockDatacenter.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush("Unauthorized", { status: 401, statusText: "Unauthorized" });
        });

        it("should update datacenter displayLabel", () => {
            const modifiedDatacenter = {
                ...mockDatacenter,
                displayLabel: "Updated Paris Data Center",
            };

            service.update(modifiedDatacenter).subscribe((datacenter) => {
                expect(datacenter.displayLabel).toBe("Updated Paris Data Center");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${modifiedDatacenter.digitalServiceUid}/inputs/datacenters/${modifiedDatacenter.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(modifiedDatacenter);
        });
    });

    describe("create", () => {
        it("should create a new datacenter", () => {
            const newDatacenter: InDatacenterRest = {
                id: undefined,
                inventoryId: 126,
                digitalServiceUid: "ds-123",
                name: "DC-New",
                pue: 1.6,
                location: "Spain",
                displayLabel: "New Data Center",
            };
            const createdDatacenter = { ...newDatacenter, id: 999 };

            service.create(newDatacenter).subscribe((datacenter) => {
                expect(datacenter).toEqual(createdDatacenter);
                expect(datacenter.id).toBe(999);
                expect(datacenter.name).toBe("DC-New");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${newDatacenter.digitalServiceUid}/inputs/datacenters`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("POST");
            expect(req.request.headers.get("content-type")).toBe("application/json");
            expect(req.request.body).toEqual(newDatacenter);
            req.flush(createdDatacenter);
        });

        it("should send correct headers with create request", () => {
            service.create(mockDatacenter).subscribe();

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockDatacenter.digitalServiceUid}/inputs/datacenters`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.headers.has("content-type")).toBe(true);
            expect(req.request.headers.get("content-type")).toBe("application/json");
            req.flush(mockDatacenter);
        });

        it("should handle HTTP 409 conflict errors on create", () => {
            const errorMessage = "Datacenter already exists";

            service.create(mockDatacenter).subscribe(
                () => fail("should have failed with 409 error"),
                (error) => {
                    expect(error.status).toBe(409);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockDatacenter.digitalServiceUid}/inputs/datacenters`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, { status: 409, statusText: "Conflict" });
        });

        it("should handle network errors on create", () => {
            service.create(mockDatacenter).subscribe(
                () => fail("should have failed with network error"),
                (error) => {
                    expect(error.error.type).toBe("error");
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockDatacenter.digitalServiceUid}/inputs/datacenters`;
            const req = httpMock.expectOne(expectedUrl);
            req.error(new ErrorEvent("error", { message: "Network error" }));
        });

        it("should create datacenter with all properties", () => {
            const completeDatacenter: InDatacenterRest = {
                id: undefined,
                inventoryId: 999,
                digitalServiceUid: "ds-complete",
                name: "DC-Complete",
                pue: 1.8,
                location: "Netherlands",
                displayLabel: "Complete Data Center",
            };

            service.create(completeDatacenter).subscribe((datacenter) => {
                expect(datacenter.name).toBe("DC-Complete");
                expect(datacenter.pue).toBe(1.8);
                expect(datacenter.location).toBe("Netherlands");
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${completeDatacenter.digitalServiceUid}/inputs/datacenters`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.body).toEqual(completeDatacenter);
            req.flush({ ...completeDatacenter, id: 100 });
        });

        it("should handle validation errors on create", () => {
            const invalidDatacenter = { ...mockDatacenter, name: "" };

            service.create(invalidDatacenter).subscribe(
                () => fail("should have failed with validation error"),
                (error) => {
                    expect(error.status).toBe(400);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${invalidDatacenter.digitalServiceUid}/inputs/datacenters`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush("Validation error", { status: 400, statusText: "Bad Request" });
        });

        it("should create datacenter with high PUE value", () => {
            const highPueDatacenter: InDatacenterRest = {
                id: undefined,
                inventoryId: 200,
                digitalServiceUid: "ds-123",
                name: "DC-HighPUE",
                pue: 2.5,
                location: "Remote",
                displayLabel: "High PUE Data Center",
            };

            service.create(highPueDatacenter).subscribe((datacenter) => {
                expect(datacenter.pue).toBe(2.5);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${highPueDatacenter.digitalServiceUid}/inputs/datacenters`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush({ ...highPueDatacenter, id: 201 });
        });

        it("should create datacenter with low PUE value", () => {
            const lowPueDatacenter: InDatacenterRest = {
                id: undefined,
                inventoryId: 300,
                digitalServiceUid: "ds-123",
                name: "DC-LowPUE",
                pue: 1.1,
                location: "Efficient",
                displayLabel: "Low PUE Data Center",
            };

            service.create(lowPueDatacenter).subscribe((datacenter) => {
                expect(datacenter.pue).toBe(1.1);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${lowPueDatacenter.digitalServiceUid}/inputs/datacenters`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush({ ...lowPueDatacenter, id: 301 });
        });
    });

    describe("delete", () => {
        it("should delete a datacenter", () => {
            const mockResponse = mockDatacenter;

            service.delete(mockDatacenter).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockDatacenter.digitalServiceUid}/inputs/datacenters/${mockDatacenter.id}`;
            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("DELETE");
            req.flush(mockResponse);
        });

        it("should handle HTTP 404 errors on delete", () => {
            const errorMessage = "Datacenter not found";

            service.delete(mockDatacenter).subscribe(
                () => fail("should have failed with 404 error"),
                (error) => {
                    expect(error.status).toBe(404);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockDatacenter.digitalServiceUid}/inputs/datacenters/${mockDatacenter.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, { status: 404, statusText: "Not Found" });
        });

        it("should handle network errors on delete", () => {
            service.delete(mockDatacenter).subscribe(
                () => fail("should have failed with network error"),
                (error) => {
                    expect(error.error.type).toBe("error");
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockDatacenter.digitalServiceUid}/inputs/datacenters/${mockDatacenter.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.error(new ErrorEvent("error", { message: "Network error" }));
        });

        it("should delete datacenters with different IDs", () => {
            const datacenter1 = { ...mockDatacenter, id: 1 };
            const datacenter2 = { ...mockDatacenter, id: 2 };

            service.delete(datacenter1).subscribe();
            const req1 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${datacenter1.digitalServiceUid}/inputs/datacenters/${datacenter1.id}`,
            );
            req1.flush(datacenter1);

            service.delete(datacenter2).subscribe();
            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${datacenter2.digitalServiceUid}/inputs/datacenters/${datacenter2.id}`,
            );
            req2.flush(datacenter2);
        });

        it("should handle forbidden errors on delete", () => {
            service.delete(mockDatacenter).subscribe(
                () => fail("should have failed with 403 error"),
                (error) => {
                    expect(error.status).toBe(403);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockDatacenter.digitalServiceUid}/inputs/datacenters/${mockDatacenter.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush("Forbidden", { status: 403, statusText: "Forbidden" });
        });

        it("should handle deletion with undefined id gracefully", () => {
            const datacenterWithoutId = { ...mockDatacenter, id: undefined };

            service.delete(datacenterWithoutId).subscribe();

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${datacenterWithoutId.digitalServiceUid}/inputs/datacenters/${datacenterWithoutId.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(datacenterWithoutId);
        });

        it("should handle conflict errors when datacenter is in use", () => {
            const errorMessage = "Cannot delete datacenter in use";

            service.delete(mockDatacenter).subscribe(
                () => fail("should have failed with 409 error"),
                (error) => {
                    expect(error.status).toBe(409);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const expectedUrl = `${Constants.ENDPOINTS.digitalServicesVersions}/${mockDatacenter.digitalServiceUid}/inputs/datacenters/${mockDatacenter.id}`;
            const req = httpMock.expectOne(expectedUrl);
            req.flush(errorMessage, { status: 409, statusText: "Conflict" });
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
                    request.url.includes("inputs/datacenters"),
            );
            expect(req.request.url).toContain(digitalServiceUid);
            req.flush([]);
        });

        it("should use correct endpoint from Constants", () => {
            const digitalServiceUid = "endpoint-test-uid";

            service.get(digitalServiceUid).subscribe();

            const req = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/datacenters`,
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

            service.get(uid1).subscribe((datacenters) => {
                expect(datacenters.length).toBe(1);
            });
            service.get(uid2).subscribe((datacenters) => {
                expect(datacenters.length).toBe(2);
            });
            service.get(uid3).subscribe((datacenters) => {
                expect(datacenters.length).toBe(0);
            });

            const req1 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${uid1}/inputs/datacenters`,
            );
            const req2 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${uid2}/inputs/datacenters`,
            );
            const req3 = httpMock.expectOne(
                `${Constants.ENDPOINTS.digitalServicesVersions}/${uid3}/inputs/datacenters`,
            );

            req1.flush([mockDatacenter]);
            req2.flush([mockDatacenter, { ...mockDatacenter, id: 2, name: "DC-London" }]);
            req3.flush([]);
        });

        it("should handle mixed CRUD operations", () => {
            const digitalServiceUid = "ds-mixed";
            const newDatacenter = {
                ...mockDatacenter,
                id: undefined,
                name: "DC-New",
            };

            service.get(digitalServiceUid).subscribe();
            service.create(newDatacenter).subscribe();
            service.update(mockDatacenter).subscribe();
            service.delete(mockDatacenter).subscribe();

            const reqGet = httpMock.expectOne((req) => req.method === "GET");
            const reqCreate = httpMock.expectOne((req) => req.method === "POST");
            const reqUpdate = httpMock.expectOne((req) => req.method === "PUT");
            const reqDelete = httpMock.expectOne((req) => req.method === "DELETE");

            reqGet.flush([mockDatacenter]);
            reqCreate.flush({ ...newDatacenter, id: 100 });
            reqUpdate.flush(mockDatacenter);
            reqDelete.flush(mockDatacenter);
        });
    });

    describe("Observable behavior", () => {
        it("should return an Observable for all methods", () => {
            const digitalServiceUid = "obs-test";
            const datacenter = mockDatacenter;

            expect(typeof service.get(digitalServiceUid).subscribe).toBe("function");
            expect(typeof service.update(datacenter).subscribe).toBe("function");
            expect(typeof service.create(datacenter).subscribe).toBe("function");
            expect(typeof service.delete(datacenter).subscribe).toBe("function");

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
                `${Constants.ENDPOINTS.digitalServicesVersions}/${digitalServiceUid}/inputs/datacenters`,
            );
            req.flush([mockDatacenter]);
        });
    });
});
