import { ComponentFixture, TestBed } from "@angular/core/testing";

import { ActivatedRoute, convertToParamMap } from "@angular/router";
import { TranslateModule } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { of } from "rxjs";
import { DigitalServicesResourcesComponent } from "./digital-services-resources.component";

describe("DigitalServicesResourcesComponent", () => {
    let component: DigitalServicesResourcesComponent;
    let fixture: ComponentFixture<DigitalServicesResourcesComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [DigitalServicesResourcesComponent, TranslateModule.forRoot()],
            providers: [
                MessageService,
                {
                    provide: ActivatedRoute,
                    useValue: {
                        parent: {
                            paramMap: of(
                                convertToParamMap({
                                    digitalServiceVersionId: "12345",
                                }),
                            ),
                        },
                    },
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesResourcesComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should show only the selected resource category", () => {
        expect(
            fixture.nativeElement.querySelector("app-digital-services-terminals"),
        ).toBeTruthy();
        expect(
            fixture.nativeElement.querySelector("app-digital-services-networks"),
        ).toBeNull();

        fixture.nativeElement.querySelector('[data-category="networks"] button').click();
        fixture.detectChanges();

        expect(
            fixture.nativeElement.querySelector("app-digital-services-terminals"),
        ).toBeNull();
        expect(
            fixture.nativeElement.querySelector("app-digital-services-networks"),
        ).toBeTruthy();
    });

    it("should show both Cloud and AI tables under external services", () => {
        fixture.nativeElement
            .querySelector('[data-category="external-services"] button')
            .click();
        fixture.detectChanges();

        expect(
            fixture.nativeElement.querySelector("app-digital-services-cloud-services"),
        ).toBeTruthy();
        expect(
            fixture.nativeElement.querySelector("app-digital-services-ai-services"),
        ).toBeTruthy();
    });

    it("should hide category buttons when the sidebar is collapsed", () => {
        fixture.nativeElement.querySelector("#collapse-button button").click();
        fixture.detectChanges();

        expect(fixture.nativeElement.querySelectorAll("app-resource-button").length).toBe(
            0,
        );
        expect(component.isSidebarCollapsed).toBeTrue();

        fixture.nativeElement.querySelector("#expand-button button").click();
        fixture.detectChanges();

        expect(fixture.nativeElement.querySelectorAll("app-resource-button").length).toBe(
            4,
        );
        expect(component.isSidebarCollapsed).toBeFalse();
    });
});
