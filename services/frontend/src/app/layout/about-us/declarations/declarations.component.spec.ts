import { ComponentFixture, TestBed } from "@angular/core/testing";

import { ActivatedRoute } from "@angular/router";
import { TranslateModule } from "@ngx-translate/core";
import { DeclarationsComponent } from "./declarations.component";

describe("DeclarationsComponent", () => {
    let component: DeclarationsComponent;
    let fixture: ComponentFixture<DeclarationsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot(), DeclarationsComponent],
            providers: [
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: { data: {} },
                    },
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DeclarationsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
