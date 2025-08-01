import { Constants } from "src/constants";
import {
    getColorFormatter,
    getErrorLabel,
    getGreyLabel,
    getLabelFormatter,
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
    });
});
