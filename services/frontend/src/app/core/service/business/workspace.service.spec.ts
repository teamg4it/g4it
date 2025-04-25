import { TestBed } from "@angular/core/testing";
import { of } from "rxjs";
import { Workspace } from "../../interfaces/administration.interfaces";
import { WorkspaceDataService } from "../data/workspace.data.service";
import { WorkspaceService } from "./workspace.service";

describe("WorkspaceService", () => {
    let service: WorkspaceService;
    let workspaceDataServiceSpy: jasmine.SpyObj<WorkspaceDataService>;

    beforeEach(() => {
        const spy = jasmine.createSpyObj("WorkspaceDataService", ["postUserWorkspace"]);

        TestBed.configureTestingModule({
            providers: [
                WorkspaceService,
                { provide: WorkspaceDataService, useValue: spy },
            ],
        });

        service = TestBed.inject(WorkspaceService);
        workspaceDataServiceSpy = TestBed.inject(
            WorkspaceDataService,
        ) as jasmine.SpyObj<WorkspaceDataService>;
    });

    it("should call postUserWorkspace with the correct body and return the expected result", (done) => {
        const mockBody = { subscriberId: 1, name: "Test Workspace", status: "active" };
        const mockResponse: Workspace = {
            id: 1,
            name: "Test Workspace",
            status: "active",
            defaultFlag: false,
        };

        workspaceDataServiceSpy.postUserWorkspace.and.returnValue(of(mockResponse));

        service.postUserWorkspace(mockBody).subscribe((result) => {
            expect(result).toEqual(mockResponse);
            expect(workspaceDataServiceSpy.postUserWorkspace).toHaveBeenCalledWith(
                mockBody,
            );
            done();
        });
    });
});
