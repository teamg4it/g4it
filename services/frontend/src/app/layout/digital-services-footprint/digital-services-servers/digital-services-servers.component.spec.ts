import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { of } from "rxjs";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { InPhysicalEquipmentsService } from "src/app/core/service/data/in-out/in-physical-equipments.service";
import { InVirtualEquipmentsService } from "src/app/core/service/data/in-out/in-virtual-equipments.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { DigitalServicesServersComponent } from "./digital-services-servers.component";

fdescribe("DigitalServicesServersComponent", () => {
    let component: DigitalServicesServersComponent;
    let fixture: ComponentFixture<DigitalServicesServersComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DigitalServicesServersComponent],
            imports: [
                RouterTestingModule,
                TranslateModule.forRoot(),
                HttpClientTestingModule,
            ],
            providers: [
                MessageService,
                DigitalServiceStoreService,
                InPhysicalEquipmentsService,
                InVirtualEquipmentsService,
                DigitalServicesDataService,
                DigitalServiceBusinessService,
                UserService,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesServersComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    describe("ngOnInit", () => {
        let digitalServiceDataMock: any;
        let digitalServiceBusinessMock: any;
        let routerMock: any;

        beforeEach(() => {
            digitalServiceDataMock = TestBed.inject(DigitalServicesDataService);
            digitalServiceBusinessMock = TestBed.inject(DigitalServiceBusinessService);
            routerMock = TestBed.inject(Router);

            spyOn(digitalServiceDataMock.digitalService$, "pipe").and.callFake(() => {
                return of({ uid: "test-ds" });
            });

            spyOn(digitalServiceBusinessMock.panelSubject$, "pipe").and.callFake(() => {
                return of(true);
            });

            spyOn(routerMock, "navigate");
            spyOnProperty(routerMock, "url", "get").and.returnValue("/some-url");
        });

        it("should subscribe to panelSubject$ and set sidebarVisible", async () => {
            await component.ngOnInit();
            expect(component.sidebarVisible).toBeTrue();
        });

        it("should navigate to ../resources if sidebarVisible becomes false and url does not end with /resources", async () => {
            digitalServiceBusinessMock.panelSubject$.pipe.and.callFake(() => of(false));
            await component.ngOnInit();
            expect(routerMock.navigate).toHaveBeenCalledWith(["../resources"], {
                relativeTo: jasmine.anything(),
            });
        });
    });
});
