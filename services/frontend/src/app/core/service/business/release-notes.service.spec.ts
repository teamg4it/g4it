import { TestBed } from "@angular/core/testing";
import { of } from "rxjs";
import { ReleaseNoteDataService } from "../data/release-notes-data.service";
import { ReleaseNotesService } from "./release-notes.service";

describe("ReleaseNotesService", () => {
    let service: ReleaseNotesService;
    let dataService: jasmine.SpyObj<ReleaseNoteDataService>;

    beforeEach(() => {
        dataService = jasmine.createSpyObj("ReleaseNoteDataService", [
            "getLatestReleaseNotes",
            "postReleaseNotesSeen",
        ]);
        dataService.getLatestReleaseNotes.and.returnValue(
            of({ content: "Latest notes" }),
        );
        dataService.postReleaseNotesSeen.and.returnValue(of({}));

        TestBed.configureTestingModule({
            providers: [
                ReleaseNotesService,
                { provide: ReleaseNoteDataService, useValue: dataService },
            ],
        });
        service = TestBed.inject(ReleaseNotesService);
    });

    it("returns the latest release notes from the data service", () => {
        let result: unknown;
        service.getLatestReleaseNotes().subscribe((notes) => (result = notes));

        expect(dataService.getLatestReleaseNotes).toHaveBeenCalled();
        expect(result).toEqual({ content: "Latest notes" });
    });

    it("records that the current user has seen the release notes", () => {
        let result: unknown;
        service.markAsSeen().subscribe((response) => (result = response));

        expect(dataService.postReleaseNotesSeen).toHaveBeenCalled();
        expect(result).toEqual({});
    });
});
