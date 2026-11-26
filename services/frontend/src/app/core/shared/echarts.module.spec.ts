import echarts from "./echarts.module";

describe("Equipment chart registration", () => {
    it("renders a sunburst through the application registry without a test-only installer", () => {
        const chart = echarts.init(document.createElement("div"), undefined, {
            renderer: "canvas",
            width: 400,
            height: 300,
        });
        try {
            chart.setOption({
                animation: false,
                series: [{
                    type: "sunburst",
                    data: [{ name: "DEVICES", children: [{ name: "Laptop", value: 2 }] }],
                }],
            });
            expect((chart.getOption()["series"] as any[])[0].type).toBe("sunburst");
            expect((chart.getOption()["series"] as any[])[0].data[0].children).toEqual([
                { name: "Laptop", value: 2 },
            ]);
            expect(chart.getDataURL()).toMatch(/^data:image\/png/);
        } finally {
            chart.dispose();
        }
    });
});
