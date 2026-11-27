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
import { ComponentFixture, TestBed } from "@angular/core/testing";

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
            jasmine.objectContaining({ severity: "error" }),
        );
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
