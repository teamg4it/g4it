/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";
import { ApplicationCriteriaPieChartComponent } from "./application-criteria-pie-chart.component";
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from "@angular/core";

describe("ApplicationCriteriaPieChartComponent", () => {
    let component: ApplicationCriteriaPieChartComponent;
    let fixture: ComponentFixture<ApplicationCriteriaPieChartComponent>;
    let inventoryDate: "05-2023";

    beforeEach(() => {
        TestBed.configureTestingModule({
    imports: [
        TranslateModule.forRoot(),
        RouterTestingModule,
        HttpClientTestingModule,
        ApplicationCriteriaPieChartComponent,
    ],
    providers: [
        TranslatePipe,
        TranslateService,
        InventoriesApplicationFootprintComponent,
        {
            provide: ActivatedRoute,
            useValue: {
                snapshot: {
                    paramMap: {
                        get: () => inventoryDate,
                    },
                },
            },
        },
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
});
        fixture = TestBed.createComponent(ApplicationCriteriaPieChartComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
