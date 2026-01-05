import { ComponentFixture, TestBed } from "@angular/core/testing";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ActivatedRoute } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule } from "@ngx-translate/core";
import { KeycloakService } from "keycloak-angular";
import { MessageService } from "primeng/api";
import { UserService } from "src/app/core/service/business/user.service";
import { ShareLandingPageComponent } from "./share-landing-page.component";

describe("ShareLandingPageComponent", () => {
    let component: ShareLandingPageComponent;
    let fixture: ComponentFixture<ShareLandingPageComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ShareLandingPageComponent,
                TranslateModule.forRoot(),
                HttpClientTestingModule,
                RouterTestingModule,
            ],
            providers: [
                KeycloakService,
                { provide: ActivatedRoute, useValue: {} },

                UserService,
                MessageService,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ShareLandingPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
