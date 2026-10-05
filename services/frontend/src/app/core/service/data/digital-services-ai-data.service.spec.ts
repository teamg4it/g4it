import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { DigitalServicesAiDataService } from "./digital-services-ai-data.service";

describe("DigitalServicesAiDataService", () => {
    let service: DigitalServicesAiDataService;
    let httpMock: HttpTestingController;
    const uid = "digital-service-id";

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [DigitalServicesAiDataService],
        });
        service = TestBed.inject(DigitalServicesAiDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it("should create", () => {
        expect(service).toBeTruthy();
    });

    it("should save existing AI infrastructure with PUT", () => {
        const infrastructure = { location: "France" };

        service.saveAiInfrastructure(uid, infrastructure).subscribe();
        httpMock
            .expectOne(`digital-service-version/${uid}/ai-infra-input`)
            .flush({ id: 1 });

        const request = httpMock.expectOne(
            `digital-service-version/${uid}/ai-infra-input`,
        );
        expect(request.request.method).toBe("PUT");
        expect(request.request.body).toEqual(infrastructure);
        expect(request.request.headers.get("content-type")).toBe("application/json");
        request.flush(infrastructure);
    });

    it("should save new AI infrastructure with POST after a 404", () => {
        const infrastructure = { location: "France" };

        service.saveAiInfrastructure(uid, infrastructure).subscribe();
        httpMock
            .expectOne(`digital-service-version/${uid}/ai-infra-input`)
            .flush(null, { status: 404, statusText: "Not Found" });

        const request = httpMock.expectOne(
            `digital-service-version/${uid}/ai-infra-input`,
        );
        expect(request.request.method).toBe("POST");
        expect(request.request.body).toEqual(infrastructure);
        request.flush(infrastructure);
    });

    it("should propagate non-404 errors when saving AI infrastructure", () => {
        let receivedStatus: number | undefined;

        service.saveAiInfrastructure(uid, {}).subscribe({
            error: (error) => (receivedStatus = error.status),
        });
        httpMock
            .expectOne(`digital-service-version/${uid}/ai-infra-input`)
            .flush(null, { status: 500, statusText: "Server Error" });

        expect(receivedStatus).toBe(500);
    });

    it("should save existing AI parameters with PUT", () => {
        const parameters = { model: "large" };

        service.saveAiParameters(uid, parameters).subscribe();
        httpMock
            .expectOne(`digital-service-version/${uid}/ai-parameter-input`)
            .flush({ id: 1 });

        const request = httpMock.expectOne(
            `digital-service-version/${uid}/ai-parameter-input`,
        );
        expect(request.request.method).toBe("PUT");
        expect(request.request.body).toEqual(parameters);
        expect(request.request.headers.get("content-type")).toBe("application/json");
        request.flush(parameters);
    });

    it("should save new AI parameters with POST after a 404", () => {
        const parameters = { model: "large" };

        service.saveAiParameters(uid, parameters).subscribe();
        httpMock
            .expectOne(`digital-service-version/${uid}/ai-parameter-input`)
            .flush(null, { status: 404, statusText: "Not Found" });

        const request = httpMock.expectOne(
            `digital-service-version/${uid}/ai-parameter-input`,
        );
        expect(request.request.method).toBe("POST");
        expect(request.request.body).toEqual(parameters);
        request.flush(parameters);
    });

    it("should propagate non-404 errors when saving AI parameters", () => {
        let receivedStatus: number | undefined;

        service.saveAiParameters(uid, {}).subscribe({
            error: (error) => (receivedStatus = error.status),
        });
        httpMock
            .expectOne(`digital-service-version/${uid}/ai-parameter-input`)
            .flush(null, { status: 500, statusText: "Server Error" });

        expect(receivedStatus).toBe(500);
    });

    it("should get the Boaviztapi country map", () => {
        service.getBoaviztapiCountryMap().subscribe();

        const request = httpMock.expectOne("referential/boaviztapi/countries");
        expect(request.request.method).toBe("GET");
        request.flush({ France: "FRA" });
    });

    it("should get AI recommendations, infrastructure, and parameters", () => {
        service.getAiRecommendations(uid).subscribe();
        service.getAiInfrastructure(uid).subscribe();
        service.getAiParameter(uid).subscribe();

        const recommendations = httpMock.expectOne(
            `digital-service-version/${uid}/outputs/ai-recomandation`,
        );
        const infrastructure = httpMock.expectOne(
            `digital-service-version/${uid}/ai-infra-input`,
        );
        const parameters = httpMock.expectOne(
            `digital-service-version/${uid}/ai-parameter-input`,
        );
        expect(recommendations.request.method).toBe("GET");
        expect(infrastructure.request.method).toBe("GET");
        expect(parameters.request.method).toBe("GET");
        recommendations.flush({});
        infrastructure.flush({});
        parameters.flush({});
    });

    it("should get the EcoMind referential", () => {
        service.getEcomindReferential().subscribe();

        const request = httpMock.expectOne("digital-services/ecomind-type");
        expect(request.request.method).toBe("GET");
        request.flush([]);
    });
});
