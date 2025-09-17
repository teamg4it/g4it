import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Constants } from "src/constants";
import {
    InDatacenterRest,
    InPhysicalEquipmentRest,
    InVirtualEquipmentRest,
} from "../../interfaces/input.interface";
import {
    OutPhysicalEquipmentRest,
    OutVirtualEquipmentRest,
} from "../../interfaces/output.interface";
import { ShareDigitalServiceDataService } from "./share-digital-service-data.service";

describe("ShareDigitalServiceDataService", () => {
    let service: ShareDigitalServiceDataService;
    let httpMock: HttpTestingController;

    const sharedEndpoint = Constants.ENDPOINTS.sharedDs;
    const dsSegment = Constants.ENDPOINTS.ds;
    const token = "share-token-123";
    const uid = "ds-999";

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [ShareDigitalServiceDataService],
        });
        service = TestBed.inject(ShareDigitalServiceDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it("getSharedPhysicalEquipments should call correct URL", () => {
        const mockResp: InPhysicalEquipmentRest[] = [{ id: 1 } as any];
        service.getSharedPhysicalEquipments(uid, token).subscribe((res) => {
            expect(res).toEqual(mockResp);
        });

        const req = httpMock.expectOne(
            `${sharedEndpoint}/${token}/${dsSegment}/${uid}/inputs/physical-equipments`,
        );
        expect(req.request.method).toBe("GET");
        req.flush(mockResp);
    });

    it("getSharedVirtualEquipments should call correct URL", () => {
        const mockResp: InVirtualEquipmentRest[] = [{ id: 2 } as any];
        service.getSharedVirtualEquipments(uid, token).subscribe((res) => {
            expect(res).toEqual(mockResp);
        });

        const req = httpMock.expectOne(
            `${sharedEndpoint}/${token}/${dsSegment}/${uid}/inputs/virtual-equipments`,
        );
        expect(req.request.method).toBe("GET");
        req.flush(mockResp);
    });

    it("getReferentialData should call correct URL", () => {
        const mockResp = { networkTypes: [] };
        service.getReferentialData(uid, token).subscribe((res) => {
            expect(res).toEqual(mockResp);
        });

        const req = httpMock.expectOne(
            `${sharedEndpoint}/${token}/${dsSegment}/${uid}/referential-data`,
        );
        expect(req.request.method).toBe("GET");
        req.flush(mockResp);
    });

    it("getInSharedDataCenters should call correct URL", () => {
        const mockResp: InDatacenterRest[] = [{ name: "DC1" } as any];
        service.getInSharedDataCenters(uid, token).subscribe((res) => {
            expect(res).toEqual(mockResp);
        });

        const req = httpMock.expectOne(
            `${sharedEndpoint}/${token}/${dsSegment}/${uid}/inputs/datacenters`,
        );
        expect(req.request.method).toBe("GET");
        req.flush(mockResp);
    });

    it("getOutSharedPhysicalEquipments should call correct URL", () => {
        const mockResp: OutPhysicalEquipmentRest[] = [{ id: "OPE1" } as any];
        service.getOutSharedPhysicalEquipments(uid, token).subscribe((res) => {
            expect(res).toEqual(mockResp);
        });

        const req = httpMock.expectOne(
            `${sharedEndpoint}/${token}/${dsSegment}/${uid}/outputs/physical-equipments`,
        );
        expect(req.request.method).toBe("GET");
        req.flush(mockResp);
    });

    it("getOutSharedVirtualEquipments should call correct URL", () => {
        const mockResp: OutVirtualEquipmentRest[] = [{ id: "OVE1" } as any];
        service.getOutSharedVirtualEquipments(uid, token).subscribe((res) => {
            expect(res).toEqual(mockResp);
        });

        const req = httpMock.expectOne(
            `${sharedEndpoint}/${token}/${dsSegment}/${uid}/outputs/virtual-equipments`,
        );
        expect(req.request.method).toBe("GET");
        req.flush(mockResp);
    });

    it("getSharedPhysicalEquipments should handle null uid", () => {
        service.getSharedPhysicalEquipments(null, token).subscribe();
        httpMock
            .expectOne(
                `${sharedEndpoint}/${token}/${dsSegment}/null/inputs/physical-equipments`,
            )
            .flush([]);
    });
});
