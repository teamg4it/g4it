import { Constants } from "src/constants";

export const getErrorLabel = (value: string): string => {
    return `{redBold| \u24d8} {red|${value}}`;
};

export const getGreyLabel = (value: string): string => {
    return `{grey| ${value}}`;
};

export const getLabelFormatter = (
    hasError: boolean,
    enableDataInconsistency: boolean,
    value: string,
): string => {
    if (hasError && enableDataInconsistency) {
        return getErrorLabel(value);
    }
    return getGreyLabel(value);
};

export const getColorFormatter = (
    hasError: boolean,
    enableDataInconsistency: boolean,
): string => {
    if (hasError || enableDataInconsistency) {
        return Constants.GRAPH_RED;
    }
    return Constants.GRAPH_GREY;
};
