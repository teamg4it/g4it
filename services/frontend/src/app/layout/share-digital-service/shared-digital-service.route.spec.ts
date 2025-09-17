import { Location } from "@angular/common";
import { TestBed } from "@angular/core/testing";
import { Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";

import { HttpClientModule } from "@angular/common/http";
import { TranslateModule } from "@ngx-translate/core";
import { DigitalServicesFootprintDashboardComponent } from "../digital-services-footprint/digital-services-footprint-dashboard/digital-services-footprint-dashboard.component";
import { DigitalServicesResourcesComponent } from "../digital-services-footprint/digital-services-resources/digital-services-resources.component";
import { ShareDigitalServiceComponent } from "./share-digital-service.component";
import { shareDsRoutes } from "./shared-digital-service.route";

describe("shareDsRoutes", () => {
    let router: Router;
    let location: Location;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                RouterTestingModule.withRoutes(shareDsRoutes),
                HttpClientModule,
                TranslateModule.forRoot(),
            ],
            declarations: [
                ShareDigitalServiceComponent,
                DigitalServicesFootprintDashboardComponent,
                DigitalServicesResourcesComponent,
            ],
        }).compileComponents();

        router = TestBed.inject(Router);
        location = TestBed.inject(Location);
        router.initialNavigation();
    });

    it('should redirect "" to "resources"', async () => {
        await router.navigate([""]);
        expect(location.path()).toBe("/resources");
    });

    it("should have isShared=true in dashboard route data", () => {
        const dashboardRoute = shareDsRoutes[0].children?.find(
            (r) => r.path === "dashboard",
        );
        expect(dashboardRoute?.data?.["isShared"]).toBeTrue();
    });

    it("should have isShared=true in resources route data", () => {
        const resourcesRoute = shareDsRoutes[0].children?.find(
            (r) => r.path === "resources",
        );
        expect(resourcesRoute?.data?.["isShared"]).toBeTrue();
    });
});
