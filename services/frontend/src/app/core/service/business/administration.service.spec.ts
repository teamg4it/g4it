import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Constants } from "src/constants";
import { AdministrationService } from "./administration.service";

describe("AdministrationService", () => {
    let service: AdministrationService;
    let httpMock: HttpTestingController;
    let organizationId = 1;
    let searchName = "sop";
    let workspaceId = 1;
    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [AdministrationService],
        });
        service = TestBed.inject(AdministrationService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    it("should have getOrganizationData function", () => {
        expect(service.getOrganizations).toBeTruthy();
    });

    it("should delete a organization", () => {
        const updateJson = {
            organizationId: 1,
            name: "DEMO",
            status: Constants.WORKSPACE_STATUSES.TO_BE_DELETED,
            dataRetentionDay: 7,
        };
        service.updateWorkspace(updateJson.organizationId, updateJson).subscribe();

        const req = httpMock.expectOne(`administrator/workspaces?workspaceId=1`);
        expect(req.request.method).toEqual("PUT");

        httpMock.verify();
    });

    it("should revert deleted organization", () => {
        const updateJson = {
            organizationId: 1,
            name: "DEMO",
            status: Constants.WORKSPACE_STATUSES.ACTIVE,
            dataRetentionDay: null,
        };
        service.updateWorkspace(updateJson.organizationId, updateJson).subscribe();

        const req = httpMock.expectOne(`administrator/workspaces?workspaceId=1`);
        expect(req.request.method).toEqual("PUT");

        httpMock.verify();
    });

    it("should get User Details", () => {
        service.getUserDetails(organizationId).subscribe();
        const req = httpMock.expectOne(`administrator/workspaces/users?workspaceId=1`);
        expect(req.request.method).toEqual("GET");

        httpMock.verify();
    });

    it("should get user Search Deatails", () => {
        service.getSearchDetails(searchName, organizationId, workspaceId).subscribe();
        const req = httpMock.expectOne(
            `administrator/organizations/users?searchedName=${searchName}&organizationId=${organizationId}&workspaceId=${workspaceId}`,
        );
        expect(req.request.method).toEqual("GET");

        httpMock.verify();
    });

    it("should save organization", () => {
        const body = {
            organizationId: organizationId,
            users: [
                {
                    userId: 1,
                    roles: [
                        "ROLE_INVENTORY_READ",
                        "ROLE_INVENTORY_WRITE",
                        "ROLE_DIGITAL_SERVICE_READ",
                        "ROLE_DIGITAL_SERVICE_WRITE",
                    ],
                },
            ],
        };
        service.postUserToWorkspaceAndAddRoles(body).subscribe();

        const req = httpMock.expectOne(`administrator/workspaces/users`);
        expect(req.request.method).toEqual("POST");

        httpMock.verify();
    });
    it("should update organization", () => {
        const body = {
            organizationId: organizationId,
            users: [
                {
                    userId: 1,
                    roles: [
                        "ROLE_INVENTORY_READ",
                        "ROLE_INVENTORY_WRITE",
                        "ROLE_DIGITAL_SERVICE_READ",
                        "ROLE_DIGITAL_SERVICE_WRITE",
                    ],
                },
            ],
        };
        service.postUserToWorkspaceAndAddRoles(body).subscribe();

        const req = httpMock.expectOne(`administrator/workspaces/users`);
        expect(req.request.method).toEqual("POST");

        httpMock.verify();
    });
    it("should delete organization", () => {
        const body = {
            organizationId: organizationId,
            users: [
                {
                    userId: 1,
                    roles: [
                        "ROLE_INVENTORY_READ",
                        "ROLE_INVENTORY_WRITE",
                        "ROLE_DIGITAL_SERVICE_READ",
                        "ROLE_DIGITAL_SERVICE_WRITE",
                    ],
                },
            ],
        };
        service.deleteUserDetails(body).subscribe();

        const req = httpMock.expectOne(`administrator/workspaces/users`);
        expect(req.request.method).toEqual("DELETE");

        httpMock.verify();
    });
});
