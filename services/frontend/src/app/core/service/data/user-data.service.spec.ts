/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Role } from "../../interfaces/roles.interfaces";
import { User } from "../../interfaces/user.interfaces";
import { UserDataService } from "./user-data.service";

describe("UserDataService", () => {
    let httpMock: HttpTestingController;
    let userService: UserDataService;
    let organization: string = "SSG";
    let userInfo: User = {
        email: "prenom.nom@soprasteria.com",
        firstName: "prenom",
        lastName: "nom",
        id: 1,
        isSuperAdmin: true,
        subscribers: [
            {
                id: 1,
                name: "SSG",
                defaultFlag: true,
                organizations: [
                    {
                        id: 1,
                        name: "SSG",
                        defaultFlag: true,
                        roles: [
                            Role.InventoryRead,
                            Role.DigitalServiceRead,
                            Role.InventoryWrite,
                            Role.DigitalServiceWrite,
                        ],
                        status: "ACTIVE",
                    },
                    {
                        id: 2,
                        name: "G4IT",
                        defaultFlag: false,
                        roles: [
                            Role.InventoryRead,
                            Role.DigitalServiceRead,
                            Role.InventoryWrite,
                            Role.DigitalServiceWrite,
                        ],
                        status: "ACTIVE",
                    },
                ],
                roles: [
                    Role.InventoryRead,
                    Role.DigitalServiceRead,
                    Role.InventoryWrite,
                    Role.DigitalServiceWrite,
                ],
            },
        ],
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [UserDataService],
        });
        userService = TestBed.inject(UserDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be created", () => {
        expect(UserDataService).toBeTruthy();
    });

    it("fetchUserInfo() should http GET organization", () => {
        userService.fetchUserInfo().subscribe((res) => {
            expect(res).toEqual(userInfo);
            expect(res.subscribers[0].name).toEqual(organization);
        });

        const req = httpMock.expectOne(`users/me`);
        expect(req.request.method).toEqual("GET");
        req.flush(userInfo);

        httpMock.verify();
    });
});
