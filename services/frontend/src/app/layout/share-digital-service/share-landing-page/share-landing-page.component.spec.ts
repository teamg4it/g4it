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
        })
            .overrideTemplate(
                ShareLandingPageComponent,
                `
        <div #mainContent>
          <button id="firstBtn">First</button>
          <button id="secondBtn">Second</button>
        </div>
        `,
            )
            .compileComponents();

        fixture = TestBed.createComponent(ShareLandingPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
    it("should focus the first focusable element", () => {
        const firstButton = fixture.nativeElement.querySelector(
            "#firstBtn",
        ) as HTMLButtonElement;

        component.focusFirstElement();
        fixture.detectChanges();

        expect(document.activeElement).toBe(firstButton);
    });

    it("should not throw error if no focusable elements exist", () => {
        component.mainContent.nativeElement.innerHTML = "<div>No focus</div>";

        expect(() => component.focusFirstElement()).not.toThrow();
    });
});
