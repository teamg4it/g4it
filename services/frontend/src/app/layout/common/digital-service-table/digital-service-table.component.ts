import { Component, EventEmitter, inject, Input, Output } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { UserService } from "src/app/core/service/business/user.service";

@Component({
    selector: "app-digital-service-table",
    templateUrl: "./digital-service-table.component.html",
})
export class DigitalServiceTableComponent {
    protected userService = inject(UserService);
    protected translate = inject(TranslateService);

    @Input() data: any[] = [];

    @Input() titleText = "";
    @Input() addButton = "";
    @Input() translationPrefix = "";
    @Input() headerFields: string[] = [];
    @Input() showId = true;

    @Output() sidebar: EventEmitter<boolean> = new EventEmitter();
    @Output() resetItem: EventEmitter<boolean> = new EventEmitter();
    @Output() setItem: EventEmitter<any> = new EventEmitter();
    @Output() deleteItem: EventEmitter<any> = new EventEmitter();

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
