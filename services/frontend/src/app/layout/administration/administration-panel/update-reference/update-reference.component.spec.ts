import { TestBed } from "@angular/core/testing";
import { MessageService } from "primeng/api";
import { of, throwError } from "rxjs";
import {
    CsvImportDataService,
    CsvImportEndpoint,
} from "src/app/core/service/data/api-route-referential.service";
import { UpdateReferenceComponent } from "./update-reference.component";

describe("UpdateReferenceComponent", () => {
    let component: UpdateReferenceComponent;
    let messageServiceMock: jasmine.SpyObj<MessageService>;
    let csvImportServiceMock: jasmine.SpyObj<CsvImportDataService>;

    const mockEndpoints: CsvImportEndpoint[] = [
        {
            name: "itemImpact",
            url: "/api/referential/itemImpact/csv",
            label: "Item Impact",
        },
        { name: "criterion", url: "/api/referential/criterion/csv", label: "Criterion" },
    ];

    const mockFile = new File(["test,data"], "test.csv", { type: "text/csv" });

    beforeEach(() => {
        messageServiceMock = jasmine.createSpyObj("MessageService", ["add"]);
        csvImportServiceMock = jasmine.createSpyObj("CsvImportDataService", [
            "getCsvEndpoints",
            "uploadCsvFile",
            "downloadCsvFile",
        ]);
        csvImportServiceMock.getCsvEndpoints.and.returnValue(mockEndpoints);

        TestBed.configureTestingModule({
            providers: [
                UpdateReferenceComponent,
                { provide: MessageService, useValue: messageServiceMock },
                { provide: CsvImportDataService, useValue: csvImportServiceMock },
            ],
        });

        component = TestBed.inject(UpdateReferenceComponent);
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    describe("ngOnInit", () => {
        it("should load csv endpoints on init", () => {
            component.ngOnInit();
            expect(csvImportServiceMock.getCsvEndpoints).toHaveBeenCalled();
            expect(component.csvEndpoints).toEqual(mockEndpoints);
        });
    });

    describe("clearFile", () => {
        it("should reset all upload state", () => {
            component.uploadedFile = mockFile;
            component.lastUploadResponse = { importedLineNumber: 10 };
            component.uploadErrors = ["error1"];
            component.importedLineNumber = 10;

            component.clearFile();

            expect(component.uploadedFile).toBeNull();
            expect(component.lastUploadResponse).toBeNull();
            expect(component.uploadErrors).toEqual([]);
            expect(component.importedLineNumber).toBe(0);
            expect(messageServiceMock.add).toHaveBeenCalledWith({
                severity: "info",
                summary: "File Removed",
                detail: "File has been removed. You can upload a new file.",
            });
        });
    });

    describe("onUpload", () => {
        it("should show warning if no endpoint is selected", () => {
            component.selectedEndpoint = null;

            component.onUpload({ files: [mockFile] });

            expect(messageServiceMock.add).toHaveBeenCalledWith({
                severity: "warn",
                summary: "Warning",
                detail: "Please select an endpoint before uploading",
            });
            expect(csvImportServiceMock.uploadCsvFile).not.toHaveBeenCalled();
        });

        it("should do nothing if no files provided", () => {
            component.selectedEndpoint = mockEndpoints[0];

            component.onUpload({ files: [] });

            expect(csvImportServiceMock.uploadCsvFile).not.toHaveBeenCalled();
        });

        it("should upload file successfully without validation errors", () => {
            component.selectedEndpoint = mockEndpoints[0];
            const response = { importedLineNumber: 5, errors: [] };
            csvImportServiceMock.uploadCsvFile.and.returnValue(of(response));

            component.onUpload({ files: [mockFile] });

            expect(csvImportServiceMock.uploadCsvFile).toHaveBeenCalledWith(
                mockEndpoints[0].name,
                mockFile,
            );
            expect(component.isProcessing).toBeFalse();
            expect(component.uploadedFile).toBe(mockFile);
            expect(component.lastUploadResponse).toEqual(response);
            expect(component.importedLineNumber).toBe(5);
            expect(messageServiceMock.add).toHaveBeenCalledWith({
                severity: "success",
                summary: "Success",
                detail: `File ${mockFile.name} uploaded successfully. 5 line(s) imported.`,
            });
        });

        it("should handle upload with validation errors", () => {
            component.selectedEndpoint = mockEndpoints[0];
            const response = {
                importedLineNumber: 3,
                errors: ["Error on line 1", "Error on line 2"],
            };
            csvImportServiceMock.uploadCsvFile.and.returnValue(of(response));

            component.onUpload({ files: [mockFile] });

            expect(component.isProcessing).toBeFalse();
            expect(component.uploadedFile).toBe(mockFile);
            expect(component.uploadErrors).toEqual(response.errors);
            expect(component.importedLineNumber).toBe(3);
            expect(messageServiceMock.add).toHaveBeenCalledWith({
                severity: "warn",
                summary: "Validation Errors",
                detail: "File uploaded but contains 2 validation error(s). 3 line(s) imported successfully.",
            });
        });

        it("should handle upload error with error.error.message", () => {
            component.selectedEndpoint = mockEndpoints[0];
            const error = { error: { message: "Server error" } };
            csvImportServiceMock.uploadCsvFile.and.returnValue(throwError(() => error));

            component.onUpload({ files: [mockFile] });

            expect(component.isProcessing).toBeFalse();
            expect(component.uploadedFile).toBeNull();
            expect(component.uploadErrors).toEqual([]);
            expect(component.importedLineNumber).toBe(0);
            expect(messageServiceMock.add).toHaveBeenCalledWith({
                severity: "error",
                summary: "Upload Failed",
                detail: `Failed to upload file ${mockFile.name}: Server error`,
            });
        });

        it("should handle upload error with error.message", () => {
            component.selectedEndpoint = mockEndpoints[0];
            const error = { message: "Network error" };
            csvImportServiceMock.uploadCsvFile.and.returnValue(throwError(() => error));

            component.onUpload({ files: [mockFile] });

            expect(component.isProcessing).toBeFalse();
            expect(component.uploadedFile).toBeNull();
            expect(component.uploadErrors).toEqual([]);
            expect(component.importedLineNumber).toBe(0);
            expect(messageServiceMock.add).toHaveBeenCalledWith({
                severity: "error",
                summary: "Upload Failed",
                detail: `Failed to upload file ${mockFile.name}: Network error`,
            });
        });

        it("should handle upload error with string error", () => {
            component.selectedEndpoint = mockEndpoints[0];
            const error = { error: "String error message" };
            csvImportServiceMock.uploadCsvFile.and.returnValue(throwError(() => error));

            component.onUpload({ files: [mockFile] });

            expect(component.isProcessing).toBeFalse();
            expect(component.uploadedFile).toBeNull();
            expect(component.uploadErrors).toEqual([]);
            expect(component.importedLineNumber).toBe(0);
            expect(messageServiceMock.add).toHaveBeenCalledWith({
                severity: "error",
                summary: "Upload Failed",
                detail: `Failed to upload file ${mockFile.name}: String error message`,
            });
        });

        it("should use unknown error message as fallback", () => {
            component.selectedEndpoint = mockEndpoints[0];
            const error = {}; // no error.error, no error.message
            csvImportServiceMock.uploadCsvFile.and.returnValue(throwError(() => error));

            component.onUpload({ files: [mockFile] });

            expect(messageServiceMock.add).toHaveBeenCalledWith({
                severity: "error",
                summary: "Upload Failed",
                detail: `Failed to upload file ${mockFile.name}: Unknown error`,
            });
        });
    });

    describe("onDownload", () => {
        it("should do nothing if no endpoint is selected", () => {
            component.selectedEndpoint = null;

            component.onDownload();

            expect(csvImportServiceMock.downloadCsvFile).not.toHaveBeenCalled();
            expect(component.isDownloading).toBeFalse();
        });

        it("should trigger file download on success", () => {
            component.selectedEndpoint = mockEndpoints[0];
            const mockBlob = new Blob(["col1,col2"], { type: "text/csv" });
            csvImportServiceMock.downloadCsvFile.and.returnValue(of(mockBlob));

            spyOn(globalThis.URL, "createObjectURL").and.returnValue("blob:fake-url");
            spyOn(globalThis.URL, "revokeObjectURL");
            const mockAnchor = {
                href: "",
                download: "",
                click: jasmine.createSpy("click"),
            };
            spyOn(document, "createElement").and.returnValue(mockAnchor as any);

            component.onDownload();

            expect(csvImportServiceMock.downloadCsvFile).toHaveBeenCalledWith(
                mockEndpoints[0].name,
            );
            expect(component.isDownloading).toBeFalse();
            expect(globalThis.URL.createObjectURL).toHaveBeenCalledWith(mockBlob);
            expect(mockAnchor.href).toBe("blob:fake-url");
            expect(mockAnchor.download).toBe(`${mockEndpoints[0].name}.csv`);
            expect(mockAnchor.click).toHaveBeenCalled();
            expect(globalThis.URL.revokeObjectURL).toHaveBeenCalledWith("blob:fake-url");
        });

        it("should set isDownloading to false and show error on failure", () => {
            component.selectedEndpoint = mockEndpoints[0];
            const error = { message: "Network error" };
            csvImportServiceMock.downloadCsvFile.and.returnValue(throwError(() => error));

            component.onDownload();

            expect(component.isDownloading).toBeFalse();
            expect(messageServiceMock.add).toHaveBeenCalledWith({
                severity: "error",
                summary: "Download Failed",
                detail: `Failed to download referential ${mockEndpoints[0].label}`,
            });
        });

        it("should use endpoint name as downloaded filename", () => {
            component.selectedEndpoint = mockEndpoints[1];
            const mockBlob = new Blob(["col1,col2"], { type: "text/csv" });
            csvImportServiceMock.downloadCsvFile.and.returnValue(of(mockBlob));

            spyOn(globalThis.URL, "createObjectURL").and.returnValue("blob:fake-url");
            spyOn(globalThis.URL, "revokeObjectURL");
            const mockAnchor = {
                href: "",
                download: "",
                click: jasmine.createSpy("click"),
            };
            spyOn(document, "createElement").and.returnValue(mockAnchor as any);

            component.onDownload();

            expect(csvImportServiceMock.downloadCsvFile).toHaveBeenCalledWith(
                mockEndpoints[1].name,
            );
            expect(mockAnchor.download).toBe(`${mockEndpoints[1].name}.csv`);
        });
    });

    describe("onError", () => {
        it("should show error message", () => {
            component.onError({});

            expect(messageServiceMock.add).toHaveBeenCalledWith({
                severity: "error",
                summary: "Error",
                detail: "Error uploading files",
            });
        });
    });

    describe("onSelect", () => {
        it("should do nothing if no files provided", () => {
            component.onSelect({ files: [] });

            expect(messageServiceMock.add).not.toHaveBeenCalled();
        });

        it("should show error if file is too large", () => {
            const largeFile = new File(["data"], "large.csv", { type: "text/csv" });
            Object.defineProperty(largeFile, "size", { value: 101 * 1024 * 1024 });

            component.onSelect({ files: [largeFile] });

            expect(messageServiceMock.add).toHaveBeenCalledWith({
                severity: "error",
                summary: "File too large",
                detail: "File large.csv exceeds maximum size of 100MB",
            });
        });

        it("should show error if file is not a CSV", () => {
            const nonCsvFile = new File(["data"], "test.txt", { type: "text/plain" });

            component.onSelect({ files: [nonCsvFile] });

            expect(messageServiceMock.add).toHaveBeenCalledWith({
                severity: "error",
                summary: "Invalid file type",
                detail: "File test.txt is not a CSV file",
            });
        });

        it("should not show error for valid CSV file", () => {
            component.onSelect({ files: [mockFile] });

            expect(messageServiceMock.add).not.toHaveBeenCalled();
        });

        it("should not show error for file with .csv extension and wrong MIME type", () => {
            // The condition uses &&: both name AND type must be wrong to trigger the error
            const csvNameWrongMime = new File(["data"], "test.csv", {
                type: "application/octet-stream",
            });

            component.onSelect({ files: [csvNameWrongMime] });

            expect(messageServiceMock.add).not.toHaveBeenCalled();
        });
    });

    describe("onRemove", () => {
        it("should reset state when removing the uploaded file", () => {
            component.uploadedFile = mockFile;
            component.lastUploadResponse = { importedLineNumber: 10 };
            component.uploadErrors = ["error"];
            component.importedLineNumber = 10;
            component.csvEndpoints = mockEndpoints;

            component.onRemove({ file: { name: mockFile.name } });

            expect(component.uploadedFile).toBeNull();
            expect(component.lastUploadResponse).toBeNull();
            expect(component.uploadErrors).toEqual([]);
            expect(component.importedLineNumber).toBe(0);
            expect(component.csvEndpoints).toEqual([]);
        });

        it("should not reset state when removing a different file", () => {
            component.uploadedFile = mockFile;
            component.lastUploadResponse = { importedLineNumber: 10 };
            component.uploadErrors = ["error"];
            component.importedLineNumber = 10;

            component.onRemove({ file: { name: "other.csv" } });

            expect(component.uploadedFile).toBe(mockFile);
            expect(component.lastUploadResponse).toEqual({ importedLineNumber: 10 });
            expect(component.uploadErrors).toEqual(["error"]);
            expect(component.importedLineNumber).toBe(10);
        });

        it("should do nothing if no file is currently uploaded", () => {
            component.uploadedFile = null;
            component.lastUploadResponse = null;

            component.onRemove({ file: { name: mockFile.name } });

            expect(component.uploadedFile).toBeNull();
            expect(component.lastUploadResponse).toBeNull();
            expect(messageServiceMock.add).not.toHaveBeenCalled();
        });
    });
});
