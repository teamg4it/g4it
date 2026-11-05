import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Constants } from "src/constants";
import { ReleaseNoteDataService } from "./release-notes-data.service";

describe("ReleaseNoteDataService", () => {
    let service: ReleaseNoteDataService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [ReleaseNoteDataService],
        });
        service = TestBed.inject(ReleaseNoteDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => httpMock.verify());

    it("gets the latest published release notes", () => {
        service.getLatestReleaseNotes().subscribe();

        const request = httpMock.expectOne(Constants.ENDPOINTS.releaseNotes);
        expect(request.request.method).toBe("GET");
        request.flush({ content: "Latest notes" });
    });

    it("posts the seen acknowledgement without a request body", () => {
        service.postReleaseNotesSeen().subscribe();

        const request = httpMock.expectOne(Constants.ENDPOINTS.releaseNotesSeen);
        expect(request.request.method).toBe("POST");
        expect(request.request.body).toBeNull();
        request.flush({});
    });
});
