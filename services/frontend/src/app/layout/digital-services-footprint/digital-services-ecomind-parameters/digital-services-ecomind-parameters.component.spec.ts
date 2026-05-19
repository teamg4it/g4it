import { ComponentFixture, TestBed } from "@angular/core/testing";

import { ActivatedRoute } from "@angular/router";
import { TranslateModule, TranslateStore } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { DigitalServicesEcomindParametersComponent } from "./digital-services-ecomind-parameters.component";

describe("DigitalServicesEcomindParametersComponent", () => {
    let component: DigitalServicesEcomindParametersComponent;
    let fixture: ComponentFixture<DigitalServicesEcomindParametersComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                DigitalServicesEcomindParametersComponent,
                TranslateModule.forRoot(),
            ],
            providers: [
                MessageService,
                TranslateStore,
                { provide: ActivatedRoute, useValue: {} },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesEcomindParametersComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
