import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { of } from "rxjs";
import { AdministrationService } from "src/app/core/service/business/administration.service";

import { ReleaseNotesComponent } from "./release-notes.component";

describe("ReleaseNotesComponent", () => {
    let component: ReleaseNotesComponent;
    let fixture: ComponentFixture<ReleaseNotesComponent>;
    let administrationService: jasmine.SpyObj<AdministrationService>;

    beforeEach(async () => {
        administrationService = jasmine.createSpyObj("AdministrationService", [
            "getLatestReleaseNotes",
            "saveReleaseNotes",
        ]);
        administrationService.getLatestReleaseNotes.and.returnValue(of({ content: "" }));

        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot(), ReleaseNotesComponent],
            providers: [
                { provide: AdministrationService, useValue: administrationService },
                MessageService,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ReleaseNotesComponent);
        component = fixture.componentInstance;
        await fixture.whenStable();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
