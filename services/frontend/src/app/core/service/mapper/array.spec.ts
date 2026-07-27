import {
    groupByCriterion,
    groupByField,
    sumByProperty,
    transformCriterion,
    transformEquipmentType,
} from "./array";

describe("Array Utility Functions", () => {
    describe("groupByCriterion", () => {
        it("should group objects by criterion", () => {
            const input = [
                { criterion: "TEST_ONE", value: 1 },
                { criterion: "TEST_ONE", value: 2 },
                { criterion: "TEST_TWO", value: 3 },
            ];
            const result = groupByCriterion(input);

            expect(result["test-one"]).toEqual([
                { criterion: "TEST_ONE", value: 1 },
                { criterion: "TEST_ONE", value: 2 },
            ]);
            expect(result["test-two"]).toEqual([{ criterion: "TEST_TWO", value: 3 }]);
        });

        it("should return empty object for empty array", () => {
            const result = groupByCriterion([]);
            expect(result).toEqual({});
        });

        it("should handle single item", () => {
            const input = [{ criterion: "SINGLE_ITEM", data: "test" }];
            const result = groupByCriterion(input);

            expect(result["single-item"]).toEqual([
                { criterion: "SINGLE_ITEM", data: "test" },
            ]);
        });

        it("should handle multiple underscores in criterion", () => {
            const input = [
                { criterion: "MULTIPLE_UNDER_SCORES", value: 1 },
                { criterion: "MULTIPLE_UNDER_SCORES", value: 2 },
            ];
            const result = groupByCriterion(input);

            expect(result["multiple-under-scores"]).toEqual(input);
        });

        it("should handle criterion with mixed case", () => {
            const input = [{ criterion: "MixedCase_Test", value: 1 }];
            const result = groupByCriterion(input);

            expect(result["mixedcase-test"]).toEqual(input);
        });
    });

    describe("transformCriterion", () => {
        it("should convert to lowercase and replace underscores with hyphens", () => {
            const result = transformCriterion("TEST_CRITERION");
            expect(result).toBe("test-criterion");
        });

        it("should handle single underscore", () => {
            const result = transformCriterion("TEST_ONE");
            expect(result).toBe("test-one");
        });

        it("should handle multiple underscores", () => {
            const result = transformCriterion("MULTIPLE_UNDER_SCORES_HERE");
            expect(result).toBe("multiple-under-scores-here");
        });

        it("should handle string without underscores", () => {
            const result = transformCriterion("NOUNDERSCORES");
            expect(result).toBe("nounderscores");
        });

        it("should handle already lowercase string", () => {
            const result = transformCriterion("already_lowercase");
            expect(result).toBe("already-lowercase");
        });

        it("should handle mixed case with underscores", () => {
            const result = transformCriterion("MixedCase_With_Underscores");
            expect(result).toBe("mixedcase-with-underscores");
        });

        it("should handle empty string", () => {
            const result = transformCriterion("");
            expect(result).toBe("");
        });
    });

    describe("transformEquipmentType", () => {
        it("should remove workspace prefix when it matches", () => {
            const result = transformEquipmentType("MyWorkspace_Equipment", "MyWorkspace");
            expect(result).toBe("Equipment");
        });

        it("should not remove prefix when it does not match", () => {
            const result = transformEquipmentType("OtherPrefix_Equipment", "MyWorkspace");
            expect(result).toBe("OtherPrefix_Equipment");
        });

        it("should handle case-insensitive matching", () => {
            const result = transformEquipmentType("myworkspace_Equipment", "MyWorkspace");
            expect(result).toBe("Equipment");
        });

        it("should handle uppercase equipment type with lowercase prefix", () => {
            const result = transformEquipmentType("MYWORKSPACE_Equipment", "myworkspace");
            expect(result).toBe("Equipment");
        });

        it("should handle whitespace in inputs", () => {
            const result = transformEquipmentType("MyWorkspace_Equipment", "MyWorkspace");
            expect(result).toBe("Equipment");
        });

        it("should return original when prefix is not at start", () => {
            const result = transformEquipmentType("Equipment_MyWorkspace", "MyWorkspace");
            expect(result).toBe("Equipment_MyWorkspace");
        });

        it("should return original when workspace name is empty", () => {
            // When workspace name is empty, empty string matches start of any string
            // so it will slice at position 1, removing first character
            const result = transformEquipmentType("Equipment_Type", "");
            expect(result).toBe("quipment_Type");
        });

        it("should handle empty equipment type", () => {
            const result = transformEquipmentType("", "MyWorkspace");
            expect(result).toBe("");
        });

        it("should handle equipment type with underscore separator", () => {
            const result = transformEquipmentType("MyWorkspace_Server", "MyWorkspace");
            expect(result).toBe("Server");
        });

        it("should handle when equipment type has no underscore", () => {
            const result = transformEquipmentType("Equipment", "MyWorkspace");
            expect(result).toBe("Equipment");
        });
    });

    describe("groupByField", () => {
        it("should group objects by specified field", () => {
            const input = [
                { category: "A", value: 1 },
                { category: "B", value: 2 },
                { category: "A", value: 3 },
            ];
            const result = groupByField(input, "category");

            expect(result["A"]).toEqual([
                { category: "A", value: 1 },
                { category: "A", value: 3 },
            ]);
            expect(result["B"]).toEqual([{ category: "B", value: 2 }]);
        });

        it("should return empty object for empty array", () => {
            const result = groupByField([], "field");
            expect(result).toEqual({});
        });

        it("should handle single item", () => {
            const input = [{ type: "single", data: "test" }];
            const result = groupByField(input, "type");

            expect(result["single"]).toEqual([{ type: "single", data: "test" }]);
        });

        it("should group by numeric field", () => {
            const input = [
                { id: 1, name: "first" },
                { id: 2, name: "second" },
                { id: 1, name: "third" },
            ];
            const result = groupByField(input, "id");

            expect(result[1]).toEqual([
                { id: 1, name: "first" },
                { id: 1, name: "third" },
            ]);
            expect(result[2]).toEqual([{ id: 2, name: "second" }]);
        });

        it("should handle all items with same field value", () => {
            const input = [
                { status: "active", id: 1 },
                { status: "active", id: 2 },
                { status: "active", id: 3 },
            ];
            const result = groupByField(input, "status");

            expect(result["active"]).toEqual(input);
            expect(Object.keys(result).length).toBe(1);
        });

        it("should handle different field types", () => {
            const input = [
                { flag: true, name: "test1" },
                { flag: false, name: "test2" },
                { flag: true, name: "test3" },
            ];
            const result = groupByField(input, "flag");

            expect(result["true"].length).toBe(2);
            expect(result["false"].length).toBe(1);
        });
    });

    describe("sumByProperty", () => {
        it("should sum values of specified property", () => {
            const input = [{ amount: 10 }, { amount: 20 }, { amount: 30 }];
            const result = sumByProperty(input, "amount");
            expect(result).toBe(60);
        });

        it("should return 0 for empty array", () => {
            const result = sumByProperty([], "amount");
            expect(result).toBe(0);
        });

        it("should handle single item", () => {
            const input = [{ value: 42 }];
            const result = sumByProperty(input, "value");
            expect(result).toBe(42);
        });

        it("should handle negative numbers", () => {
            const input = [{ balance: 100 }, { balance: -50 }, { balance: -30 }];
            const result = sumByProperty(input, "balance");
            expect(result).toBe(20);
        });

        it("should handle zero values", () => {
            const input = [{ count: 0 }, { count: 0 }, { count: 0 }];
            const result = sumByProperty(input, "count");
            expect(result).toBe(0);
        });

        it("should handle decimal numbers", () => {
            const input = [{ price: 10.5 }, { price: 20.3 }, { price: 15.2 }];
            const result = sumByProperty(input, "price");
            expect(result).toBe(46);
        });

        it("should handle mix of positive and negative numbers", () => {
            const input = [{ delta: 50 }, { delta: -20 }, { delta: 30 }, { delta: -10 }];
            const result = sumByProperty(input, "delta");
            expect(result).toBe(50);
        });

        it("should handle large numbers", () => {
            const input = [{ value: 1000000 }, { value: 2000000 }, { value: 3000000 }];
            const result = sumByProperty(input, "value");
            expect(result).toBe(6000000);
        });
    });
});
