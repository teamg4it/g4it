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

import { Constants } from "src/constants";
import { environment } from "src/environments/environment";
import {
    DigitalService,
    Host,
    NetworkType,
    ShareLinkResp,
    TerminalsType,
} from "../../interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "./digital-services-data.service";

describe("DigitalServicesDataService", () => {
    let service: DigitalServicesDataService;
    let httpMock: HttpTestingController;

    const endpointDsVersions = Constants.ENDPOINTS.digitalServicesVersions;
    const sharedEndpoint = Constants.ENDPOINTS.sharedDs;
    const dsSegment = Constants.ENDPOINTS.dsv;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
        });
        service = TestBed.inject(DigitalServicesDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should list digital services", () => {
        const allDigitalService: DigitalService[] = [
            {
                uid: "",
                name: "Digital Service#1",
                lastUpdateDate: Date.now(),
                creationDate: Date.now(),
                lastCalculationDate: null,
                networks: [],
                servers: [],
                terminals: [],
                isAi: false,
                enableDataInconsistency: false,
                activeDsvUid: "1",
            },
        ];

        service.list().subscribe((res) => {
            expect(res[0].name).toBe("Digital Service#1");
            expect(res).toHaveSize(1);
        });

        const req = httpMock.expectOne(`digital-services`);
        expect(req.request.method).toEqual("GET");

        req.flush(allDigitalService);

        httpMock.verify();
    });

    it("should create a digital service", () => {
        const newDigitalService: DigitalService = {
            uid: "",
            name: "Digital Service#1",
            lastUpdateDate: Date.now(),
            creationDate: Date.now(),
            lastCalculationDate: null,
            networks: [],
            servers: [],
            terminals: [],
            isAi: false,
            enableDataInconsistency: false,
            activeDsvUid: "1",
        };

        service
            .create({
                dsName: "1",
                versionName: "2",
            })
            .subscribe((res) => {
                expect(res.name).toBe("Digital Service#1");
            });

        const req = httpMock.expectOne(`digital-service-version`);
        expect(req.request.method).toEqual("POST");

        req.flush(newDigitalService);

        httpMock.verify();
    });

    it("should update a digital service", () => {
        const updatedDigitalService: DigitalService = {
            uid: "ds-uuid",
            name: "Digital Service#1",
            lastUpdateDate: Date.now(),
            creationDate: Date.now(),
            lastCalculationDate: null,
            networks: [],
            servers: [],
            terminals: [],
            isAi: false,
            enableDataInconsistency: false,
            activeDsvUid: "1",
        };

        service.update(updatedDigitalService).subscribe((res) => {
            expect(res.name).toBe("Digital Service#1");
        });

        const req = httpMock.expectOne(`${endpointDsVersions}/ds-uuid`);
        expect(req.request.method).toEqual("PUT");

        req.flush(updatedDigitalService);

        httpMock.verify();
    });

    it("should get a digital service", () => {
        const digitalService: DigitalService = {
            uid: "ds-uuid",
            name: "Digital Service#1",
            lastUpdateDate: Date.now(),
            creationDate: Date.now(),
            lastCalculationDate: null,
            networks: [],
            servers: [],
            terminals: [],
            isAi: false,
            enableDataInconsistency: false,
            activeDsvUid: "1",
        };

        service.get(digitalService.uid).subscribe((res) => {
            expect(res.name).toBe(digitalService.name);
        });

        const req = httpMock.expectOne(`${endpointDsVersions}/ds-uuid`);
        expect(req.request.method).toEqual("GET");

        req.flush(digitalService);

        httpMock.verify();
    });

    it("should get a digital service", () => {
        const digitalService: DigitalService = {
            uid: "ds-uuid",
            name: "Digital Service#1",
            lastUpdateDate: Date.now(),
            creationDate: Date.now(),
            lastCalculationDate: null,
            networks: [],
            servers: [],
            terminals: [],
            isAi: false,
            enableDataInconsistency: false,
            activeDsvUid: "1",
        };

        service.getDsTasks(digitalService.uid).subscribe((res) => {
            expect(res.name).toBe(digitalService.name);
        });

        const req = httpMock.expectOne(`${endpointDsVersions}/ds-uuid`);
        expect(req.request.method).toEqual("GET");

        req.flush(digitalService);

        httpMock.verify();
    });

    it("should delete a digital service", () => {
        const digitalService: DigitalService = {
            uid: "ds-uuid",
            name: "Digital Service#1",
            lastUpdateDate: Date.now(),
            creationDate: Date.now(),
            lastCalculationDate: null,
            networks: [],
            servers: [],
            terminals: [],
            isAi: false,
            enableDataInconsistency: false,
            activeDsvUid: "1",
        };

        service.delete(digitalService.uid).subscribe();

        const req = httpMock.expectOne(`digital-services/ds-uuid`);
        expect(req.request.method).toEqual("DELETE");

        httpMock.verify();
    });

    it("should get a networkType", () => {
        const networks: NetworkType[] = [
            {
                code: "fixe-line-network-1",
                value: "Fixed FR",
                annualQuantityOfGo: 1,
                country: "France",
                type: "type",
            },
        ];

        service.getNetworkReferential().subscribe((res) => {
            expect(res).toEqual(networks);
        });

        const req = httpMock.expectOne(`digital-services/network-type`);

        expect(req.request.method).toEqual("GET");

        httpMock.verify();
    });

    it("should get referentials of device type", () => {
        const types: TerminalsType[] = [
            {
                code: "smartphone-2",
                value: "Mobile Phone",
                lifespan: 5,
            },
            {
                code: "landline-phone-1",
                value: "Landline",
                lifespan: 5,
            },
            {
                code: "tablet-3",
                value: "Tablet",
                lifespan: 5,
            },
        ];

        service.getDeviceReferential().subscribe((res) => {
            expect(res).toEqual(types);
        });

        const req = httpMock.expectOne(`digital-services/device-type`);

        expect(req.request.method).toEqual("GET");

        httpMock.verify();
    });

    it("should get referentials of country", () => {
        const countries: string[] = ["France", "Germany", "China"];

        service.getCountryReferential().subscribe((res) => {
            expect(res).toHaveSize(3);
            expect(res).toContain("France");
            expect(res).toContain("China");
            expect(res).toContain("Germany");
        });

        const req = httpMock.expectOne(`digital-services/country`);

        expect(req.request.method).toEqual("GET");

        httpMock.verify();
    });

    it("should get referentials of host servers", () => {
        const host: Host[] = [
            {
                code: 2,
                value: "Server Storage S",
                characteristic: [
                    {
                        code: "lifespan",
                        value: 5,
                    },
                    {
                        code: "vCPU",
                        value: 36,
                    },
                ],
            },
            {
                code: 3,
                value: "Server Storage M",
                characteristic: [
                    {
                        code: "lifespan",
                        value: 5,
                    },
                    {
                        code: "vCPU",
                        value: 36,
                    },
                ],
            },
        ];

        service.getHostServerReferential("Storage").subscribe((res) => {
            expect(res).toEqual(host);
            expect(res).toHaveSize(2);
        });

        const req = httpMock.expectOne(`digital-services/server-host?type=Storage`);

        expect(req.request.method).toEqual("GET");

        httpMock.verify();
    });

    it("copyUrl should call /{uid}/share and map to frontEndUrl + response + /footprint and extendLink=true", () => {
        const uid = "ds-123";

        let result: ShareLinkResp | undefined;
        const ds = {
            uid: uid,
            lastCalculationDate: "2024-01-01",
        } as unknown as DigitalService;
        service.copyUrl(uid, ds, true).subscribe((resp) => {
            expect(resp.url).toBe(
                environment.frontEndUrl + "/shared/abc/footprint/dashboard",
            );
        });

        const req = httpMock.expectOne(
            `${endpointDsVersions}/${uid}/share?extendLink=true`,
        );
        expect(req.request.method).toBe("GET");
    });

    it("copyUrl should call /{uid}/share and map to frontEndUrl + response + /footprint and extendLink=false", () => {
        const uid = "ds-123";

        let result: ShareLinkResp | undefined;
        const ds = {
            uid: uid,
        } as unknown as DigitalService;
        service.copyUrl(uid, ds, false).subscribe((res) => (result = res));

        const req = httpMock.expectOne(`${endpointDsVersions}/${uid}/share`);
        expect(req.request.method).toBe("GET");
    });

    it("validateShareToken should hit shared endpoint and pass boolean through", () => {
        const token = "tok-abc";
        const id = "ds-9";
        let value: boolean | undefined;

        service.validateShareToken(id, token).subscribe((res) => (value = res));

        const req = httpMock.expectOne(
            `${sharedEndpoint}/${token}/${dsSegment}/${id}/validate`,
        );
        expect(req.request.method).toBe("GET");
        req.flush(true);

        expect(value).toBeTrue();
    });

    it("getDs should fetch shared DS and push it to digitalService$ subject", (done) => {
        const token = "tok-xyz";
        const id = "ds-55";
        const ds: DigitalService = {
            uid: id,
        } as DigitalService;

        // First subscribe to digitalService$ to assert emission
        service.digitalService$.subscribe((emitted) => {
            expect(emitted.uid).toBe(id);
            done();
        });

        service.getDs(id, token).subscribe((res) => {
            expect(res.uid).toBe(id);
        });

        const req = httpMock.expectOne(`${sharedEndpoint}/${token}/${dsSegment}/${id}`);
        expect(req.request.method).toBe("GET");
        req.flush(ds);
    });

    it("getDs should fetch getDuplicateDigitalServiceAndVersionName", () => {
        const id = "ds-55";

        service.getDuplicateDigitalServiceAndVersionName(id).subscribe((res) => {
            expect(res.versionNames).toBe([]);
        });

        const req = httpMock.expectOne(
            `${endpointDsVersions}/${id}/validate-duplicate-names`,
        );
        expect(req.request.method).toBe("GET");
    });
});
