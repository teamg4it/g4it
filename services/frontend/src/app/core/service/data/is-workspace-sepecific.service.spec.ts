import { HttpErrorResponse } from "@angular/common/http";
import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Constants } from "src/constants";
import { IsWorkspaceSpecificService } from "./is-workspace-sepecific.service";

describe("IsWorkspaceSpecificService", () => {
    let service: IsWorkspaceSpecificService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [IsWorkspaceSpecificService],
        });
        service = TestBed.inject(IsWorkspaceSpecificService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it("should create", () => {
        expect(service).toBeTruthy();
    });

    for (const isWorkspaceSpecific of [true, false]) {
        it(`should return ${isWorkspaceSpecific} from the workspace-specific endpoint`, () => {
            const next = jasmine.createSpy("next");

            service.getIsWorkspaceSpecific().subscribe(next);

            const request = httpMock.expectOne(Constants.ENDPOINTS.isWorkspaceSpecific);
            expect(request.request.method).toBe("GET");
            request.flush(isWorkspaceSpecific);

            expect(next).toHaveBeenCalledOnceWith(isWorkspaceSpecific);
        });
    }

    it("should propagate HTTP errors", () => {
        const next = jasmine.createSpy("next");
        const error = jasmine.createSpy("error");

        service.getIsWorkspaceSpecific().subscribe({ next, error });

        const request = httpMock.expectOne(Constants.ENDPOINTS.isWorkspaceSpecific);
        request.flush("Server error", {
            status: 500,
            statusText: "Internal Server Error",
        });

        expect(next).not.toHaveBeenCalled();
        expect(error).toHaveBeenCalledOnceWith(jasmine.any(HttpErrorResponse));
        expect((error.calls.mostRecent().args[0] as HttpErrorResponse).status).toBe(500);
    });
});
