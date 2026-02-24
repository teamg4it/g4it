import { ComponentFixture, TestBed } from "@angular/core/testing";
import { NoopAnimationsModule } from "@angular/platform-browser/animations";
import { TranslateModule } from "@ngx-translate/core";

import { ConfigureViewFiltersComponent } from "./configure-view-filters.component";

describe("ConfigureViewFiltersComponent", () => {
    let component: ConfigureViewFiltersComponent;
    let fixture: ComponentFixture<ConfigureViewFiltersComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ConfigureViewFiltersComponent,
                TranslateModule.forRoot(),
                NoopAnimationsModule,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ConfigureViewFiltersComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
