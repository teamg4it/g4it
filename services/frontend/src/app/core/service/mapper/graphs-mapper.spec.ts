import { Constants } from "src/constants";
import {
    createStackBarGradientColor,
    getColorFormatter,
    getErrorLabel,
    getGreyLabel,
    getLabelFormatter,
    getUniqueColorFromText,
    resetColorMap,
} from "./graphs-mapper";

describe("graphs-mapper utility functions", () => {
    describe("getErrorLabel", () => {
        it("should return the error label format", () => {
            expect(getErrorLabel("Test")).toBe("{redBold| \u24d8} {red|Test}");
        });
    });

    describe("getGreyLabel", () => {
        it("should return the grey label format", () => {
            expect(getGreyLabel("Test")).toBe("{grey| Test}");
        });
    });

    describe("getLabelFormatter", () => {
        it("should return error label if hasError and enableDataInconsistency are true", () => {
            expect(getLabelFormatter(true, true, "Test")).toBe(
                "{redBold| \u24d8} {red|Test}",
            );
        });

        it("should return grey label if hasError is false", () => {
            expect(getLabelFormatter(false, true, "Test")).toBe("{grey| Test}");
        });

        it("should return grey label if enableDataInconsistency is false", () => {
            expect(getLabelFormatter(true, false, "Test")).toBe("{grey| Test}");
        });

        it("should return grey label if both are false", () => {
            expect(getLabelFormatter(false, false, "Test")).toBe("{grey| Test}");
        });
    });

    describe("getColorFormatter", () => {
        it("should return GRAPH_RED if hasError is true", () => {
            expect(getColorFormatter(true, false)).toBe(Constants.GRAPH_RED);
        });

        it("should return GRAPH_RED if enableDataInconsistency is true", () => {
            expect(getColorFormatter(false, true)).toBe(Constants.GRAPH_RED);
        });

        it("should return GRAPH_RED if both are true", () => {
            expect(getColorFormatter(true, true)).toBe(Constants.GRAPH_RED);
        });

        it("should return GRAPH_GREY if both are false", () => {
            expect(getColorFormatter(false, false)).toBe(Constants.GRAPH_GREY);
        });
    });

    describe("createStackBarGradientColor", () => {
        it("should return BLUE_COLOR when totalCount is 1", () => {
            expect(createStackBarGradientColor(0, 1)).toBe(Constants.BLUE_COLOR);
        });

        it("should return BLUE_COLOR for first item when totalCount > 1", () => {
            expect(createStackBarGradientColor(0, 5)).toBe("rgb(0,178,255)");
        });

        it("should return YELLOW_COLOR for last item when totalCount > 1", () => {
            expect(createStackBarGradientColor(4, 5)).toBe("rgb(255,189,0)");
        });

        it("should return intermediate color for middle items", () => {
            const result = createStackBarGradientColor(2, 5);
            expect(result).toMatch(/^rgb\(\d+,\d+,\d+\)$/);
            expect(result).toBe("rgb(128,184,128)");
        });

        it("should calculate gradient correctly for 2 items", () => {
            const first = createStackBarGradientColor(0, 2);
            const last = createStackBarGradientColor(1, 2);
            expect(first).toBe("rgb(0,178,255)");
            expect(last).toBe("rgb(255,189,0)");
        });
    });

    describe("resetColorMap", () => {
        it("should reset the color map and index", () => {
            // Populate the color map
            getUniqueColorFromText("item1");
            getUniqueColorFromText("item2");
            getUniqueColorFromText("item3");

            // Reset
            resetColorMap();

            // After reset, the same text should get the first color again
            const color1 = getUniqueColorFromText("item1");
            expect(color1).toBe(Constants.COLOR[0]);
        });
    });

    describe("getUniqueColorFromText", () => {
        beforeEach(() => {
            resetColorMap();
        });

        it("should return first color for empty text", () => {
            expect(getUniqueColorFromText("")).toBe(Constants.COLOR[0]);
        });

        it("should return consistent color for same text", () => {
            const color1 = getUniqueColorFromText("test");
            const color2 = getUniqueColorFromText("test");
            expect(color1).toBe(color2);
            expect(color1).toBe(Constants.COLOR[0]);
        });

        it("should return different colors for different texts", () => {
            const color1 = getUniqueColorFromText("text1");
            const color2 = getUniqueColorFromText("text2");
            const color3 = getUniqueColorFromText("text3");

            expect(color1).toBe(Constants.COLOR[0]);
            expect(color2).toBe(Constants.COLOR[1]);
            expect(color3).toBe(Constants.COLOR[2]);
        });

        it("should cycle through palette when texts exceed palette length", () => {
            const colors: string[] = [];
            const paletteLength = Constants.COLOR.length;

            // Request more colors than palette has
            for (let i = 0; i < paletteLength + 5; i++) {
                colors.push(getUniqueColorFromText(`text${i}`));
            }

            // Colors should cycle
            expect(colors[paletteLength]).toBe(Constants.COLOR[0]);
            expect(colors[paletteLength + 1]).toBe(Constants.COLOR[1]);
        });

        it("should use custom palette when provided", () => {
            const customPalette = ["#FF0000", "#00FF00", "#0000FF"];
            const color1 = getUniqueColorFromText("test1", customPalette);
            const color2 = getUniqueColorFromText("test2", customPalette);

            expect(color1).toBe("#FF0000");
            expect(color2).toBe("#00FF00");
        });

        it("should return first color of custom palette for empty text", () => {
            const customPalette = ["#FF0000", "#00FF00"];
            expect(getUniqueColorFromText("", customPalette)).toBe("#FF0000");
        });
    });
});
