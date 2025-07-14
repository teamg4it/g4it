import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormBuilder, ReactiveFormsModule } from "@angular/forms";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { of } from "rxjs";
import { LoadingDataService } from "src/app/core/service/data/loading-data.service";
import { TemplateFileService } from "src/app/core/service/data/template-file.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { FilePanelComponent } from "./file-panel.component";

describe("FilePanelComponent", () => {
    let component: FilePanelComponent;
    let fixture: ComponentFixture<FilePanelComponent>;
    let mockTemplateFileService: jasmine.SpyObj<TemplateFileService>;
    let mockLoadingService: jasmine.SpyObj<LoadingDataService>;
    let mockMessageService: jasmine.SpyObj<MessageService>;

    beforeEach(async () => {
        mockTemplateFileService = jasmine.createSpyObj("TemplateFileService", [
            "getTemplateFiles",
            "transformTemplateFiles",
            "getdownloadTemplateFile",
        ]);
        mockLoadingService = jasmine.createSpyObj("LoadingDataService", [
            "launchLoadInputFiles",
        ]);
        mockMessageService = jasmine.createSpyObj("MessageService", ["add"]);

        await TestBed.configureTestingModule({
            declarations: [FilePanelComponent],
            imports: [
                HttpClientTestingModule,
                RouterTestingModule,
                ReactiveFormsModule,
                SharedModule,
                TranslateModule.forRoot(),
            ],

            providers: [
                FormBuilder,
                TranslateService,
                { provide: TemplateFileService, useValue: mockTemplateFileService },
                { provide: LoadingDataService, useValue: mockLoadingService },
                { provide: MessageService, useValue: mockMessageService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(FilePanelComponent);
        component = fixture.componentInstance;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should initialize fileTypes and inventoriesForm on ngOnInit", async () => {
        mockTemplateFileService.getTemplateFiles.and.returnValue(of([]));
        await component.ngOnInit();
        expect(component.fileTypes.length).toBeGreaterThan(0);
        expect(component.inventoriesForm).toBeDefined();
    });

    it("should call getdownloadTemplateFile on downloadTemplateFile", () => {
        component.isTemplateParam = "is_template";
        component.downloadTemplateFile("template.csv");
        expect(mockTemplateFileService.getdownloadTemplateFile).toHaveBeenCalledWith(
            "template.csv",
            "is_template",
        );
    });
});
