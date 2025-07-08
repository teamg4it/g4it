import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule } from "@ngx-translate/core";
import { of } from "rxjs";
import { LoadingDataService } from "src/app/core/service/data/loading-data.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { MultiFileImportComponent } from "./multi-file-import.component";

describe("MultiFileImportComponent", () => {
    let component: MultiFileImportComponent;
    let fixture: ComponentFixture<MultiFileImportComponent>;

    const mockLoadingService = {
        launchLoadInputFiles: jasmine.createSpy().and.returnValue(of(true)),
    };

    const mockActivatedRoute = {
        snapshot: {
            paramMap: {
                get: () => "mock-ds-id",
            },
        },
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [MultiFileImportComponent],
            imports: [
                HttpClientTestingModule,
                RouterTestingModule,
                FormsModule,
                SharedModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                { provide: LoadingDataService, useValue: mockLoadingService },
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(MultiFileImportComponent);
        component = fixture.componentInstance;

        component.importForm = {
            get: jasmine.createSpy().and.returnValue({
                setValue: jasmine.createSpy(),
            }),
        };

        component.form = [{ name: "nonCloud" }, { name: "cloud" }];
        component.selectedMenuIndex = 0;

        fixture.detectChanges();
    });

    it("should create component", () => {
        expect(component).toBeTruthy();
    });
});
