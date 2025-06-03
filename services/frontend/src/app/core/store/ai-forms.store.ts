import { Injectable, signal } from "@angular/core";

export interface AIInfrastructureForm {
    infrastructureType: string;
    nbCpuCores: number;
    nbGpu: number;
    gpuMemory: number;
    ramSize: number;
    location: string;
    pue: number;
    complementaryPue: number;
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
    private infrastructureFormData = signal<AIInfrastructureForm | null>(null);
    private parametersFormData = signal<AIParametersForm | null>(null);

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

    clearForms() {
        this.infrastructureFormData.set(null);
        this.parametersFormData.set(null);
    }
}
