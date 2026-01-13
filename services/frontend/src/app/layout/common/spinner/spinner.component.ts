import { Component, inject, ViewEncapsulation } from "@angular/core";
import { GlobalStoreService } from "src/app/core/store/global.store";

@Component({
    selector: "app-spinner",
    templateUrl: "./spinner.component.html",
    styleUrls: [],
    encapsulation: ViewEncapsulation.None
})
export class SpinnerComponent {
    protected store = inject(GlobalStoreService);
}
