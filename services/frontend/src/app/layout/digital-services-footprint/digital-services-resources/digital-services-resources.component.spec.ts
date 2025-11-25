import { ComponentFixture, TestBed } from "@angular/core/testing";

import { ActivatedRoute, convertToParamMap } from "@angular/router";
import { of } from "rxjs";
import { DigitalServicesResourcesComponent } from "./digital-services-resources.component";

describe("DigitalServicesResourcesComponent", () => {
    let component: DigitalServicesResourcesComponent;
    let fixture: ComponentFixture<DigitalServicesResourcesComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DigitalServicesResourcesComponent],
            providers: [
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
});
