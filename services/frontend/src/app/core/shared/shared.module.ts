/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { RouterModule } from "@angular/router";
import { TranslateModule } from "@ngx-translate/core";
import { ClipboardModule } from "ngx-clipboard";
import { NgxEchartsModule } from "ngx-echarts";
import { AccordionModule } from "primeng/accordion";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { CheckboxModule } from "primeng/checkbox";
import { ConfirmDialogModule } from "primeng/confirmdialog";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { DialogModule } from "primeng/dialog";
import { DropdownModule } from "primeng/dropdown";
import { EditorModule } from "primeng/editor";
import { FocusTrapModule } from "primeng/focustrap";
import { InputTextModule } from "primeng/inputtext";
import { OverlayModule } from "primeng/overlay";
import { PaginatorModule } from "primeng/paginator";
import { RadioButtonModule } from "primeng/radiobutton";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { SidebarModule } from "primeng/sidebar";
import { TableModule } from "primeng/table";
import { TabMenuModule } from "primeng/tabmenu";
import { TabViewModule } from "primeng/tabview";
import { ToastModule } from "primeng/toast";
import { TooltipModule } from "primeng/tooltip";
import { CommonEditorComponent } from "src/app/layout/common/common-editor/common-editor.component";
import { CriteriaPopupComponent } from "src/app/layout/common/criteria-popup/criteria-popup.component";
import { InformationCardComponent } from "src/app/layout/common/information-card/information-card.component";
import { SpinnerComponent } from "src/app/layout/common/spinner/spinner.component";
import { StackBarChartComponent } from "src/app/layout/common/stack-bar-chart/stack-bar-chart.component";
import { StatsComponent } from "src/app/layout/common/stats/stats.component";
import { WorkspaceComponent } from "src/app/layout/common/workspace/workspace.component";
import { DatavizFilterComponent } from "src/app/layout/inventories-footprint/dataviz-filter/dataviz-filter.component";
import { InventoriesHeaderFootprintComponent } from "src/app/layout/inventories-footprint/header/inventories-header-footprint.component";
import { AutofocusDirective } from "../directives/auto-focus.directive";
import { BusinessHoursRendererPipe } from "../pipes/business-hours-renderer.pipe";
import { DecimalsPipe } from "../pipes/decimal.pipe";
import { IntegerPipe } from "../pipes/integer.pipe";
import { MonthYearPipe } from "../pipes/monthyear.pipe";

@NgModule({
    declarations: [
        SpinnerComponent,
        MonthYearPipe,
        DecimalsPipe,
        IntegerPipe,
        InformationCardComponent,
        InventoriesHeaderFootprintComponent,
        DatavizFilterComponent,
        CommonEditorComponent,
        BusinessHoursRendererPipe,
        StatsComponent,
        CriteriaPopupComponent,
        AutofocusDirective,
        StackBarChartComponent,
        WorkspaceComponent,
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        ReactiveFormsModule,
        TranslateModule,
        TooltipModule,
        ToastModule,
        CardModule,
        ScrollPanelModule,
        SidebarModule,
        RadioButtonModule,
        ConfirmPopupModule,
        ButtonModule,
        CheckboxModule,
        TabViewModule,
        OverlayModule,
        EditorModule,
        DropdownModule,
        ConfirmDialogModule,
        TabMenuModule,
        PaginatorModule,
        AccordionModule,
        InputTextModule,
        ClipboardModule,
        NgxEchartsModule.forRoot({
            echarts: () => import("echarts"),
        }),
        DialogModule,
        FocusTrapModule,
    ],
    exports: [
        TooltipModule,
        MonthYearPipe,
        DecimalsPipe,
        IntegerPipe,
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        ToastModule,
        InformationCardComponent,
        CardModule,
        ScrollPanelModule,
        InventoriesHeaderFootprintComponent,
        ConfirmPopupModule,
        ButtonModule,
        DatavizFilterComponent,
        PaginatorModule,
        CheckboxModule,
        TabViewModule,
        OverlayModule,
        SidebarModule,
        RadioButtonModule,
        CommonEditorComponent,
        StatsComponent,
        DropdownModule,
        ConfirmDialogModule,
        TableModule,
        TabMenuModule,
        AccordionModule,
        BusinessHoursRendererPipe,
        InputTextModule,
        SpinnerComponent,
        ClipboardModule,
        CriteriaPopupComponent,
        AutofocusDirective,
        StackBarChartComponent,
        WorkspaceComponent,
    ],
    providers: [DecimalsPipe, IntegerPipe],
})
export class SharedModule {}
