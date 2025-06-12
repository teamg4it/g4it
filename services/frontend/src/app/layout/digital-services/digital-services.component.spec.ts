import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import { of, Subject } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { Role } from "src/app/core/interfaces/roles.interfaces";
import { Organization } from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { DigitalServicesComponent } from "./digital-services.component";

describe("DigitalServicesComponent", () => {
    let component: DigitalServicesComponent;
    let fixture: ComponentFixture<DigitalServicesComponent>;
    let mockRouter: any;
    let mockRoute: any;
    let mockDigitalServicesData: any;
    let mockUserService: any;
    let mockGlobalStore: any;

    beforeEach(async () => {
        mockRouter = {
            navigate: jasmine.createSpy("navigate"),
            events: new Subject(),
        };
        mockRoute = {};
        mockDigitalServicesData = {
            list: jasmine
                .createSpy("list")
                .and.returnValue(of([{ uid: "1", name: "Service A" }])),
            create: jasmine.createSpy("create").and.returnValue(of({ uid: "2" })),
            delete: jasmine.createSpy("delete").and.returnValue(of(null)),
            get: jasmine
                .createSpy("get")
                .and.returnValue(of({ uid: "1", note: { content: "Note" } })),
            update: jasmine.createSpy("update").and.returnValue(of(null)),
        };
        mockUserService = {
            currentOrganization$: of({ name: "Org1" } as Organization),
            roles$: of([Role.DigitalServiceRead]),
        };
        mockGlobalStore = {
            setLoading: jasmine.createSpy("setLoading"),
        };

        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot()],
            declarations: [DigitalServicesComponent],
            providers: [
                { provide: Router, useValue: mockRouter },
                { provide: ActivatedRoute, useValue: mockRoute },
                {
                    provide: DigitalServicesDataService,
                    useValue: mockDigitalServicesData,
                },
                { provide: UserService, useValue: mockUserService },
                { provide: GlobalStoreService, useValue: mockGlobalStore },
                TranslateService,
                MessageService,
                ConfirmationService,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize and retrieve digital services", async () => {
        spyOn(component, "retrieveDigitalServices").and.callThrough();
        await component.ngOnInit();
        expect(component.retrieveDigitalServices).toHaveBeenCalled();
        expect(component.allDigitalServices.length).toBe(1);
        expect(component.allDigitalServices[0].name).toBe("Service A");
    });

    it("should update paginated items on page change", () => {
        component["allDigitalServices"] = Array.from(
            { length: 20 },
            (_, i) =>
                ({
                    uid: `${i}`,
                    name: `Service ${i}`,
                }) as DigitalService,
        );
        component.onPageChange({ first: 0, rows: 10, page: 1 } as any);
        expect(component.paginatedDigitalServices.length).toBe(10);
        expect(component.paginatedDigitalServices[0].name).toBe("Service 10");
    });

    it("should navigate to digital service footprint", () => {
        const uid = "123";
        component.goToDigitalServiceFootprint(uid);
        expect(mockRouter.navigate).toHaveBeenCalledWith([`${uid}/footprint/terminals`], {
            relativeTo: mockRoute,
        });
    });

    it("should create a new digital service and navigate to its footprint", async () => {
        await component.createNewDigitalService();
        expect(mockDigitalServicesData.create).toHaveBeenCalled();
        expect(mockRouter.navigate).toHaveBeenCalledWith(["2/footprint/terminals"], {
            relativeTo: mockRoute,
        });
    });

    it("should delete a digital service and refresh the list", () => {
        spyOn(component, "retrieveDigitalServices");
        component.itemDelete("1");
        expect(mockGlobalStore.setLoading).toHaveBeenCalledWith(true);
        expect(mockDigitalServicesData.delete).toHaveBeenCalledWith("1");
        expect(component.retrieveDigitalServices).toHaveBeenCalled();
    });

    it("should save a note for a digital service", () => {
        const noteContent = "Updated Note";
        component.selectedDigitalService = { uid: "1" } as any;
        component.noteSaveValue(noteContent);
        expect(mockDigitalServicesData.get).toHaveBeenCalledWith("1");
        expect(mockDigitalServicesData.update).toHaveBeenCalledWith(
            jasmine.objectContaining({ note: { content: noteContent } }),
        );
    });

    it("should delete a note for a digital service", () => {
        component.selectedDigitalService = { uid: "1" } as any;
        component.noteDelete();
        expect(mockDigitalServicesData.get).toHaveBeenCalledWith("1");
        expect(mockDigitalServicesData.update).toHaveBeenCalledWith(
            jasmine.objectContaining({ note: undefined }),
        );
    });
});
