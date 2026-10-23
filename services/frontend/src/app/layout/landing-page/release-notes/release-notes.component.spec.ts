import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule } from "@ngx-translate/core";
import { of } from "rxjs";
import { ReleaseNotesService } from "src/app/core/service/business/release-notes.service";

import { ReleaseNotesComponent } from "./release-notes.component";

describe("ReleaseNotesComponent", () => {
    let component: ReleaseNotesComponent;
    let fixture: ComponentFixture<ReleaseNotesComponent>;
    let releaseNotesService: jasmine.SpyObj<ReleaseNotesService>;

    beforeEach(async () => {
        releaseNotesService = jasmine.createSpyObj("ReleaseNotesService", [
            "getLatestReleaseNotes",
            "markAsSeen",
        ]);
        releaseNotesService.getLatestReleaseNotes.and.returnValue(of(null));

        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot(), ReleaseNotesComponent],
            providers: [{ provide: ReleaseNotesService, useValue: releaseNotesService }],
        }).compileComponents();

        fixture = TestBed.createComponent(ReleaseNotesComponent);
        component = fixture.componentInstance;
        await fixture.whenStable();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
