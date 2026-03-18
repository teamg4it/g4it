/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, NgModule } from "@angular/core";
import { NgxEchartsModule } from "ngx-echarts";
import { InventoryUtilService } from "src/app/core/service/business/inventory-util.service";
import { InventoriesFootprintComponent } from "./inventories-footprint.component";
import { inventoriesFootprintRouter } from "./inventories-footprint.router";

@NgModule({
    imports: [
        NgxEchartsModule.forRoot({
            echarts: () =>
                import(
                    /* webpackChunkName: "echarts" */
                    /* webpackMode: "lazy" */
                    "src/app/core/shared/echarts.module"
                ).then((m) => m.default),
        }),
        inventoriesFootprintRouter,
        InventoriesFootprintComponent,
    ],
    providers: [InventoryUtilService],
    exports: [InventoriesFootprintComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
})
export class InventoriesFootprintModule {}
