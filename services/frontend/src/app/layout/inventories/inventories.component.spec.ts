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
import { Injectable } from "@angular/core";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { ActivatedRoute } from "@angular/router";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { AccordionModule } from "primeng/accordion";
import { ButtonModule } from "primeng/button";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { DropdownModule } from "primeng/dropdown";
import { FileUploadModule } from "primeng/fileupload";
import { ProgressSpinnerModule } from "primeng/progressspinner";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { SidebarModule } from "primeng/sidebar";
import { ToastModule } from "primeng/toast";
import { Observable, from, of } from "rxjs";
import { Inventory } from "src/app/core/interfaces/inventory.interfaces";
import { MonthYearPipe } from "src/app/core/pipes/monthyear.pipe";
import { EvaluationDataService } from "src/app/core/service/data/evaluation-data.service";
import { InventoryDataService } from "src/app/core/service/data/inventory-data.service";
import { LoadingDataService } from "src/app/core/service/data/loading-data.service";
import { FilePanelComponent } from "./file-panel/file-panel.component";
import { InventoriesComponent } from "./inventories.component";
import { UserService } from "src/app/core/service/business/user.service";
import { MessageService } from "primeng/api";

@Injectable()
class InventoryServiceMock extends InventoryDataService {
    override getInventories(): Observable<Inventory[]> {
        return of([]);
    }
}

describe("InventoryComponent", () => {
    let component: InventoriesComponent;
    let fixture: ComponentFixture<InventoriesComponent>;
    let inventoryDate: string = "06-2023";
    let template: HTMLElement;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
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
        InventoriesComponent, MonthYearPipe, FilePanelComponent,
    ],
    providers: [
        TranslatePipe,
        TranslateService,
        MessageService,
        UserService,
        {
            provide: InventoryDataService,
            useClass: InventoryServiceMock,
        },
        LoadingDataService,
        EvaluationDataService,
        Location,
        {
            provide: ActivatedRoute,
            useValue: {
                params: from([{ inventoryDate: inventoryDate }]),
            },
        },
    ],
}).compileComponents();

        fixture = TestBed.createComponent(InventoriesComponent);
        component = fixture.componentInstance;
        component.doLoop = false;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
        template = fixture.nativeElement;
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should unsubscribe on component destruction", () => {
        spyOn(component.ngUnsubscribe, "next");
        spyOn(component.ngUnsubscribe, "complete");

        fixture.destroy();

        expect(component.ngUnsubscribe.next).toHaveBeenCalled();
        expect(component.ngUnsubscribe.complete).toHaveBeenCalled();
    });
});
