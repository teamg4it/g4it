import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ReactiveFormsModule } from "@angular/forms";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { CreateDigitalServicesSidebarComponent } from "./create-digital-services-sidebar.component";

describe("CreateDigitalServicesSidebarComponent", () => {
    let component: CreateDigitalServicesSidebarComponent;
    let fixture: ComponentFixture<CreateDigitalServicesSidebarComponent>;
    let digitalServiceBusinessService: jasmine.SpyObj<DigitalServiceBusinessService>;
    let translateService: jasmine.SpyObj<TranslateService>;

    const mockDigitalServices: DigitalService[] = [
        {
            uid: "1",
            name: "Digital Service 1",
            creationDate: 1234567890,
            lastUpdateDate: 1234567890,
            lastCalculationDate: null,
            terminals: [],
            servers: [],
            networks: [],
            enableDataInconsistency: false,
            activeDsvUid: "dsv1",
        },
        {
            uid: "2",
            name: "Digital Service 2",
            creationDate: 1234567890,
            lastUpdateDate: 1234567890,
            lastCalculationDate: null,
            terminals: [],
            servers: [],
            networks: [],
            enableDataInconsistency: false,
            activeDsvUid: "dsv2",
        },
    ];

    beforeEach(async () => {
        const digitalServiceBusinessServiceSpy = jasmine.createSpyObj(
            "DigitalServiceBusinessService",
            ["getNextAvailableName"],
        );
        const translateServiceSpy = jasmine.createSpyObj("TranslateService", ["instant"]);

        await TestBed.configureTestingModule({
    imports: [ReactiveFormsModule, TranslateModule.forRoot(), CreateDigitalServicesSidebarComponent],
    providers: [
        {
            provide: DigitalServiceBusinessService,
            useValue: digitalServiceBusinessServiceSpy,
        },
        { provide: TranslateService, useValue: translateServiceSpy },
    ],
}).compileComponents();

        fixture = TestBed.createComponent(CreateDigitalServicesSidebarComponent);
        component = fixture.componentInstance;
        digitalServiceBusinessService = TestBed.inject(
            DigitalServiceBusinessService,
        ) as jasmine.SpyObj<DigitalServiceBusinessService>;
        translateService = TestBed.inject(
            TranslateService,
        ) as jasmine.SpyObj<TranslateService>;

        // Setup default translate mocks
        translateService.instant.and.returnValue("translated-text");
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    describe("ngOnInit", () => {
        it("should initialize createForm with default values", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 3",
            );
            fixture.componentRef.setInput("allDigitalServices", mockDigitalServices);
            fixture.componentRef.setInput("isEcoMindAi", false);

            component.ngOnInit();

            expect(component.createForm).toBeDefined();
            expect(component.createForm.get("dsName")?.value).toBe("Digital Service 3");
            expect(component.createForm.get("dsVersionName")?.value).toBe("Version 1");
        });

        it("should append ' AI' to dsName when isEcoMindAi is true", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 3",
            );
            fixture.componentRef.setInput("allDigitalServices", mockDigitalServices);
            fixture.componentRef.setInput("isEcoMindAi", true);

            component.ngOnInit();

            expect(component.createForm.get("dsName")?.value).toBe(
                "Digital Service 3 AI",
            );
        });

        it("should remove ' AI' from existing names when isEcoMindAi is true", () => {
            const mockAiServices: DigitalService[] = [
                {
                    uid: "1",
                    name: "AI Service 1 AI",
                    creationDate: 1234567890,
                    lastUpdateDate: 1234567890,
                    lastCalculationDate: null,
                    terminals: [],
                    servers: [],
                    networks: [],
                    enableDataInconsistency: false,
                    activeDsvUid: "dsv1",
                    isAi: true,
                },
            ];
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "AI Service 2",
            );
            fixture.componentRef.setInput("allDigitalServices", mockAiServices);
            fixture.componentRef.setInput("isEcoMindAi", true);

            component.ngOnInit();

            expect(
                digitalServiceBusinessService.getNextAvailableName,
            ).toHaveBeenCalledWith(["AI Service 1"], "Digital Service", true, false);
        });

        it("should not modify existing names when isEcoMindAi is false", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 3",
            );
            fixture.componentRef.setInput("allDigitalServices", mockDigitalServices);
            fixture.componentRef.setInput("isEcoMindAi", false);

            component.ngOnInit();

            expect(
                digitalServiceBusinessService.getNextAvailableName,
            ).toHaveBeenCalledWith(
                ["Digital Service 1", "Digital Service 2"],
                "Digital Service",
                true,
                false,
            );
        });

        it("should set required validators on dsName", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 1",
            );
            fixture.componentRef.setInput("allDigitalServices", []);
            fixture.componentRef.setInput("isEcoMindAi", false);

            component.ngOnInit();

            const dsNameControl = component.createForm.get("dsName");
            expect(dsNameControl?.hasError("required")).toBeFalsy();

            dsNameControl?.setValue("");
            expect(dsNameControl?.hasError("required")).toBeTruthy();
        });

        it("should validate unique name", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 3",
            );
            fixture.componentRef.setInput("allDigitalServices", mockDigitalServices);
            fixture.componentRef.setInput("isEcoMindAi", false);

            component.ngOnInit();

            const dsNameControl = component.createForm.get("dsName");
            dsNameControl?.setValue("Digital Service 1");
            expect(dsNameControl?.hasError("uniqueName")).toBeTruthy();
        });

        it("should validate whitespace in name", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 1",
            );
            fixture.componentRef.setInput("allDigitalServices", []);
            fixture.componentRef.setInput("isEcoMindAi", false);

            component.ngOnInit();

            const dsNameControl = component.createForm.get("dsName");
            dsNameControl?.setValue("   ");
            expect(dsNameControl?.hasError("required")).toBeTruthy();
        });

        it("should set required validator on dsVersionName", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 1",
            );
            fixture.componentRef.setInput("allDigitalServices", []);
            fixture.componentRef.setInput("isEcoMindAi", false);

            component.ngOnInit();

            const versionControl = component.createForm.get("dsVersionName");
            expect(versionControl?.hasError("required")).toBeFalsy();

            versionControl?.setValue("");
            expect(versionControl?.hasError("required")).toBeTruthy();
        });

        it("should handle empty allDigitalServices array", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 1",
            );
            fixture.componentRef.setInput("allDigitalServices", []);
            fixture.componentRef.setInput("isEcoMindAi", false);

            component.ngOnInit();

            expect(component.createForm.get("dsName")?.value).toBe("Digital Service 1");
            expect(
                digitalServiceBusinessService.getNextAvailableName,
            ).toHaveBeenCalledWith([], "Digital Service", true, false);
        });
    });

    describe("createDS", () => {
        beforeEach(() => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 1",
            );
            fixture.componentRef.setInput("allDigitalServices", []);
            fixture.componentRef.setInput("isEcoMindAi", false);
            component.ngOnInit();
        });

        it("should emit submitCreateDsForm event with form values when form is valid", () => {
            spyOn(component.submitCreateDsForm, "emit");

            component.createForm.patchValue({
                dsName: "New Digital Service",
                dsVersionName: "Version 1",
            });

            component.createDS();

            expect(component.submitCreateDsForm.emit).toHaveBeenCalledWith({
                dsName: "New Digital Service",
                versionName: "Version 1",
            });
        });

        it("should not emit submitCreateDsForm event when form is invalid", () => {
            spyOn(component.submitCreateDsForm, "emit");

            component.createForm.patchValue({
                dsName: "",
                dsVersionName: "Version 1",
            });

            component.createDS();

            expect(component.submitCreateDsForm.emit).not.toHaveBeenCalled();
        });

        it("should not emit when dsName is whitespace only", () => {
            spyOn(component.submitCreateDsForm, "emit");

            component.createForm.patchValue({
                dsName: "   ",
                dsVersionName: "Version 1",
            });

            component.createDS();

            expect(component.submitCreateDsForm.emit).not.toHaveBeenCalled();
        });

        it("should not emit when dsVersionName is empty", () => {
            spyOn(component.submitCreateDsForm, "emit");

            component.createForm.patchValue({
                dsName: "New Digital Service",
                dsVersionName: "",
            });

            component.createDS();

            expect(component.submitCreateDsForm.emit).not.toHaveBeenCalled();
        });

        it("should emit with AI suffix when isEcoMindAi is true", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "AI Service 1",
            );
            fixture.componentRef.setInput("isEcoMindAi", true);
            component.ngOnInit();
            spyOn(component.submitCreateDsForm, "emit");

            component.createForm.patchValue({
                dsName: "AI Service 1 AI",
                dsVersionName: "Version 1",
            });

            component.createDS();

            expect(component.submitCreateDsForm.emit).toHaveBeenCalledWith({
                dsName: "AI Service 1 AI",
                versionName: "Version 1",
            });
        });

        it("should emit with custom version name", () => {
            spyOn(component.submitCreateDsForm, "emit");

            component.createForm.patchValue({
                dsName: "Digital Service",
                dsVersionName: "Custom Version",
            });

            component.createDS();

            expect(component.submitCreateDsForm.emit).toHaveBeenCalledWith({
                dsName: "Digital Service",
                versionName: "Custom Version",
            });
        });
    });

    describe("closeSidebar", () => {
        beforeEach(() => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 1",
            );
            fixture.componentRef.setInput("allDigitalServices", []);
            fixture.componentRef.setInput("isEcoMindAi", false);
            component.ngOnInit();
        });

        it("should reset the form", () => {
            component.createForm.patchValue({
                dsName: "Modified Name",
                dsVersionName: "Modified Version",
            });

            component.closeSidebar();

            expect(component.createForm.get("dsName")?.value).toBeNull();
            expect(component.createForm.get("dsVersionName")?.value).toBeNull();
        });

        it("should emit sidebarVisibleChange event with false", () => {
            spyOn(component.sidebarVisibleChange, "emit");

            component.closeSidebar();

            expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
        });

        it("should reset form and emit event in sequence", () => {
            spyOn(component.sidebarVisibleChange, "emit");
            component.createForm.patchValue({
                dsName: "Test Name",
                dsVersionName: "Test Version",
            });

            component.closeSidebar();

            expect(component.createForm.get("dsName")?.value).toBeNull();
            expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
        });
    });

    describe("Form Validation Integration", () => {
        it("should mark form as invalid when both fields are empty", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 1",
            );
            fixture.componentRef.setInput("allDigitalServices", []);
            fixture.componentRef.setInput("isEcoMindAi", false);
            component.ngOnInit();

            component.createForm.patchValue({
                dsName: "",
                dsVersionName: "",
            });

            expect(component.createForm.valid).toBeFalsy();
        });

        it("should mark form as valid when all required fields are filled correctly", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 1",
            );
            fixture.componentRef.setInput("allDigitalServices", []);
            fixture.componentRef.setInput("isEcoMindAi", false);
            component.ngOnInit();

            component.createForm.patchValue({
                dsName: "Valid Name",
                dsVersionName: "Valid Version",
            });

            expect(component.createForm.valid).toBeTruthy();
        });

        it("should ignore spaces when validating unique names", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 3",
            );
            fixture.componentRef.setInput("allDigitalServices", mockDigitalServices);
            fixture.componentRef.setInput("isEcoMindAi", false);
            component.ngOnInit();

            const dsNameControl = component.createForm.get("dsName");
            dsNameControl?.setValue("DigitalService1");
            expect(dsNameControl?.hasError("uniqueName")).toBeTruthy();

            dsNameControl?.setValue("Digital Service1");
            expect(dsNameControl?.hasError("uniqueName")).toBeTruthy();
        });

        it("should allow names that are unique after removing spaces", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 3",
            );
            fixture.componentRef.setInput("allDigitalServices", mockDigitalServices);
            fixture.componentRef.setInput("isEcoMindAi", false);
            component.ngOnInit();

            const dsNameControl = component.createForm.get("dsName");
            dsNameControl?.setValue("Digital Service 3");
            expect(dsNameControl?.hasError("uniqueName")).toBeFalsy();
        });
    });

    describe("Input Signal Changes", () => {
        it("should reinitialize form when allDigitalServices input changes", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 1",
            );
            fixture.componentRef.setInput("allDigitalServices", []);
            fixture.componentRef.setInput("isEcoMindAi", false);
            component.ngOnInit();

            const initialValue = component.createForm.get("dsName")?.value;

            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 3",
            );
            fixture.componentRef.setInput("allDigitalServices", mockDigitalServices);
            component.ngOnInit();

            expect(component.createForm.get("dsName")?.value).not.toBe(initialValue);
        });

        it("should handle isEcoMindAi toggle correctly", () => {
            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 1",
            );
            fixture.componentRef.setInput("allDigitalServices", []);
            fixture.componentRef.setInput("isEcoMindAi", false);
            component.ngOnInit();

            expect(component.createForm.get("dsName")?.value).toBe("Digital Service 1");

            digitalServiceBusinessService.getNextAvailableName.and.returnValue(
                "Digital Service 1",
            );
            fixture.componentRef.setInput("isEcoMindAi", true);
            component.ngOnInit();

            expect(component.createForm.get("dsName")?.value).toBe(
                "Digital Service 1 AI",
            );
        });
    });
});
