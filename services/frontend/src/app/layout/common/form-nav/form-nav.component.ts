import { Component, EventEmitter, Input, Output } from "@angular/core";
import { FormGroup } from "@angular/forms";

@Component({
    selector: "app-form-nav",
    templateUrl: "./form-nav.component.html",
    styleUrl: "./form-nav.component.scss",
})
export class FormNavComponent {
    @Input() spaceDetails: any;
    @Input() spaceForm!: FormGroup;
    @Output() tabSelected = new EventEmitter<number>();
    selectTab(index: number): void {
        this.tabSelected.emit(index);
    }

    handleKeydown(event: KeyboardEvent, index: number) {
        let nextIndex = index;
        if (event.key === "ArrowDown" || event.key === "ArrowRight") {
            nextIndex = index + 1;
            this.focusElement("space-menu-item-" + nextIndex);
        } else if (event.key === "ArrowUp" || event.key === "ArrowLeft") {
            nextIndex = index - 1;
            this.focusElement("space-menu-item-" + nextIndex);
        } else if (event.key === "Enter" || event.key === " ") {
            this.selectTab(nextIndex);
            event.preventDefault();
        }
    }

    focusElement(elm: string) {
        if (!document.getElementById(elm)?.classList.contains("disabled")) {
            document.getElementById(elm)?.focus();
        }
    }

    getValidMenu() {
        return this.spaceDetails["menu"].filter((menu: any) => {
            return menu.hidden !== true;
        });
    }
}
