import { HttpClientTestingModule } from "@angular/common/http/testing";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { ConfirmationService } from "primeng/api";
import { of } from "rxjs";
import { DigitalServiceVersionResponse } from "src/app/core/interfaces/digital-service-version.interface";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServiceVersionDataService } from "src/app/core/service/data/digital-service-version-data-service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { DigitalServiceManageVersionTableComponent } from "./digital-service-manage-version-table.component";

describe("DigitalServiceManageVersionTableComponent", () => {
    let component: DigitalServiceManageVersionTableComponent;
    let fixture: ComponentFixture<DigitalServiceManageVersionTableComponent>;
    let dataServiceSpy: jasmine.SpyObj<DigitalServiceVersionDataService>;
    let digitalServicesDataServiceSpy: jasmine.SpyObj<DigitalServicesDataService>;
    let userServiceSpy: jasmine.SpyObj<UserService>;
    let routerSpy: jasmine.SpyObj<Router>;
    let confirmationServiceSpy: jasmine.SpyObj<ConfirmationService>;
    let globalStoreSpy: jasmine.SpyObj<GlobalStoreService>;
    let translateSpy: jasmine.SpyObj<TranslateService>;
    let activatedRoute: any;

    const mockVersions: DigitalServiceVersionResponse[] = [
        {
            digitalServiceUid: "ds-1",
            digitalServiceVersionUid: "version-1",
            versionName: "Version 1",
            versionType: "active",
            creationDate: "2024-01-01",
            lastCalculationDate: "2024-01-02",
        } as DigitalServiceVersionResponse,
        {
            digitalServiceUid: "ds-1",
            digitalServiceVersionUid: "version-2",
            versionName: "Version 2",
            versionType: "draft",
            creationDate: "2024-01-03",
        } as DigitalServiceVersionResponse,
        {
            digitalServiceUid: "ds-1",
            digitalServiceVersionUid: "version-3",
            versionName: "Version 3",
            versionType: "archived",
            creationDate: "2024-01-04",
        } as DigitalServiceVersionResponse,
    ];

    beforeEach(async () => {
        dataServiceSpy = jasmine.createSpyObj("DigitalServiceVersionDataService", [
            "getVersions",
            "duplicateVersion",
        ]);

        digitalServicesDataServiceSpy = jasmine.createSpyObj(
            "DigitalServicesDataService",
            ["deleteVersion"],
        );

        userServiceSpy = jasmine.createSpyObj("UserService", [
            "isAllowedDigitalServiceWrite$",
        ]);

        routerSpy = jasmine.createSpyObj("Router", ["navigate"]);
        Object.defineProperty(routerSpy, "url", {
            writable: true,
            value: "/subscribers/1/organizations/2/digital-services/3/digital-service-version/version-1/manage-versions",
        });

        confirmationServiceSpy = jasmine.createSpyObj("ConfirmationService", ["confirm"]);

        globalStoreSpy = jasmine.createSpyObj("GlobalStoreService", ["setLoading"]);

        translateSpy = jasmine.createSpyObj("TranslateService", ["instant"]);
        translateSpy.instant.and.returnValue("Translated text");

        activatedRoute = {
            snapshot: {
                paramMap: {
                    get: jasmine.createSpy("get").and.callFake((key: string) => {
                        if (key === "digitalServiceVersionId") return "version-1";
                        return null;
                    }),
                },
            },
            parent: {
                parent: {},
            },
        };

        await TestBed.configureTestingModule({
            declarations: [DigitalServiceManageVersionTableComponent],
            imports: [TranslateModule.forRoot(), HttpClientTestingModule],
            schemas: [NO_ERRORS_SCHEMA],
            providers: [
                { provide: DigitalServiceVersionDataService, useValue: dataServiceSpy },
                {
                    provide: DigitalServicesDataService,
                    useValue: digitalServicesDataServiceSpy,
                },
                { provide: Router, useValue: routerSpy },
                { provide: ActivatedRoute, useValue: activatedRoute },
                { provide: TranslateService, useValue: translateSpy },
                { provide: UserService, useValue: userServiceSpy },
                { provide: ConfirmationService, useValue: confirmationServiceSpy },
                { provide: GlobalStoreService, useValue: globalStoreSpy },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServiceManageVersionTableComponent);
        component = fixture.componentInstance;
        dataServiceSpy.getVersions.and.returnValue(of(mockVersions));
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    describe("ngOnInit", () => {
        it("should set dsVersionUid from route params", () => {
            fixture.detectChanges();

            expect(component.dsVersionUid).toBe("version-1");
        });

        it("should call getVersions on init", () => {
            fixture.detectChanges();

            expect(dataServiceSpy.getVersions).toHaveBeenCalledWith("version-1");
        });
    });

    describe("getVersions", () => {
        it("should fetch versions using dsVersionUid when versionData is empty", () => {
            component.versionData = [];
            component.dsVersionUid = "version-1";

            component.getVersions();

            expect(dataServiceSpy.getVersions).toHaveBeenCalledWith("version-1");
            expect(component.versionData).toEqual(mockVersions);
        });

        it("should fetch versions using active version uid when versionData exists", () => {
            component.versionData = mockVersions;
            dataServiceSpy.getVersions.and.returnValue(of(mockVersions));

            component.getVersions();

            expect(dataServiceSpy.getVersions).toHaveBeenCalledWith("version-1");
            expect(component.versionData).toEqual(mockVersions);
        });
    });

    describe("redirectToVersionDetails", () => {
        it("should navigate to digital-service-version dashboard when calculationDone is true and not eco-mind-ai", () => {
            Object.defineProperty(routerSpy, "url", {
                writable: true,
                value: "/subscribers/1/organizations/2/digital-services/3/digital-service-version/version-1/manage-versions",
            });

            component.redirectToVersionDetails("version-123", true);

            expect(routerSpy.navigate).toHaveBeenCalledWith(
                ["digital-service-version", "version-123", "footprint", "dashboard"],
                { relativeTo: activatedRoute.parent?.parent },
            );
        });

        it("should navigate to digital-service-version resources when calculationDone is false and not eco-mind-ai", () => {
            component.redirectToVersionDetails("version-123", false);

            expect(routerSpy.navigate).toHaveBeenCalledWith(
                ["digital-service-version", "version-123", "footprint", "resources"],
                { relativeTo: activatedRoute.parent?.parent },
            );
        });

        it("should navigate to resources when calculationDone is undefined", () => {
            component.redirectToVersionDetails("version-123");

            expect(routerSpy.navigate).toHaveBeenCalledWith(
                ["digital-service-version", "version-123", "footprint", "resources"],
                { relativeTo: activatedRoute.parent?.parent },
            );
        });
    });

    describe("compareVersions", () => {
        it("should navigate to compare-versions page with query params when 2 versions are selected", () => {
            component.selectedVersions = ["version-1", "version-2"];

            component.compareVersions();

            expect(routerSpy.navigate).toHaveBeenCalledWith(["../compare-versions"], {
                relativeTo: activatedRoute,
                queryParams: {
                    version1: "version-1",
                    version2: "version-2",
                },
            });
        });

        it("should not navigate when less than 2 versions are selected", () => {
            component.selectedVersions = ["version-1"];

            component.compareVersions();

            expect(routerSpy.navigate).not.toHaveBeenCalled();
        });

        it("should not navigate when no versions are selected", () => {
            component.selectedVersions = [];

            component.compareVersions();

            expect(routerSpy.navigate).not.toHaveBeenCalled();
        });
    });

    describe("onVersionSelect", () => {
        it("should add version to selectedVersions when less than 2 are selected", () => {
            const version = { ...mockVersions[0], selected: false };
            component.selectedVersions = [];

            component.onVersionSelect(version);

            expect(component.selectedVersions).toContain("version-1");
            expect(version.selected).toBe(true);
        });

        it("should remove version from selectedVersions when already selected", () => {
            const version = { ...mockVersions[0], selected: true };
            component.selectedVersions = ["version-1"];

            component.onVersionSelect(version);

            expect(component.selectedVersions).not.toContain("version-1");
            expect(version.selected).toBe(false);
        });

        it("should not add version when 2 are already selected", () => {
            const version = { ...mockVersions[2], selected: false };
            component.selectedVersions = ["version-1", "version-2"];

            component.onVersionSelect(version);

            expect(component.selectedVersions).not.toContain("version-3");
            expect(version.selected).toBe(false);
            expect(component.selectedVersions.length).toBe(2);
        });

        it("should allow selecting second version", () => {
            const version = { ...mockVersions[1], selected: false };
            component.selectedVersions = ["version-1"];

            component.onVersionSelect(version);

            expect(component.selectedVersions).toContain("version-2");
            expect(version.selected).toBe(true);
            expect(component.selectedVersions.length).toBe(2);
        });
    });

    describe("duplicateDigitalServiceVersion", () => {
        it("should call duplicateVersion service and redirect to version details", () => {
            const duplicatedVersion = { uid: "new-version-uid" };
            dataServiceSpy.duplicateVersion.and.returnValue(of(duplicatedVersion as any));
            spyOn(component, "redirectToVersionDetails");

            component.duplicateDigitalServiceVersion("version-1");

            expect(dataServiceSpy.duplicateVersion).toHaveBeenCalledWith("version-1");
            expect(component.redirectToVersionDetails).toHaveBeenCalledWith(
                "new-version-uid",
                false,
            );
        });
    });
});
