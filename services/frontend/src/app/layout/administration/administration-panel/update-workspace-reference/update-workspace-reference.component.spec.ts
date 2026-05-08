import { provideHttpClient } from "@angular/common/http";
import { provideHttpClientTesting } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateLoader, TranslateModule } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { of } from "rxjs";
import { UpdateWorkspaceReferenceComponent } from "./update-workspace-reference.component";

describe("UpdateWorkspaceReferenceComponent", () => {
    let component: UpdateWorkspaceReferenceComponent;
    let fixture: ComponentFixture<UpdateWorkspaceReferenceComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                UpdateWorkspaceReferenceComponent,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useValue: {
                            getTranslation: () => of({}),
                        },
                    },
                }),
            ],
            providers: [MessageService, provideHttpClient(), provideHttpClientTesting()],
        }).compileComponents();

        fixture = TestBed.createComponent(UpdateWorkspaceReferenceComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
