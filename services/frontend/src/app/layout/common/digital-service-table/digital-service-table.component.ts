import { Component, computed, EventEmitter, inject, Input, Output } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { UserService } from "src/app/core/service/business/user.service";
import { GlobalStoreService } from "src/app/core/store/global.store";

@Component({
    selector: "app-digital-service-table",
    templateUrl: "./digital-service-table.component.html",
})
export class DigitalServiceTableComponent {
    protected userService = inject(UserService);
    protected translate = inject(TranslateService);
    private readonly globalStoreService = inject(GlobalStoreService);

    @Input() data: any[] = [];

    @Input() titleText = "";
    @Input() accessibilityText = "";
    @Input() translationPrefix = "";
    @Input() headerFields: string[] = [];
    @Input() showId = true;
    @Input() addButtonId = "add-button";

    @Output() sidebar: EventEmitter<boolean> = new EventEmitter();
    @Output() resetItem: EventEmitter<boolean> = new EventEmitter();
    @Output() setItem: EventEmitter<any> = new EventEmitter();
    @Output() deleteItem: EventEmitter<any> = new EventEmitter();

    isMobileView = computed(() => this.globalStoreService.mobileView());

    doResetItem() {
        this.resetItem.emit(true);
    }

    sidebarVisible(isVisible: boolean) {
        this.sidebar.emit(isVisible);
    }

    doSetItem(item: any, index: number) {
        const el = {
            index,
            ...item,
        };
        this.setItem.emit(el);
    }

    doDeleteItem(item: any, index: number) {
        this.deleteItem.emit({
            ...item,
            index,
        });
    }
}
