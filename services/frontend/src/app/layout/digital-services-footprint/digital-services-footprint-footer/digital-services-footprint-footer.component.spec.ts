import { ComponentFixture, TestBed } from "@angular/core/testing";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { UserService } from "src/app/core/service/business/user.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { DigitalServicesFootprintFooterComponent } from "./digital-services-footprint-footer.component";

describe("DigitalServicesFootprintFooterComponent", () => {
    let component: DigitalServicesFootprintFooterComponent;
    let fixture: ComponentFixture<DigitalServicesFootprintFooterComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DigitalServicesFootprintFooterComponent],
            imports: [
                HttpClientTestingModule,
                RouterTestingModule,
                SharedModule,
                TranslateModule.forRoot(),
            ],
            providers: [UserService, MessageService],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesFootprintFooterComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
