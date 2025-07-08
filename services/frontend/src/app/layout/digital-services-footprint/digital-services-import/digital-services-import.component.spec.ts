import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule } from "@ngx-translate/core";
import { of } from "rxjs";
import { FileSystemBusinessService } from "src/app/core/service/business/file-system.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { TemplateFileService } from "src/app/core/service/data/template-file.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { DigitalServicesImportComponent } from "./digital-services-import.component";

const mockTemplateFileService = {
    getTemplateFiles: jasmine.createSpy().and.returnValue(of([])),
    transformTemplateFiles: jasmine.createSpy().and.callFake((files: any) => files),
    getdownloadTemplateFile: jasmine.createSpy(),
};

const mockDigitalServicesData = {
    getDsTasks: jasmine.createSpy().and.returnValue(of({ tasks: [] })),
};

const mockActivatedRoute = {
    snapshot: {
        paramMap: new Map([["digitalServiceId", "123"]]),
    },
};

const mockFileSystemBusinessService = {
    downloadFile: jasmine.createSpy(),
    getTaskDetail: jasmine.createSpy(),
};

const mockUserService = {
    currentSubscriber$: of({ name: "Subscriber A" }),
    currentOrganization$: of({ name: "Org A" }),
};

describe("DigitalServicesImportComponent", () => {
    let component: DigitalServicesImportComponent;
    let fixture: ComponentFixture<DigitalServicesImportComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DigitalServicesImportComponent],
            imports: [
                HttpClientTestingModule,
                RouterTestingModule,
                FormsModule,
                SharedModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                { provide: TemplateFileService, useValue: mockTemplateFileService },
                {
                    provide: DigitalServicesDataService,
                    useValue: mockDigitalServicesData,
                },
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                {
                    provide: FileSystemBusinessService,
                    useValue: mockFileSystemBusinessService,
                },
                { provide: UserService, useValue: mockUserService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesImportComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create component", () => {
        expect(component).toBeTruthy();
    });
});
