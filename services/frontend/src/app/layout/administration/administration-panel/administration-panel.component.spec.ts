import { TestBed } from "@angular/core/testing";
import { TranslateModule } from "@ngx-translate/core";
import { BehaviorSubject } from "rxjs";
import { User } from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { AdministrationPanelComponent } from "./administration-panel.component";

describe("AdministrationPanelComponent", () => {
    let component: AdministrationPanelComponent;
    let userSubject: BehaviorSubject<User>;
    let userService: jasmine.SpyObj<UserService>;

    beforeEach(async () => {
        const regularAdmin: User = {
            email: "admin@example.com",
            firstName: "Admin",
            lastName: "User",
            id: 1,
            organizations: [],
            isSuperAdmin: false,
        };
        userSubject = new BehaviorSubject(regularAdmin);
        userService = jasmine.createSpyObj("UserService", ["hasAnyAdminRole"]);
        userService.user$ = userSubject.asObservable();
        userService.hasAnyAdminRole.and.returnValue(true);

        await TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot(), AdministrationPanelComponent],
            providers: [{ provide: UserService, useValue: userService }],
        }).compileComponents();

        component = TestBed.createComponent(
            AdministrationPanelComponent,
        ).componentInstance;
        component.ngOnInit();
    });

    it("shows release note management to super administrators", () => {
        userSubject.next({
            email: "super-admin@example.com",
            firstName: "Super",
            lastName: "Admin",
            id: 2,
            organizations: [],
            isSuperAdmin: true,
        });

        expect(component.tabMenuList.map((item) => item.id)).toContain("release-notes");
    });

    it("does not show release note management to regular administrators", () => {
        expect(component.tabMenuList.map((item) => item.id)).not.toContain(
            "release-notes",
        );
        expect(component.tabMenuList.map((item) => item.id)).toContain("users-tab");
    });
});
