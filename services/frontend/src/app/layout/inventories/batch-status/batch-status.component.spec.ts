/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { TooltipModule } from "primeng/tooltip";
import { MonthYearPipe } from "src/app/core/pipes/monthyear.pipe";
import { FileSystemDataService } from "src/app/core/service/data/file-system-data.service";
import { BatchStatusComponent } from "./batch-status.component";

describe("BatchStatusComponent", () => {
    let component: BatchStatusComponent;
    let fixture: ComponentFixture<BatchStatusComponent>;
    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [BatchStatusComponent, MonthYearPipe],
            imports: [HttpClientTestingModule, TooltipModule, TranslateModule.forRoot()],
            providers: [
                TranslatePipe,
                TranslateService,
                FileSystemDataService,
                MessageService,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(BatchStatusComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should choose the good class and tootip with batchStatus as UKNOWN", () => {
        component.batchStatusCode = "UNKNOWN";
        component.ngOnInit();
        expect(component.cssClass).toBe("pi pi-spin pi-spinner icon-running");
        expect(component.toolTip).toBe("common.running");
    });

    it("should choose the good class and tootip with batchStatus as COMPLETED", () => {
        component.batchStatusCode = "COMPLETED";
        component.ngOnInit();
        expect(component.toolTip).toBe("common.completed");
    });

    it("should choose the good class and tootip with batchStatus as FAILED", () => {
        component.batchStatusCode = "FAILED";
        component.ngOnInit();
        expect(component.toolTip).toBe("common.failed");
    });

    it("should choose the good class and tootip with batchStatus as COMPLETED_WITH_ERRORS", () => {
        component.batchStatusCode = "COMPLETED_WITH_ERRORS";
        component.ngOnInit();
        expect(component.toolTip).toBe("common.completed-with-errors");
        expect(component.betweenDiv).toBe("!");
    });

    it("should choose the good class and tootip with batchStatus as SKIPPED", () => {
        component.batchStatusCode = "SKIPPED";
        component.ngOnInit();
        expect(component.toolTip).toBe("common.completed-with-errors");
        expect(component.betweenDiv).toBe("!");
    });

    it("should call fileSystemBusinessService.getTaskDetail with the correct taskId", () => {
        const taskId = "12345";
        const fileSystemBusinessServiceSpy = spyOn(
            (component as any).fileSystemBusinessService,
            "getTaskDetail",
        );
        component.getTaskDetail(taskId);
        expect(fileSystemBusinessServiceSpy).toHaveBeenCalledWith(taskId);
    });

    it("should call fileSystemBusinessService.downloadFile with correct arguments", () => {
        component.taskId = "task-1";
        component.selectedSubscriber = "subscriber-1";
        component.selectedOrganization = "org-1";
        component.inventoryId = 42;

        const fileSystemBusinessServiceSpy = spyOn(
            (component as any).fileSystemBusinessService,
            "downloadFile",
        );

        component.downloadFile();

        expect(fileSystemBusinessServiceSpy).toHaveBeenCalledWith(
            "task-1",
            "subscriber-1",
            "org-1",
            42,
        );
    });
});
