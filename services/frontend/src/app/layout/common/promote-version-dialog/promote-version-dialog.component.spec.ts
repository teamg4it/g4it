import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule } from "@ngx-translate/core";
import { of } from "rxjs";
import { DigitalServiceVersionDataService } from "src/app/core/service/data/digital-service-version-data-service";
import { PromoteVersionDialogComponent } from "./promote-version-dialog.component";

describe("PromoteVersionDialogComponent", () => {
    let component: PromoteVersionDialogComponent;
    let fixture: ComponentFixture<PromoteVersionDialogComponent>;
    let serviceSpy: jasmine.SpyObj<DigitalServiceVersionDataService>;

    beforeEach(async () => {
        serviceSpy = jasmine.createSpyObj("DigitalServiceVersionDataService", [
            "promoteVersion",
        ]);

        await TestBed.configureTestingModule({
            declarations: [PromoteVersionDialogComponent],
            providers: [
                { provide: DigitalServiceVersionDataService, useValue: serviceSpy },
            ],
            imports: [TranslateModule.forRoot()],
        }).compileComponents();

        fixture = TestBed.createComponent(PromoteVersionDialogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should emit outClose when closePopup is called", () => {
        spyOn(component.outClose, "emit");

        component.closePopup();

        expect(component.outClose.emit).toHaveBeenCalled();
    });

    it("should not call promoteVersion service if dsvId is empty", () => {
        fixture.componentRef.setInput("dsvId", "");

        component.promoteVersion();

        expect(serviceSpy.promoteVersion).not.toHaveBeenCalled();
    });

    it("should call promoteVersion service when dsvId is present", () => {
        const dsvId = "123";
        fixture.componentRef.setInput("dsvId", dsvId);
        serviceSpy.promoteVersion.and.returnValue(
            of({
                digitalServiceUid: "1",
                digitalServiceVersionUid: "2",
                isPromoted: false,
            }),
        );

        component.promoteVersion();

        expect(serviceSpy.promoteVersion).toHaveBeenCalledWith(dsvId);
    });

    it("should emit promotedEvent when response is promoted", () => {
        const dsvId = "123";
        fixture.componentRef.setInput("dsvId", dsvId);
        serviceSpy.promoteVersion.and.returnValue(
            of({
                digitalServiceUid: "1",
                digitalServiceVersionUid: "2",
                isPromoted: true,
            }),
        );

        spyOn(component.promotedEvent, "emit");
        spyOn(component, "closePopup");

        component.promoteVersion();

        expect(component.promotedEvent.emit).toHaveBeenCalledWith(true);
        expect(component.closePopup).toHaveBeenCalled();
    });

    it("should close popup even if not promoted", () => {
        const dsvId = "123";
        fixture.componentRef.setInput("dsvId", dsvId);
        serviceSpy.promoteVersion.and.returnValue(
            of({
                digitalServiceUid: "1",
                digitalServiceVersionUid: "2",
                isPromoted: false,
            }),
        );

        spyOn(component, "closePopup");

        component.promoteVersion();

        expect(component.closePopup).toHaveBeenCalled();
    });
});
