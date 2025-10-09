export function generateColor(str: string): string {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = str.codePointAt(i)! + ((hash << 5) - hash);
    }
    const color = (hash & 0x00ffffff).toString(16).toUpperCase();
    return "#" + "00000".substring(0, 6 - color.length) + color;
}

export function chooseTextContrast(bgColor: string): string {
    const color = bgColor.startsWith("#") ? bgColor.substring(1, 7) : bgColor;
    const r = Number.parseInt(color.substring(0, 2), 16); // hexToR
    const g = Number.parseInt(color.substring(2, 4), 16); // hexToG
    const b = Number.parseInt(color.substring(4, 6), 16); // hexToB
    const uicolors = [r / 255, g / 255, b / 255];
    const c = uicolors.map((col) => {
        if (col <= 0.03928) {
            return col / 12.92;
        }
        return Math.pow((col + 0.055) / 1.055, 2.4);
    });
    const L = 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2];
    return L > 0.179 ? "#000000" : "#FFFFFF";
}
