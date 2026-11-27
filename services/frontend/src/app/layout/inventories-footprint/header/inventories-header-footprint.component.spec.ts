// This file has been created (totally or partially) with the assistance of artificial intelligence tools.
// All content has been generated under the direct supervision of a named individual,
// and under the AI.Backbone Orchestrator Compliance framework

/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import {
    ComponentFixture,
    TestBed,
    fakeAsync,
    flush,
    flushMicrotasks,
    tick,
} from "@angular/core/testing";

import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { ButtonModule } from "primeng/button";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { ToastModule } from "primeng/toast";
import { SharedModule } from "src/app/core/shared/shared.module";
import { InventoriesHeaderFootprintComponent } from "./inventories-header-footprint.component";
import { RouterTestingModule } from "@angular/router/testing";
import { UserService } from "src/app/core/service/business/user.service";
import { ConfirmationService, MessageService } from "primeng/api";
import { of, throwError } from "rxjs";

describe("InventoriesHeaderFootprintComponent", () => {
    let component: InventoriesHeaderFootprintComponent;
    let fixture: ComponentFixture<InventoriesHeaderFootprintComponent>;
    let httpMock: HttpTestingController;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
    imports: [
        RouterTestingModule,
        ButtonModule,
        ConfirmPopupModule,
        ToastModule,
        SharedModule,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        InventoriesHeaderFootprintComponent,
    ],
    providers: [
        TranslatePipe,
        TranslateService,
        {
            provide: UserService,
            useValue: {
                currentOrganization$: of({ name: "synthetic-org" }),
                currentWorkspace$: of({ name: "synthetic-workspace" }),
                isAllowedInventoryRead$: of(true),
                isAllowedInventoryWrite$: of(true),
            },
        },
        MessageService,
    ],
}).compileComponents();

        fixture = TestBed.createComponent(InventoriesHeaderFootprintComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("uses the spreadsheet download for the primary export action", () => {
        const download = spyOn(component, "downloadFile").and.resolveTo();

        fixture.nativeElement.querySelector("#export-inventory-button button").click();

        expect(download).toHaveBeenCalledOnceWith();
        expect(component.downloadInProgress).toBeTrue();
    });

    it("requires explicit confirmation before downloading raw CSV", () => {
        const confirmation = fixture.debugElement.injector.get(ConfirmationService);
        const confirm = spyOn(confirmation, "confirm");
        const download = spyOn(component, "downloadFile").and.resolveTo();

        fixture.nativeElement.querySelector("#export-inventory-csv-button button").click();

        expect(confirm).toHaveBeenCalled();
        expect(download).not.toHaveBeenCalled();
        const request = confirm.calls.mostRecent().args[0];
        expect(request.message).toBe("inventories-footprint.header.raw-warning");
        request.accept?.();
        expect(download).toHaveBeenCalledOnceWith("csv");
    });

    it("keeps raw CSV confirmation mandatory when the event has no target", () => {
        const confirmation = fixture.debugElement.injector.get(ConfirmationService);
        const confirm = spyOn(confirmation, "confirm");
        const download = spyOn(component, "downloadFile").and.resolveTo();

        component.confirmRawDownload(new Event("click"));

        const request = confirm.calls.mostRecent().args[0];
        expect(request.target).toBeUndefined();
        expect(download).not.toHaveBeenCalled();
        request.accept?.();
        expect(download).toHaveBeenCalledOnceWith("csv");
        expect(component.downloadInProgress).toBeTrue();
    });

    (["xlsx", "csv"] as const).forEach((format) => {
        it(`saves the ${format} export with the correct filename and resets progress`, fakeAsync(() => {
            const blob = new Blob(["synthetic export"]);
            const spreadsheet = spyOn(
                component.footprintService,
                "downloadSpreadsheetExport",
            ).and.returnValue(of(blob));
            const raw = spyOn(
                component.footprintService,
                "downloadExportResultsFile",
            ).and.returnValue(of(blob));
            const createUrl = spyOn(globalThis.URL, "createObjectURL").and.returnValue(
                "blob:synthetic-export",
            );
            const revokeUrl = spyOn(globalThis.URL, "revokeObjectURL");
            let filename: string | undefined;
            const saveClick = spyOn(
                HTMLAnchorElement.prototype,
                "dispatchEvent",
            ).and.callFake(function (this: HTMLAnchorElement) {
                filename = this.download;
                return true;
            });
            const messages = fixture.debugElement.injector.get(MessageService);
            const notify = spyOn(messages, "add");
            component.inventoryId = 9;
            component.downloadInProgress = true;

            void component.downloadFile(format);
            flushMicrotasks();
            tick(0);

            expect(format === "xlsx" ? spreadsheet : raw).toHaveBeenCalledOnceWith(9);
            expect(format === "xlsx" ? raw : spreadsheet).not.toHaveBeenCalled();
            expect(createUrl).toHaveBeenCalledOnceWith(blob);
            expect(saveClick).toHaveBeenCalledOnceWith(jasmine.any(MouseEvent));
            expect(filename).toBe(
                `g4it_synthetic-org_synthetic-workspace_9_export-result-files.${format === "xlsx" ? "xlsx" : "zip"}`,
            );
            expect(component.downloadInProgress).toBeTrue();
            tick(2000);
            expect(component.downloadInProgress).toBeFalse();
            expect(notify).not.toHaveBeenCalled();
            flush();
            expect(revokeUrl).toHaveBeenCalledOnceWith("blob:synthetic-export");
        }));
    });

    it("reports a spreadsheet error without falling back to unsafe raw CSV", async () => {
        spyOn(component.footprintService, "downloadSpreadsheetExport").and.returnValue(
            throwError(() => new Error("synthetic export failure")),
        );
        const rawDownload = spyOn(component.footprintService, "downloadExportResultsFile");
        const messages = fixture.debugElement.injector.get(MessageService);
        const notify = spyOn(messages, "add");
        component.downloadInProgress = true;

        await component.downloadFile();

        expect(rawDownload).not.toHaveBeenCalled();
        expect(notify).toHaveBeenCalledWith(
            jasmine.objectContaining({
                severity: "error",
                summary: "inventories-footprint.header.export-error",
            }),
        );
        expect(component.downloadInProgress).toBeFalse();
    });

    it("reports a raw CSV download failure without falling back to a spreadsheet", async () => {
        const rawDownload = spyOn(
            component.footprintService,
            "downloadExportResultsFile",
        ).and.returnValue(throwError(() => new Error("synthetic raw export failure")));
        const spreadsheet = spyOn(component.footprintService, "downloadSpreadsheetExport");
        const messages = fixture.debugElement.injector.get(MessageService);
        const notify = spyOn(messages, "add");
        component.downloadInProgress = true;

        await component.downloadFile("csv");

        expect(rawDownload).toHaveBeenCalledOnceWith(component.inventoryId);
        expect(spreadsheet).not.toHaveBeenCalled();
        expect(notify).toHaveBeenCalledOnceWith({
            severity: "error",
            summary: "common.fileNoLongerAvailable",
        });
        expect(component.downloadInProgress).toBeFalse();
    });

    it("should unsubscribe on component destruction", () => {
        spyOn(component.ngUnsubscribe, "next");
        spyOn(component.ngUnsubscribe, "complete");

        fixture.destroy();

        expect(component.ngUnsubscribe.next).toHaveBeenCalled();
        expect(component.ngUnsubscribe.complete).toHaveBeenCalled();
    });
});
