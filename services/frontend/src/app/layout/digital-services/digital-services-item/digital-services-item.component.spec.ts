import { HttpClientTestingModule } from "@angular/common/http/testing";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { of } from "rxjs";
import { UserService } from "src/app/core/service/business/user.service";
import { UserDataService } from "src/app/core/service/data/user-data.service";
import { DigitalServicesItemComponent } from "./digital-services-item.component";

describe("DigitalServicesItemComponent", () => {
    let component: DigitalServicesItemComponent;
    let fixture: ComponentFixture<DigitalServicesItemComponent>;

    let routerSpy = { navigate: jasmine.createSpy("navigate") };
    let translateServiceStub = {
        instant: (key: string) => key,
    };

    let confirmationServiceStub = {
        confirm: jasmine.createSpy("confirm"),
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DigitalServicesItemComponent],
            imports: [HttpClientTestingModule],
            providers: [
                UserService,
                UserDataService,
                MessageService,
                { provide: Router, useValue: routerSpy },
                { provide: TranslateService, useValue: translateServiceStub },
                { provide: ConfirmationService, useValue: confirmationServiceStub },
                { provide: ActivatedRoute, useValue: { snapshot: {}, params: of({}) } },
            ],
            schemas: [NO_ERRORS_SCHEMA], // Ignore unknown elements/attributes in template
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DigitalServicesItemComponent);
        component = fixture.componentInstance;
        component.digitalService = { uid: "123", name: "Test Service" } as any;
        fixture.detectChanges();
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should set firstFootprintTab based on isAi flag (false)", async () => {
        component.isAi = false;
        await component.ngOnInit();
        expect(component.firstFootprintTab).toBe("resources");
    });

    it("should set firstFootprintTab based on isAi flag (true)", async () => {
        component.isAi = true;
        await component.ngOnInit();
        expect(component.firstFootprintTab).toBe("infrastructure");
    });

    it("should navigate to footprint page", () => {
        component.goToDigitalServiceFootprint("abc123");
        expect(routerSpy.navigate).toHaveBeenCalledWith(["abc123/footprint/resources"], {
            relativeTo: TestBed.inject(ActivatedRoute),
        });
    });

    it("should emit noteOpened when openNote is called", () => {
        spyOn(component.noteOpened, "emit");
        component.openNote();
        expect(component.noteOpened.emit).toHaveBeenCalledWith(component.digitalService);
    });

    it("should set firstFootprintTab to 'resources' in constructor when isAi is false", () => {
        component.isAi = false;
        // Recreate component to trigger constructor logic
        fixture = TestBed.createComponent(DigitalServicesItemComponent);
        component = fixture.componentInstance;
        expect(component.firstFootprintTab).toBe("resources");
    });

    it("should set firstFootprintTab to 'infrastructure' in constructor when isAi is false", () => {
        component.isAi = true;
        // Recreate component to trigger constructor logic
        fixture = TestBed.createComponent(DigitalServicesItemComponent);
        component = fixture.componentInstance;
        expect(component.firstFootprintTab).toBe("infrastructure");
    });
});
