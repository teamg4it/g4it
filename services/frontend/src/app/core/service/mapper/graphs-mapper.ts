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

export const createStackBarGradientColor = (
    index: number,
    totalCount: number,
): string => {
    if (totalCount == 1) {
        return Constants.BLUE_COLOR;
    }
    const startColor = Constants.BLUE_COLOR;
    const endColor = Constants.YELLOW_COLOR;
    const t = index / (totalCount - 1);
    const startR = Number.parseInt(startColor.slice(1, 3), 16);
    const startG = Number.parseInt(startColor.slice(3, 5), 16);
    const startB = Number.parseInt(startColor.slice(5, 7), 16);
    const endR = Number.parseInt(endColor.slice(1, 3), 16);
    const endG = Number.parseInt(endColor.slice(3, 5), 16);
    const endB = Number.parseInt(endColor.slice(5, 7), 16);
    const r = Math.round((1 - t) * startR + t * endR);
    const g = Math.round((1 - t) * startG + t * endG);
    const b = Math.round((1 - t) * startB + t * endB);
    return `rgb(${r},${g},${b})`;
};
