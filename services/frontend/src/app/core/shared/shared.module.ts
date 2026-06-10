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
import { BadgeModule } from "primeng/badge";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { CheckboxModule } from "primeng/checkbox";
import { ConfirmDialogModule } from "primeng/confirmdialog";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { DialogModule } from "primeng/dialog";
import { DrawerModule } from "primeng/drawer";
import { EditorModule } from "primeng/editor";
import { FocusTrapModule } from "primeng/focustrap";
import { InputTextModule } from "primeng/inputtext";
import { MenubarModule } from "primeng/menubar";
import { OverlayModule } from "primeng/overlay";
import { PaginatorModule } from "primeng/paginator";
import { RadioButtonModule } from "primeng/radiobutton";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { SelectModule } from "primeng/select";
import { TableModule } from "primeng/table";
import { ToastModule } from "primeng/toast";
import { TooltipModule } from "primeng/tooltip";
import { CommonEditorComponent } from "src/app/layout/common/common-editor/common-editor.component";
import { CriteriaPopupComponent } from "src/app/layout/common/criteria-popup/criteria-popup.component";
import { FormNavComponent } from "src/app/layout/common/form-nav/form-nav.component";
import { ImpactSidebarComponent } from "src/app/layout/common/impact-sidebar/impact-sidebar.component";
import { PromoteVersionDialogComponent } from "src/app/layout/common/promote-version-dialog/promote-version-dialog.component";
import { SpinnerComponent } from "src/app/layout/common/spinner/spinner.component";
import { StackBarChartComponent } from "src/app/layout/common/stack-bar-chart/stack-bar-chart.component";
import { WorkspaceComponent } from "src/app/layout/common/workspace/workspace.component";
import { BaseFilterSidebarComponent } from "src/app/layout/inventories-footprint/base-filter-sidebar/base-filter-sidebar.component";
import { DatavizFilterComponent } from "src/app/layout/inventories-footprint/dataviz-filter/dataviz-filter.component";
import { InventoriesHeaderFootprintComponent } from "src/app/layout/inventories-footprint/header/inventories-header-footprint.component";
import { AutofocusDirective } from "../directives/auto-focus.directive";
import { BusinessHoursRendererPipe } from "../pipes/business-hours-renderer.pipe";
import { DecimalsPipe } from "../pipes/decimal.pipe";
import { IntegerPipe } from "../pipes/integer.pipe";
import { MonthYearPipe } from "../pipes/monthyear.pipe";

@NgModule({
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
        DrawerModule,
        RadioButtonModule,
        ConfirmPopupModule,
        ButtonModule,
        CheckboxModule,
        MenubarModule,
        OverlayModule,
        EditorModule,
        SelectModule,
        ConfirmDialogModule,
        PaginatorModule,
        AccordionModule,
        InputTextModule,
        ClipboardModule,
        NgxEchartsModule.forRoot({
            echarts: () => import("echarts"),
        }),
        DialogModule,
        FocusTrapModule,
        ImpactSidebarComponent,
        DecimalsPipe,
        IntegerPipe,
        BadgeModule,
        SpinnerComponent,
        MonthYearPipe,
        InventoriesHeaderFootprintComponent,
        BaseFilterSidebarComponent,
        DatavizFilterComponent,
        CommonEditorComponent,
        BusinessHoursRendererPipe,
        CriteriaPopupComponent,
        PromoteVersionDialogComponent,
        AutofocusDirective,
        StackBarChartComponent,
        WorkspaceComponent,
        FormNavComponent,
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
        CardModule,
        MenubarModule,
        ScrollPanelModule,
        InventoriesHeaderFootprintComponent,
        ConfirmPopupModule,
        ButtonModule,
        BaseFilterSidebarComponent,
        DatavizFilterComponent,
        PaginatorModule,
        CheckboxModule,
        OverlayModule,
        DrawerModule,
        RadioButtonModule,
        CommonEditorComponent,
        SelectModule,
        ConfirmDialogModule,
        TableModule,
        AccordionModule,
        BusinessHoursRendererPipe,
        InputTextModule,
        SpinnerComponent,
        ClipboardModule,
        CriteriaPopupComponent,
        PromoteVersionDialogComponent,
        AutofocusDirective,
        StackBarChartComponent,
        WorkspaceComponent,
        FormNavComponent,
        ImpactSidebarComponent,
        DialogModule,
    ],
    providers: [DecimalsPipe, IntegerPipe],
})
export class SharedModule {}
