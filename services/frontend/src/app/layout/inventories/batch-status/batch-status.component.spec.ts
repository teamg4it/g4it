/*
 * G4IT5
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { provideHttpClient } from "@angular/common/http";
import { provideHttpClientTesting } from "@angular/common/http/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { TooltipModule } from "primeng/tooltip";
import { of } from "rxjs";
import { MonthYearPipe } from "src/app/core/pipes/monthyear.pipe";
import { UserService } from "src/app/core/service/business/user.service";
import { FileSystemDataService } from "src/app/core/service/data/file-system-data.service";
import { BatchStatusComponent } from "./batch-status.component";

describe("BatchStatusComponent", () => {
    let component: BatchStatusComponent;
    let fixture: ComponentFixture<BatchStatusComponent>;
    let mockUserService: any;

    beforeEach(async () => {
        mockUserService = {
            currentOrganization$: of({ name: "test-org" }),
            currentWorkspace$: of({ name: "test-workspace" }),
        };

        await TestBed.configureTestingModule({
            imports: [
                TooltipModule,
                TranslateModule.forRoot(),
                BatchStatusComponent,
                MonthYearPipe,
            ],
            providers: [
                provideHttpClient(),
                provideHttpClientTesting(),
                TranslatePipe,
                TranslateService,
                FileSystemDataService,
                MessageService,
                { provide: UserService, useValue: mockUserService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(BatchStatusComponent);
        component = fixture.componentInstance;

        // Set a valid createTime to avoid date pipe errors
        fixture.componentRef.setInput("createTime", new Date("2024-01-01T12:00:00"));

        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should choose the good class and tooltip with batchStatus as UNKNOWN", () => {
        fixture.componentRef.setInput("batchStatusCode", "UNKNOWN");
        fixture.detectChanges();
        component.ngOnInit();
        expect(component.cssClass).toBe("pi pi-spin pi-spinner icon-running");
        expect(component.toolTip).toBe("common.running");
    });

    it("should choose the good class and tooltip with batchStatus as COMPLETED", () => {
        fixture.componentRef.setInput("batchStatusCode", "COMPLETED");
        fixture.detectChanges();
        component.ngOnInit();
        expect(component.toolTip).toBe("common.completed");
    });

    it("should choose the good class and tooltip with batchStatus as FAILED", () => {
        fixture.componentRef.setInput("batchStatusCode", "FAILED");
        fixture.detectChanges();
        component.ngOnInit();
        expect(component.toolTip).toBe("common.failed");
    });

    it("should choose the good class and tooltip with batchStatus as COMPLETED_WITH_ERRORS", () => {
        fixture.componentRef.setInput("batchStatusCode", "COMPLETED_WITH_ERRORS");
        fixture.detectChanges();
        component.ngOnInit();
        expect(component.toolTip).toBe("common.completed-with-errors");
        expect(component.betweenDiv).toBe("!");
    });

    it("should choose the good class and tooltip with batchStatus as SKIPPED", () => {
        fixture.componentRef.setInput("batchStatusCode", "SKIPPED");
        fixture.detectChanges();
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
        fixture.componentRef.setInput("taskId", "task-1");
        fixture.componentRef.setInput("inventoryId", 42);
        fixture.detectChanges();

        component.selectedOrganization = "organization-1";
        component.selectedWorkspace = "work-1";

        const fileSystemBusinessServiceSpy = spyOn(
            (component as any).fileSystemBusinessService,
            "downloadFile",
        );

        component.downloadFile();

        expect(fileSystemBusinessServiceSpy).toHaveBeenCalledWith(
            "task-1",
            "organization-1",
            "work-1",
            42,
        );
    });
});
