import { Injectable, signal } from '@angular/core';

export interface AIInfrastructureForm {
  type: string;
  cpuCores: number;
  gpuCount: number;
  gpuMemory: number;
  ramSize: number;
  datacenterPue: number;
  complementaryPue: number;
  location: string;
}

export interface AIParametersForm {
  model: string;
  parameter: string;
  framework: string;
  quantization: string;
  inference: boolean;
  finetuning: boolean;
  numberOfUsers: number;
  averageRequest: number;
  averageToken: number;
  totalTokenGenerate: number;
}

@Injectable({
  providedIn: 'root'
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