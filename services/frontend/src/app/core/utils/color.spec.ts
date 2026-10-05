/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { chooseTextContrast, generateColor } from "./color";

describe("Color Utils", () => {
    describe("generateColor", () => {
        it("should return a valid hex color code for a given string", () => {
            const inputString = "example";

            const result = generateColor(inputString);

            expect(result).toMatch(/^#[0-9A-F]{6}$/i);
        });

        it("should return the same color for the same input string", () => {
            const inputString = "example";

            const result1 = generateColor(inputString);
            const result2 = generateColor(inputString);

            expect(result1).toEqual(result2);
        });

        it("should return different colors for different input strings", () => {
            const input1 = "test1";
            const input2 = "test2";

            const result1 = generateColor(input1);
            const result2 = generateColor(input2);

            expect(result1).not.toEqual(result2);
        });

        it("should handle empty string", () => {
            const result = generateColor("");

            expect(result).toMatch(/^#[0-9A-F]{6}$/i);
            expect(result).toBe("#000000");
        });

        it("should handle single character", () => {
            const result = generateColor("A");

            expect(result).toMatch(/^#[0-9A-F]{6}$/i);
        });

        it("should handle long strings", () => {
            const longString = "ThisIsAVeryLongStringToTestTheColorGenerationFunction";

            const result = generateColor(longString);

            expect(result).toMatch(/^#[0-9A-F]{6}$/i);
        });

        it("should handle special characters", () => {
            const specialChars = "!@#$%^&*()_+-={}[]|:;<>?,./";

            const result = generateColor(specialChars);

            expect(result).toMatch(/^#[0-9A-F]{6}$/i);
        });

        it("should handle unicode characters", () => {
            const unicode = "日本語テスト";

            const result = generateColor(unicode);

            expect(result).toMatch(/^#[0-9A-F]{6}$/i);
        });

        it("should handle numbers as strings", () => {
            const numberString = "123456789";

            const result = generateColor(numberString);

            expect(result).toMatch(/^#[0-9A-F]{6}$/i);
        });

        it("should pad color with leading zeros correctly", () => {
            // Test a string that would generate a short hash
            const result = generateColor("A");

            expect(result).toHaveSize(7); // # + 6 hex chars
            expect(result).toMatch(/^#[0-9A-F]{6}$/i);
        });
    });

    describe("chooseTextContrast", () => {
        it("should return black text for light background colors", () => {
            // White background
            expect(chooseTextContrast("#FFFFFF")).toBe("#000000");
            // Light gray
            expect(chooseTextContrast("#CCCCCC")).toBe("#000000");
            // Light yellow
            expect(chooseTextContrast("#FFFF00")).toBe("#000000");
            // Light cyan
            expect(chooseTextContrast("#00FFFF")).toBe("#000000");
        });

        it("should return white text for dark background colors", () => {
            // Black background
            expect(chooseTextContrast("#000000")).toBe("#FFFFFF");
            // Dark gray
            expect(chooseTextContrast("#333333")).toBe("#FFFFFF");
            // Dark blue
            expect(chooseTextContrast("#000080")).toBe("#FFFFFF");
            // Dark red
            expect(chooseTextContrast("#800000")).toBe("#FFFFFF");
        });

        it("should handle colors without # prefix", () => {
            // White without #
            expect(chooseTextContrast("FFFFFF")).toBe("#000000");
            // Black without #
            expect(chooseTextContrast("000000")).toBe("#FFFFFF");
        });

        it("should handle colors with # prefix", () => {
            // White with #
            expect(chooseTextContrast("#FFFFFF")).toBe("#000000");
            // Black with #
            expect(chooseTextContrast("#000000")).toBe("#FFFFFF");
        });

        it("should handle lowercase hex colors", () => {
            expect(chooseTextContrast("#ffffff")).toBe("#000000");
            expect(chooseTextContrast("#000000")).toBe("#FFFFFF");
        });

        it("should handle mixed case hex colors", () => {
            expect(chooseTextContrast("#FfFfFf")).toBe("#000000");
            expect(chooseTextContrast("#000000")).toBe("#FFFFFF");
        });

        it("should return black for medium-light colors", () => {
            // Light green
            expect(chooseTextContrast("#90EE90")).toBe("#000000");
            // Light blue
            expect(chooseTextContrast("#ADD8E6")).toBe("#000000");
        });

        it("should return white for medium-dark colors", () => {
            // Medium blue
            expect(chooseTextContrast("#0000CD")).toBe("#FFFFFF");
            // Medium purple
            expect(chooseTextContrast("#800080")).toBe("#FFFFFF");
        });

        it("should handle edge case colors near the threshold", () => {
            // Colors near the 0.179 luminance threshold
            const nearThreshold1 = "#7F7F7F";
            const nearThreshold2 = "#808080";

            const result1 = chooseTextContrast(nearThreshold1);
            const result2 = chooseTextContrast(nearThreshold2);

            expect(result1).toMatch(/^#(000000|FFFFFF)$/);
            expect(result2).toMatch(/^#(000000|FFFFFF)$/);
        });

        it("should handle primary colors correctly", () => {
            // Red
            expect(chooseTextContrast("#FF0000")).toBe("#000000");
            // Green
            expect(chooseTextContrast("#00FF00")).toBe("#000000");
            // Blue
            expect(chooseTextContrast("#0000FF")).toBe("#FFFFFF");
        });

        it("should handle common UI colors", () => {
            // Bootstrap primary (blue)
            expect(chooseTextContrast("#0d6efd")).toBe("#000000");
        });
    });
});
