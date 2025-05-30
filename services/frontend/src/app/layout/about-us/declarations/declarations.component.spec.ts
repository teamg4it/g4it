import { ComponentFixture, TestBed } from "@angular/core/testing";

import { provideHttpClient, withInterceptorsFromDi } from "@angular/common/http";
import { ActivatedRoute } from "@angular/router";
import { TranslateModule } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { DeclarationsComponent } from "./declarations.component";

describe("DeclarationsComponent", () => {
    let component: DeclarationsComponent;
    let fixture: ComponentFixture<DeclarationsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot(), DeclarationsComponent],
            providers: [
                provideHttpClient(withInterceptorsFromDi()),
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: { data: {} },
                    },
                },
                {
                    provide: MessageService,
                    useValue: {}, // Mock implementation of MessageService
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
