import { Component } from "@angular/core";
import { DigitalServicesAiInfrastructureComponent } from "./digital-services-ai-infrastructure/digital-services-ai-infrastructure.component";
import { DigitalServicesAiParametersComponent } from "./digital-services-ai-parameters/digital-services-ai-parameters.component";

@Component({
    selector: "app-digital-services-ecomind-parameters",
    templateUrl: "./digital-services-ecomind-parameters.component.html",
    imports: [DigitalServicesAiInfrastructureComponent, DigitalServicesAiParametersComponent]
})
export class DigitalServicesEcomindParametersComponent {}
