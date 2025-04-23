/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Location } from "@angular/common";
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { ActivatedRoute } from "@angular/router";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { Accordion, AccordionModule } from "primeng/accordion";
import { MessageService } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { DropdownModule } from "primeng/dropdown";
import { FileUploadModule } from "primeng/fileupload";
import { ProgressSpinnerModule } from "primeng/progressspinner";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { SidebarModule } from "primeng/sidebar";
import { ToastModule } from "primeng/toast";
import { from } from "rxjs";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { MonthYearPipe } from "src/app/core/pipes/monthyear.pipe";
import { UserService } from "src/app/core/service/business/user.service";
import { EvaluationDataService } from "src/app/core/service/data/evaluation-data.service";
import { LoadingDataService } from "src/app/core/service/data/loading-data.service";
import { InventoryItemComponent } from "./inventory-item.component";

describe("InventoryComponent", () => {
    let component: InventoryItemComponent;
    let fixture: ComponentFixture<InventoryItemComponent>;
    let template: HTMLElement;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [InventoryItemComponent, MonthYearPipe],
            imports: [
                HttpClientTestingModule,
                ToastModule,
                ButtonModule,
                AccordionModule,
                SidebarModule,
                ScrollPanelModule,
                ProgressSpinnerModule,
                ConfirmPopupModule,
                DropdownModule,
                BrowserAnimationsModule,
                FileUploadModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                UserService,
                MessageService,
                LoadingDataService,
                EvaluationDataService,
                Location,
                {
                    provide: ActivatedRoute,
                    useValue: {
                        params: from([{ inventoryDate: "unknown" }]),
                    },
                },
                Accordion,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(InventoryItemComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
        template = fixture.nativeElement;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should disable estimation if physicalEquipmentCount is zero", () => {
        component.inventory = {
            physicalEquipmentCount: 0,
            virtualEquipmentCount: 0,
        } as Inventory;
        const result = component.isEstimationDisabled();
        expect(result).toBeTrue();
    });

    it("should enable estimation if any estimation job has no batch running", () => {
        component.inventory = {
            physicalEquipmentCount: 1,
        } as Inventory;
        const result = component.isEstimationDisabled();
        expect(result).toBeFalse();
    });
});
