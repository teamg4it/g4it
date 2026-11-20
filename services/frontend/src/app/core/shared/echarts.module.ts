/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { BarChart, PieChart, SunburstChart } from "echarts/charts";
import {
    DataZoomComponent,
    GridComponent,
    LegendComponent,
    PolarComponent,
    TooltipComponent,
} from "echarts/components";
import * as echarts from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";

echarts.use([
    PieChart,
    BarChart,
    GridComponent,
    TooltipComponent,
    PolarComponent,
    LegendComponent,
    CanvasRenderer,
    DataZoomComponent,
    SunburstChart,
]);

export default echarts;
