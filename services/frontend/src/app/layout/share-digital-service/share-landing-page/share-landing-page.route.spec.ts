import { TestBed } from "@angular/core/testing";
import { Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { SharedAccessGuard } from "src/app/guard/shared-ds.guard";
import { ShareLandingPageComponent } from "./share-landing-page.component";
import { appRoutes } from "./share-landing-page.route";

describe("Share Landing App Routes", () => {
    let router: Router;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [RouterTestingModule.withRoutes(appRoutes)],
            providers: [
                {
                    provide: SharedAccessGuard,
                    useValue: {
                        canActivate: () => true, // mock guard
                    },
                },
            ],
        }).compileComponents();

        router = TestBed.inject(Router);
        router.initialNavigation();
    });

    it("should configure root route with ShareLandingPageComponent", () => {
        const rootRoute = appRoutes.find((r) => r.path === "");
        expect(rootRoute?.component).toBe(ShareLandingPageComponent);
        expect(rootRoute?.children?.length).toBeGreaterThan(0);
    });
});
