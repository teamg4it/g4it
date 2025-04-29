import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Constants } from "src/constants";
import { WorkspaceDataService } from "./workspace.data.service";

describe("WorkspaceDataService", () => {
    let service: WorkspaceDataService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [WorkspaceDataService],
        });

        service = TestBed.inject(WorkspaceDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    describe("getDomainSubscribers", () => {
        it("should send a POST request to the correct endpoint and return DomainSubscribers[]", () => {
            const mockResponse = [
                {
                    id: 1,
                    name: "test1@example.com",
                    organizations: [
                        { id: 22, name: "org1", status: "active" },
                        { id: 32, name: "org2", status: "inactive" },
                    ],
                },
                {
                    id: 2,
                    name: "test2@example.com",
                    organizations: [
                        { id: 33, name: "org3", status: "active" },
                        { id: 23, name: "org4", status: "inactive" },
                    ],
                },
            ];
            const requestBody = { email: "test@example.com" };
            const expectedUrl = `${Constants.ENDPOINTS.workspace}/domain-subscribers`;

            service.getDomainSubscribers(requestBody).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            const req = httpMock.expectOne(expectedUrl);
            expect(req.request.method).toBe("POST");
            expect(req.request.body).toEqual(requestBody);
            req.flush(mockResponse);
        });
    });
});
