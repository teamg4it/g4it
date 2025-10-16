import { Injectable, signal } from "@angular/core";
import { EcomindType } from "../interfaces/digital-service.interfaces";

export interface AIInfrastructureForm {
    infrastructureType: EcomindType;
    nbCpuCores: number;
    nbGpu: number;
    gpuMemory: number;
    ramSize: number;
    location: string;
    pue: number;
}

export interface AIParametersForm {
    modelName: string;
    nbParameters: string;
    framework: string;
    quantization: string;
    isInference: boolean;
    isFinetuning: boolean;
    numberUserYear: number;
    averageNumberRequest: number;
    averageNumberToken: number;
    totalGeneratedTokens: number;
}

@Injectable({
    providedIn: "root",
})
export class AIFormsStore {
    private readonly infrastructureFormData = signal<AIInfrastructureForm | null>(null);
    private readonly parametersFormData = signal<AIParametersForm | null>(null);
    private infrastructureChange = false;
    private parameterChange = false;

    setInfrastructureFormData(data: AIInfrastructureForm) {
        this.infrastructureFormData.set(data);
    }

    getInfrastructureFormData() {
        return this.infrastructureFormData();
    }

    setParametersFormData(data: AIParametersForm) {
        this.parametersFormData.set(data);
    }

    getParametersFormData() {
        return this.parametersFormData();
    }

    setInfrastructureChange(value: boolean) {
        this.infrastructureChange = value;
    }

    getInfrastructureChange() {
        return this.infrastructureChange;
    }

    setParameterChange(value: boolean) {
        this.parameterChange = value;
    }

    getParameterChange() {
        return this.parameterChange;
    }

    clearForms() {
        this.infrastructureFormData.set(null);
        this.parametersFormData.set(null);
    }
}
