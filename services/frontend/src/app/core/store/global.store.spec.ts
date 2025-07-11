import { GlobalStoreService } from "./global.store";

describe("GlobalStoreService", () => {
    let service: GlobalStoreService;

    beforeEach(() => {
        service = new GlobalStoreService();
    });

    it("should update isMobileSignal to true when setIsMobile is called with true", () => {
        service.setIsMobile(true);
        expect(service.mobileView()).toBe(true);
    });

    it("should update isMobileSignal to false when setIsMobile is called with false", () => {
        service.setIsMobile(false);
        expect(service.mobileView()).toBe(false);
    });
});
