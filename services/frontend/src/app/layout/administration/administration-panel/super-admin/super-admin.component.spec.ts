import { TestBed } from "@angular/core/testing";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { Subject } from "rxjs";
import { UserService } from "src/app/core/service/business/user.service";
import { SuperAdminDataService } from "src/app/core/service/data/super-admin-data.service";
import { SuperAdminComponent } from "./super-admin.component";

describe("SuperAdminComponent", () => {
    let component: SuperAdminComponent;
    let userServiceMock: jasmine.SpyObj<UserService>;
    let routerMock: jasmine.SpyObj<Router>;
    let superAdminDataServiceMock: jasmine.SpyObj<SuperAdminDataService>;
    let userSubject: Subject<any>;

    beforeEach(() => {
        userSubject = new Subject();

        userServiceMock = jasmine.createSpyObj("UserService", ["user$"]);
        userServiceMock.user$ = userSubject.asObservable();

        routerMock = jasmine.createSpyObj("Router", ["navigateByUrl"]);

        superAdminDataServiceMock = jasmine.createSpyObj("SuperAdminDataService", [
            "launchReleaseScript",
        ]);

        TestBed.configureTestingModule({
            providers: [
                SuperAdminComponent,
                { provide: UserService, useValue: userServiceMock },
                { provide: Router, useValue: routerMock },
                { provide: TranslateService, useValue: {} },
                { provide: SuperAdminDataService, useValue: superAdminDataServiceMock },
            ],
        });

        component = TestBed.inject(SuperAdminComponent);
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    describe("ngOnInit", () => {
        it("should navigate to 403 if the user is not a super admin", () => {
            component.ngOnInit();
            userSubject.next({ isSuperAdmin: false });
            expect(routerMock.navigateByUrl).toHaveBeenCalledWith(
                "something-went-wrong/403",
            );
        });

        it("should not navigate if the user is a super admin", () => {
            component.ngOnInit();
            userSubject.next({ isSuperAdmin: true });
            expect(routerMock.navigateByUrl).not.toHaveBeenCalled();
        });
    });

    describe("launchReleaseScript", () => {
        it("should disable the button and call launchReleaseScript", () => {
            const launchReleaseScriptSubject = new Subject<void>();
            superAdminDataServiceMock.launchReleaseScript.and.returnValue(
                launchReleaseScriptSubject.asObservable(),
            );

            component.launchReleaseScript();

            expect(component.isMigrateDataButtonDisabled).toBeTrue();
            expect(superAdminDataServiceMock.launchReleaseScript).toHaveBeenCalled();
        });
    });
});
