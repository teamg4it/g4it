import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { ConfirmationService } from "primeng/api";
import { UserService } from "src/app/core/service/business/user.service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { DigitalServiceTableComponent } from "./digital-service-table.component";

describe("DigitalServiceTableComponent", () => {
    let component: DigitalServiceTableComponent;
    let fixture: ComponentFixture<DigitalServiceTableComponent>;
    let userService: jasmine.SpyObj<UserService>;
    let translateService: jasmine.SpyObj<TranslateService>;
    let confirmationService: jasmine.SpyObj<ConfirmationService>;
    let globalStoreService: GlobalStoreService;

    const mockData = [
        { id: 1, name: "Service 1", description: "Description 1" },
        { id: 2, name: "Service 2", description: "Description 2" },
        { id: 3, name: "Service 3", description: "Description 3" },
    ];

    beforeEach(async () => {
        const userServiceSpy = jasmine.createSpyObj("UserService", ["getCurrentUser"]);
        const translateServiceSpy = jasmine.createSpyObj("TranslateService", ["instant"]);
        const confirmationServiceSpy = jasmine.createSpyObj("ConfirmationService", [
            "confirm",
        ]);

        await TestBed.configureTestingModule({
            declarations: [DigitalServiceTableComponent],
            imports: [TranslateModule.forRoot()],
            providers: [
                { provide: UserService, useValue: userServiceSpy },
                { provide: TranslateService, useValue: translateServiceSpy },
                { provide: ConfirmationService, useValue: confirmationServiceSpy },
                GlobalStoreService,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServiceTableComponent);
        component = fixture.componentInstance;
        userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
        translateService = TestBed.inject(
            TranslateService,
        ) as jasmine.SpyObj<TranslateService>;
        confirmationService = TestBed.inject(ConfirmationService) as jasmine.SpyObj<ConfirmationService>;
        globalStoreService = TestBed.inject(GlobalStoreService);

        // Setup default translate mocks
        translateService.instant.and.returnValue("translated-text");
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    describe("Input properties", () => {
        it("should have default values", () => {
            expect(component.data).toEqual([]);
            expect(component.titleText).toBe("");
            expect(component.accessibilityText).toBe("");
            expect(component.translationPrefix).toBe("");
            expect(component.headerFields).toEqual([]);
            expect(component.showId).toBe(true);
            expect(component.addButtonId).toBe("add-button");
            expect(component.isVM).toBe(false);
        });

        it("should accept custom data input", () => {
            component.data = mockData;
            expect(component.data.length).toBe(3);
            expect(component.data[0].name).toBe("Service 1");
        });

        it("should accept custom titleText", () => {
            component.titleText = "Custom Title";
            expect(component.titleText).toBe("Custom Title");
        });

        it("should accept custom accessibilityText", () => {
            component.accessibilityText = "Custom Accessibility";
            expect(component.accessibilityText).toBe("Custom Accessibility");
        });

        it("should accept custom translationPrefix", () => {
            component.translationPrefix = "custom.prefix";
            expect(component.translationPrefix).toBe("custom.prefix");
        });

        it("should accept custom headerFields", () => {
            const headers = ["name", "description", "status"];
            component.headerFields = headers;
            expect(component.headerFields).toEqual(headers);
        });

        it("should accept custom showId value", () => {
            component.showId = false;
            expect(component.showId).toBe(false);
        });

        it("should accept custom addButtonId", () => {
            component.addButtonId = "custom-add-btn";
            expect(component.addButtonId).toBe("custom-add-btn");
        });

        it("should accept custom isVM value", () => {
            component.isVM = true;
            expect(component.isVM).toBe(true);
        });
    });

    describe("isMobileView computed signal", () => {
        it("should reflect the mobile view state from global store", () => {
            globalStoreService.setIsMobile(false);
            expect(component.isMobileView()).toBe(false);

            globalStoreService.setIsMobile(true);
            expect(component.isMobileView()).toBe(true);
        });

        it("should update when global store mobile state changes", () => {
            globalStoreService.setIsMobile(false);
            const initialValue = component.isMobileView();
            expect(initialValue).toBe(false);

            globalStoreService.setIsMobile(true);
            expect(component.isMobileView()).toBe(true);
            expect(component.isMobileView()).not.toBe(initialValue);
        });
    });

    describe("doResetItem", () => {
        it("should emit resetItem event with true", () => {
            spyOn(component.resetItem, "emit");

            component.doResetItem();

            expect(component.resetItem.emit).toHaveBeenCalledWith(true);
        });

        it("should emit resetItem event only once per call", () => {
            spyOn(component.resetItem, "emit");

            component.doResetItem();

            expect(component.resetItem.emit).toHaveBeenCalledTimes(1);
        });
    });

    describe("sidebarVisible", () => {
        it("should emit sidebar event with true when passed true", () => {
            spyOn(component.sidebar, "emit");

            component.sidebarVisible(true);

            expect(component.sidebar.emit).toHaveBeenCalledWith(true);
        });

        it("should emit sidebar event with false when passed false", () => {
            spyOn(component.sidebar, "emit");

            component.sidebarVisible(false);

            expect(component.sidebar.emit).toHaveBeenCalledWith(false);
        });

        it("should emit sidebar event only once per call", () => {
            spyOn(component.sidebar, "emit");

            component.sidebarVisible(true);

            expect(component.sidebar.emit).toHaveBeenCalledTimes(1);
        });
    });

    describe("doSetItem", () => {
        it("should emit setItem event with item and index", () => {
            spyOn(component.setItem, "emit");
            const item = { id: 1, name: "Test Item" };
            const index = 0;

            component.doSetItem(item, index);

            expect(component.setItem.emit).toHaveBeenCalledWith({
                index: 0,
                id: 1,
                name: "Test Item",
            });
        });

        it("should merge index with item properties", () => {
            spyOn(component.setItem, "emit");
            const item = { id: 2, name: "Item 2", status: "active" };
            const index = 5;

            component.doSetItem(item, index);

            expect(component.setItem.emit).toHaveBeenCalledWith({
                index: 5,
                id: 2,
                name: "Item 2",
                status: "active",
            });
        });

        it("should handle item with no properties", () => {
            spyOn(component.setItem, "emit");
            const item = {};
            const index = 1;

            component.doSetItem(item, index);

            expect(component.setItem.emit).toHaveBeenCalledWith({ index: 1 });
        });

        it("should handle negative index", () => {
            spyOn(component.setItem, "emit");
            const item = { id: 3, name: "Item 3" };
            const index = -1;

            component.doSetItem(item, index);

            expect(component.setItem.emit).toHaveBeenCalledWith({
                index: -1,
                id: 3,
                name: "Item 3",
            });
        });

        it("should handle zero index", () => {
            spyOn(component.setItem, "emit");
            const item = { id: 1, name: "First Item" };
            const index = 0;

            component.doSetItem(item, index);

            expect(component.setItem.emit).toHaveBeenCalledWith({
                index: 0,
                id: 1,
                name: "First Item",
            });
        });
    });

    describe("doDeleteItem", () => {
        it("should emit deleteItem event with item and index", () => {
            spyOn(component.deleteItem, "emit");
            const item = { id: 1, name: "Test Item" };
            const index = 0;

            component.doDeleteItem(item, index);

            expect(component.deleteItem.emit).toHaveBeenCalledWith({
                id: 1,
                name: "Test Item",
                index: 0,
            });
        });

        it("should merge index with item properties", () => {
            spyOn(component.deleteItem, "emit");
            const item = { id: 2, name: "Item 2", description: "Description" };
            const index = 3;

            component.doDeleteItem(item, index);

            expect(component.deleteItem.emit).toHaveBeenCalledWith({
                id: 2,
                name: "Item 2",
                description: "Description",
                index: 3,
            });
        });

        it("should handle item with no properties", () => {
            spyOn(component.deleteItem, "emit");
            const item = {};
            const index = 2;

            component.doDeleteItem(item, index);

            expect(component.deleteItem.emit).toHaveBeenCalledWith({ index: 2 });
        });

        it("should handle large index values", () => {
            spyOn(component.deleteItem, "emit");
            const item = { id: 100, name: "Item 100" };
            const index = 999;

            component.doDeleteItem(item, index);

            expect(component.deleteItem.emit).toHaveBeenCalledWith({
                id: 100,
                name: "Item 100",
                index: 999,
            });
        });
    });

    describe("confirmDelete", () => {
        let mockEvent: Event;
        let mockItem: any;
        let mockIndex: number;

        beforeEach(() => {
            mockEvent = new Event("click");
            Object.defineProperty(mockEvent, "target", {
                writable: false,
                value: document.createElement("button"),
            });
            mockItem = { id: 1, name: "Test Item" };
            mockIndex = 0;
        });

        it("should call doDeleteItem directly when isVM is true", () => {
            component.isVM = true;
            spyOn(component, "doDeleteItem");

            component.confirmDelete(mockEvent, mockItem, mockIndex);

            expect(component.doDeleteItem).toHaveBeenCalledWith(mockItem, mockIndex);
            expect(confirmationService.confirm).not.toHaveBeenCalled();
        });

        it("should not call doDeleteItem when confirmation is rejected", () => {
            component.isVM = false;
            spyOn(component, "doDeleteItem");

            component.confirmDelete(mockEvent, mockItem, mockIndex);

            // Only accepting would trigger doDeleteItem
            expect(component.doDeleteItem).not.toHaveBeenCalled();
        });

        it("should translate confirmation dialog labels", () => {
            component.isVM = false;
            translateService.instant.and.callFake((key: string) => `translated-${key}`);

            component.confirmDelete(mockEvent, mockItem, mockIndex);

            expect(translateService.instant).toHaveBeenCalledWith("common.yes");
            expect(translateService.instant).toHaveBeenCalledWith("common.no");
            expect(translateService.instant).toHaveBeenCalledWith(
                "common.resourcesPopup.delete-question",
            );
        });

        it("should handle VM items without confirmation", () => {
            component.isVM = true;
            spyOn(component.deleteItem, "emit");

            component.confirmDelete(mockEvent, mockItem, mockIndex);

            expect(component.deleteItem.emit).toHaveBeenCalledWith({
                id: 1,
                name: "Test Item",
                index: 0,
            });
        });

        it("should handle non-VM items with confirmation", () => {
            component.isVM = false;
            spyOn(component.deleteItem, "emit");

            component.confirmDelete(mockEvent, mockItem, mockIndex);

            // deleteItem should not be called until acceptance
            expect(component.deleteItem.emit).not.toHaveBeenCalled();
        });
    });

    describe("Output events", () => {
        it("should have sidebar output emitter", () => {
            expect(component.sidebar).toBeDefined();
        });

        it("should have resetItem output emitter", () => {
            expect(component.resetItem).toBeDefined();
        });

        it("should have setItem output emitter", () => {
            expect(component.setItem).toBeDefined();
        });

        it("should have deleteItem output emitter", () => {
            expect(component.deleteItem).toBeDefined();
        });
    });

    describe("Integration tests", () => {
        it("should handle complete workflow for editing item", () => {
            spyOn(component.sidebar, "emit");
            spyOn(component.setItem, "emit");
            const item = mockData[0];

            component.sidebarVisible(true);
            component.doSetItem(item, 0);

            expect(component.sidebar.emit).toHaveBeenCalledWith(true);
            expect(component.setItem.emit).toHaveBeenCalledWith({
                index: 0,
                ...item,
            });
        });

        it("should handle complete workflow for deleting VM item", () => {
            component.isVM = true;
            spyOn(component.deleteItem, "emit");
            const item = mockData[1];
            const mockEvent = new Event("click");

            component.confirmDelete(mockEvent, item, 1);

            expect(component.deleteItem.emit).toHaveBeenCalledWith({
                ...item,
                index: 1,
            });
        });

        it("should handle complete workflow for resetting and adding new item", () => {
            spyOn(component.resetItem, "emit");
            spyOn(component.sidebar, "emit");

            component.doResetItem();
            component.sidebarVisible(true);

            expect(component.resetItem.emit).toHaveBeenCalledWith(true);
            expect(component.sidebar.emit).toHaveBeenCalledWith(true);
        });

        it("should handle multiple items with different indices", () => {
            spyOn(component.setItem, "emit");

            mockData.forEach((item, index) => {
                component.doSetItem(item, index);
            });

            expect(component.setItem.emit).toHaveBeenCalledTimes(3);
            expect(component.setItem.emit).toHaveBeenCalledWith({
                index: 0,
                ...mockData[0],
            });
            expect(component.setItem.emit).toHaveBeenCalledWith({
                index: 1,
                ...mockData[1],
            });
            expect(component.setItem.emit).toHaveBeenCalledWith({
                index: 2,
                ...mockData[2],
            });
        });
    });
    describe("updateSelectAllState", () => {
    it("should set selectAll to false when data is empty", () => {
        component.data = [];
        component.selectAll = true;

        component.updateSelectAllState();

        expect(component.selectAll).toBeFalse();
    });

    it("should set selectAll to true when all items are selected", () => {
        component.data = [
            { selected: true },
            { selected: true }
        ] as any;

        component.updateSelectAllState();

        expect(component.selectAll).toBeTrue();
    });

    it("should set selectAll to false when not all items are selected", () => {
        component.data = [
            { selected: true },
            { selected: false }
        ] as any;

        component.updateSelectAllState();

        expect(component.selectAll).toBeFalse();
    });
});
describe("onSelectAllChange", () => {
    it("should update all items to selected = true", () => {
        component.data = [
            { selected: false },
            { selected: false }
        ] as any;

        component.selectAll = true;

        spyOn(component.selectAllChange, "emit");

        component.onSelectAllChange();

        expect(component.data.every(i => i.selected)).toBeTrue();
        expect(component.selectAllChange.emit).toHaveBeenCalledWith(true);
    });

    it("should update all items to selected = false", () => {
        component.data = [
            { selected: true },
            { selected: true }
        ] as any;

        component.selectAll = false;

        spyOn(component.selectAllChange, "emit");

        component.onSelectAllChange();

        expect(component.data.every(i => !i.selected)).toBeTrue();
        expect(component.selectAllChange.emit).toHaveBeenCalledWith(false);
    });

    it("should call updateSelectAllState after change", () => {
        component.data = [{ selected: false }] as any;
        component.selectAll = true;

        spyOn(component, "updateSelectAllState");

        component.onSelectAllChange();

        expect(component.updateSelectAllState).toHaveBeenCalled();
    });
});

});
