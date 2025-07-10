import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Constants } from "src/constants";
import { TaskRest } from "../../interfaces/inventory.interfaces";
import { TaskDataService } from "./task-data.service";

describe("TaskDataService", () => {
    let service: TaskDataService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [TaskDataService],
        });

        service = TestBed.inject(TaskDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify(); // ensures no open requests
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    it("should fetch task by ID", () => {
        const mockTask: TaskRest = {
            id: 1,
            status: "COMPLETED",
            creationDate: new Date(),
            // add other TaskRest fields as needed
        } as TaskRest;

        service.getTask(1).subscribe((task) => {
            expect(task).toEqual(mockTask);
        });

        const req = httpMock.expectOne(`${Constants.ENDPOINTS.task}/1`);
        expect(req.request.method).toBe("GET");
        req.flush(mockTask);
    });
});
