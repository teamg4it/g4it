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
import { MessageService } from "primeng/api";

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
    providers: [TranslatePipe, TranslateService, UserService, MessageService],
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

    it("should unsubscribe on component destruction", () => {
        spyOn(component.ngUnsubscribe, "next");
        spyOn(component.ngUnsubscribe, "complete");

        fixture.destroy();

        expect(component.ngUnsubscribe.next).toHaveBeenCalled();
        expect(component.ngUnsubscribe.complete).toHaveBeenCalled();
    });
});
