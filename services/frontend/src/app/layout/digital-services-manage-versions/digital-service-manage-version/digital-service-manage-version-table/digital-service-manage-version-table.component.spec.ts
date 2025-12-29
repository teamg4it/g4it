import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { of } from "rxjs";
import { DigitalServiceVersionResponse } from "src/app/core/interfaces/digital-service-version.interface";
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
            imports: [TranslateModule.forRoot(), HttpClientTestingModule],
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
                TranslateService,
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
});
