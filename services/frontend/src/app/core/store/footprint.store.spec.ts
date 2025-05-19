import { TestBed } from "@angular/core/testing";
import { FootprintStoreService } from "./footprint.store";

describe("FootprintStoreService", () => {
    let service: FootprintStoreService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [FootprintStoreService],
        });
        service = TestBed.inject(FootprintStoreService);
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    describe("Equipment State", () => {
        it("should set and get unit", () => {
            const unit = "newUnit";
            service.setUnit(unit);
            expect(service.unit()).toBe(unit);
        });

        it("should set and get criteria", () => {
            const criteria = "newCriteria";
            service.setCriteria(criteria);
            expect(service.criteria()).toBe(criteria);
        });
    });

    describe("Application State", () => {
        it("should set and get application criteria", () => {
            const criteria = "newAppCriteria";
            service.setApplicationCriteria(criteria);
            expect(service.applicationCriteria()).toBe(criteria);
        });

        it("should set and get graph type", () => {
            const graph = "newGraph";
            service.setGraphType(graph);
            expect(service.appGraphType()).toBe(graph);
        });

        it("should set and get domain", () => {
            const domain = "newDomain";
            service.setDomain(domain);
            expect(service.appDomain()).toBe(domain);
        });

        it("should set and get sub-domain", () => {
            const subDomain = "newSubDomain";
            service.setSubDomain(subDomain);
            expect(service.appSubDomain()).toBe(subDomain);
        });

        it("should set and get application", () => {
            const application = "newApplication";
            service.setApplication(application);
            expect(service.appApplication()).toBe(application);
        });
    });
});
