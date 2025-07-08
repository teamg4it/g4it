import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { TranslateService } from "@ngx-translate/core";
import * as FileSaver from "file-saver";
import { MessageService } from "primeng/api";
import { of, throwError } from "rxjs";
import { Constants } from "src/constants";
import { FileDescription } from "../../interfaces/file-system.interfaces";
import { TemplateFileService } from "./template-file.service";

describe("TemplateFileService", () => {
    let service: TemplateFileService;
    let httpMock: HttpTestingController;
    let messageServiceSpy: jasmine.SpyObj<MessageService>;
    let translateSpy: jasmine.SpyObj<TranslateService>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                TemplateFileService,
                {
                    provide: TranslateService,
                    useValue: jasmine.createSpyObj("TranslateService", ["instant"]),
                },
                {
                    provide: MessageService,
                    useValue: jasmine.createSpyObj("MessageService", ["add"]),
                },
            ],
        });

        service = TestBed.inject(TemplateFileService);
        httpMock = TestBed.inject(HttpTestingController);
        translateSpy = TestBed.inject(
            TranslateService,
        ) as jasmine.SpyObj<TranslateService>;
        messageServiceSpy = TestBed.inject(
            MessageService,
        ) as jasmine.SpyObj<MessageService>;
    });

    afterEach(() => {
        httpMock.verify();
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    it("should fetch template files", () => {
        const dummyFiles: FileDescription[] = [
            { name: "file1.csv", type: "csv", metadata: { size: "1024" } },
        ];

        service.getTemplateFiles("module1").subscribe((files) => {
            expect(files.length).toBe(1);
            expect(files[0].name).toBe("file1.csv");
        });

        const req = httpMock.expectOne(
            `${Constants.ENDPOINTS.templateFiles}?module=module1`,
        );
        expect(req.request.method).toBe("GET");
        req.flush(dummyFiles);
    });

    it("should download .csv file with correct headers", () => {
        const fileName = "file.csv";
        service.downloadTemplateFile(fileName, "mod1").subscribe();

        const req = httpMock.expectOne(
            `${Constants.ENDPOINTS.templateFiles}/${fileName}?module=mod1`,
        );
        expect(req.request.method).toBe("GET");
        expect(req.request.headers.get("Accept")).toBe("text/csv");
        expect(req.request.responseType).toBe("blob");
        req.flush(new Blob());
    });

    it("should download .xlsx file with correct headers", () => {
        const fileName = "file.xlsx";
        service.downloadTemplateFile(fileName, "mod1").subscribe();

        const req = httpMock.expectOne(
            `${Constants.ENDPOINTS.templateFiles}/${fileName}?module=mod1`,
        );
        expect(req.request.headers.get("Accept")).toBe("application/vnd.ms-excel");
        expect(req.request.responseType).toBe("blob");
        req.flush(new Blob());
    });

    it("should download .zip file with correct headers", () => {
        const fileName = "file.zip";
        service.downloadTemplateFile(fileName, "mod1").subscribe();

        const req = httpMock.expectOne(
            `${Constants.ENDPOINTS.templateFiles}/${fileName}?module=mod1`,
        );
        expect(req.request.headers.get("Accept")).toBe("application/zip");
        req.flush(new Blob());
    });

    it("should convert bytes to KB correctly", () => {
        expect(service.toKB("1024")).toBe("1.00");
        expect(service.toKB("2048")).toBe("2.00");
        expect(service.toKB(undefined)).toBe(0);
    });

    it("should handle successful getdownloadTemplateFile", async () => {
        const blob = new Blob(["test content"]);
        spyOn(service, "downloadTemplateFile").and.returnValue(of(blob));
        spyOn(FileSaver, "saveAs");

        await service.getdownloadTemplateFile("test.csv", "mod1");

        expect(FileSaver.saveAs).toHaveBeenCalledWith(blob, "test.csv");
    });

    it("should handle failed getdownloadTemplateFile", async () => {
        spyOn(service, "downloadTemplateFile").and.returnValue(
            throwError(() => new Error("fail")),
        );
        translateSpy.instant.and.returnValue("File not available");

        await service.getdownloadTemplateFile("fail.csv", "mod1");

        expect(messageServiceSpy.add).toHaveBeenCalledWith({
            severity: "error",
            summary: "File not available",
        });
    });

    it("should transform template files correctly", () => {
        translateSpy.instant.and.returnValue("translated-label");

        const files: FileDescription[] = [
            { name: "DATACENTER.csv", type: "csv", metadata: { size: "2048" } },
            { name: "file.zip", type: "zip", metadata: { size: "4096" } },
            { name: "model.xlsx", type: "xlsx", metadata: { size: "1024" } },
        ];

        const result = service.transformTemplateFiles(files, false);

        expect(result.length).toBe(3);
        expect(result[0].type).toBe("zip");
        expect(result[1].type).toBe("csv");
        expect(result[2].type).toBe("xlsx");
    });
});
