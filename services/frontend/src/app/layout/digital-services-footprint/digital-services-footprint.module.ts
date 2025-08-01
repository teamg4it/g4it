/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CommonModule } from "@angular/common";
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, NgModule } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { RouterModule } from "@angular/router";
import { DropdownModule } from "primeng/dropdown";
import { InplaceModule } from "primeng/inplace";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { RadioButtonModule } from "primeng/radiobutton";
import { SidebarModule } from "primeng/sidebar";
import { TableModule } from "primeng/table";
import { TabMenuModule } from "primeng/tabmenu";
import { TagModule } from "primeng/tag";

import { NgxEchartsModule } from "ngx-echarts";
import { CardModule } from "primeng/card";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { DividerModule } from "primeng/divider";
import { FileUploadModule } from "primeng/fileupload";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { SharedModule } from "src/app/core/shared/shared.module";
import { DigitalServiceTableComponent } from "../common/digital-service-table/digital-service-table.component";
import { DigitalServicesAiInfrastructureComponent } from "./digital-services-ai-infrastructure/digital-services-ai-infrastructure.component";
import { DigitalServicesAiParametersComponent } from "./digital-services-ai-parameters/digital-services-ai-parameters.component";
import { DigitalServicesCloudServicesSidePanelComponent } from "./digital-services-cloud-services/digital-services-cloud-services-side-panel/digital-services-cloud-services-side-panel.component";
import { DigitalServicesCloudServicesComponent } from "./digital-services-cloud-services/digital-services-cloud-services.component";
import { BarChartComponent } from "./digital-services-footprint-dashboard/bar-chart/bar-chart.component";
import { DigitalServicesFootprintDashboardComponent } from "./digital-services-footprint-dashboard/digital-services-footprint-dashboard.component";
import { ImpactButtonComponent } from "./digital-services-footprint-dashboard/impact-button/impact-button.component";
import { PieChartComponent } from "./digital-services-footprint-dashboard/pie-chart/pie-chart.component";
import { RadialChartComponent } from "./digital-services-footprint-dashboard/radial-chart/radial-chart.component";
import { SetViewPopupComponent } from "./digital-services-footprint-dashboard/set-view-popup/set-view-popup.component";
import { DigitalServicesFootprintHeaderComponent } from "./digital-services-footprint-header/digital-services-footprint-header.component";
import { DigitalServicesFootprintComponent } from "./digital-services-footprint.component";
import { digitalServicesFootprintRouter } from "./digital-services-footprint.router";
import { DigitalServicesImportComponent } from "./digital-services-import/digital-services-import.component";
import { MultiFileImportComponent } from "./digital-services-import/multi-file-import/multi-file-import.component";
import { DigitalServicesNetworksSidePanelComponent } from "./digital-services-networks/digital-services-networks-side-panel/digital-services-networks-side-panel.component";
import { DigitalServicesNetworksComponent } from "./digital-services-networks/digital-services-networks.component";
import { DigitalServicesResourcesComponent } from "./digital-services-resources/digital-services-resources.component";
import { DigitalServicesServersComponent } from "./digital-services-servers/digital-services-servers.component";
import PanelDatacenterComponent from "./digital-services-servers/side-panel/add-datacenter/datacenter.component";
import { PanelAddVmComponent } from "./digital-services-servers/side-panel/add-vm/add-vm.component";
import { PanelCreateServerComponent } from "./digital-services-servers/side-panel/create-server/create-server.component";
import { PanelListVmComponent } from "./digital-services-servers/side-panel/list-vm/list-vm.component";
import { PanelServerParametersComponent } from "./digital-services-servers/side-panel/server-parameters/server-parameters.component";
import { DigitalServicesTerminalsSidePanelComponent } from "./digital-services-terminals/digital-services-terminals-side-panel/digital-services-terminals-side-panel.component";
import { DigitalServicesTerminalsComponent } from "./digital-services-terminals/digital-services-terminals.component";

@NgModule({
    declarations: [
        DigitalServicesFootprintComponent,
        DigitalServicesFootprintDashboardComponent,
        DigitalServicesFootprintHeaderComponent,
        DigitalServicesTerminalsComponent,
        DigitalServicesNetworksComponent,
        DigitalServicesServersComponent,
        DigitalServicesCloudServicesComponent,
        SetViewPopupComponent,
        DigitalServiceTableComponent,
        ImpactButtonComponent,
        RadialChartComponent,
        PieChartComponent,
        BarChartComponent,
        DigitalServicesTerminalsSidePanelComponent,
        DigitalServicesNetworksSidePanelComponent,
        DigitalServicesCloudServicesSidePanelComponent,
        DigitalServicesResourcesComponent,
        PanelCreateServerComponent,
        PanelDatacenterComponent,
        PanelServerParametersComponent,
        PanelAddVmComponent,
        PanelListVmComponent,
        DigitalServicesAiParametersComponent,
        DigitalServicesAiInfrastructureComponent,
        DigitalServicesImportComponent,
        MultiFileImportComponent,
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        InplaceModule,
        RadioButtonModule,
        DropdownModule,
        InputNumberModule,
        InputTextModule,
        SharedModule,
        FileUploadModule,
        TabMenuModule,
        TableModule,
        TagModule,
        CardModule,
        ScrollPanelModule,
        SidebarModule,
        ConfirmPopupModule,
        NgxEchartsModule.forRoot({
            echarts: () => import("echarts"),
        }),
        TableModule,
        RadioButtonModule,
        DividerModule,
        digitalServicesFootprintRouter,
        TableModule,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    exports: [DigitalServicesFootprintComponent],
})
export class DigitalServicesFootprintModule {}
