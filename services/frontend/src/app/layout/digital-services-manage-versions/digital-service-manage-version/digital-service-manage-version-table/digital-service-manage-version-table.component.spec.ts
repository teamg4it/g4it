import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, Router } from "@angular/router";
import { of } from "rxjs";
import { DigitalServiceVersionResponse } from "src/app/core/interfaces/digital-service-version.interface";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceVersionDataService } from "src/app/core/service/data/digital-service-version-data-service";
import { DigitalServiceManageVersionTableComponent } from "./digital-service-manage-version-table.component";

describe("DigitalServiceManageVersionTableComponent", () => {
    let component: DigitalServiceManageVersionTableComponent;
    let fixture: ComponentFixture<DigitalServiceManageVersionTableComponent>;
    let dataServiceSpy: jasmine.SpyObj<DigitalServiceVersionDataService>;
    let routerSpy: jasmine.SpyObj<Router>;

    beforeEach(async () => {
        dataServiceSpy = jasmine.createSpyObj("DigitalServiceVersionDataService", [
            "getVersions",
            "duplicateVersion",
        ]);

        routerSpy = jasmine.createSpyObj("Router", ["navigate"]);

        await TestBed.configureTestingModule({
            declarations: [DigitalServiceManageVersionTableComponent],
            providers: [
                { provide: DigitalServiceVersionDataService, useValue: dataServiceSpy },
                { provide: Router, useValue: routerSpy },
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            paramMap: {
                                get: (key: string) =>
                                    key === "digitalServiceVersionId" ? "123" : null,
                            },
                        },
                        parent: {
                            parent: {}, // Needed for relativeTo
                        },
                    },
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServiceManageVersionTableComponent);
        component = fixture.componentInstance;
    });

    it("should fetch versions on init", () => {
        const mockVersions: DigitalServiceVersionResponse[] = [
            {
                digitalServiceUid: "1",
                digitalServiceVersionUid: "2",
                versionName: "Version 1",
                versionType: "Type 1",
            },
            {
                digitalServiceUid: "2",
                digitalServiceVersionUid: "3",
                versionName: "Version 2",
                versionType: "Type 2",
            },
        ];

        dataServiceSpy.getVersions.and.returnValue(of(mockVersions));

        fixture.detectChanges(); // triggers ngOnInit()

        expect(dataServiceSpy.getVersions).toHaveBeenCalledWith("123");
        expect(component.versionData).toEqual(mockVersions);
    });

    it("should navigate to version details", () => {
        component.redirectToVersionDetails("v1");

        expect(routerSpy.navigate).toHaveBeenCalledWith(
            ["digital-service-version", "v1", "footprint", "resources"],
            { relativeTo: TestBed.inject(ActivatedRoute).parent?.parent },
        );
    });

    it("should duplicate version and redirect with new uid", () => {
        const inputUid = "123";
        const returnedVersion = { uid: "999" } as DigitalService;

        // mock observable return
        dataServiceSpy.duplicateVersion.and.returnValue(of(returnedVersion));

        component.duplicateDigitalServiceVersion(inputUid);

        expect(dataServiceSpy.duplicateVersion).toHaveBeenCalledWith(inputUid);
    });
});
