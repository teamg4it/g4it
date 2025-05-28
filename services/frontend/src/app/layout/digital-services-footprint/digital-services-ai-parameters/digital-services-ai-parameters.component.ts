import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AIFormsStore, AIParametersForm } from 'src/app/core/store/ai-forms.store';
import { DigitalServiceParameterIa } from '../../../core/interfaces/digital-service/parameter.interfaces';
import { ParameterService } from '../../../core/service/business/parameter.service';

@Component({
  selector: 'app-digital-services-ai-parameters',
  templateUrl: './digital-services-ai-parameters.component.html',
})
export class DigitalServicesAiParametersComponent implements OnInit, OnDestroy {
  terminalsForm!: FormGroup;
  private formSubscription: Subscription | undefined;

  model: string = 'LLM';
  models:any[] = [];
  modelOptions: any[] = [];
  parameterOptions: any[] = [];
  frameworkOptions: any[] =[];
  quantizationOptions: any[] = [];
  constructor(
    private fb: FormBuilder,
    private inferenceService: ParameterService,
    private aiFormsStore: AIFormsStore
  ) {}
  ngOnInit(): void {
    this.terminalsForm = this.fb.group({
      model: [null, Validators.required],
      parameter: [null, Validators.required],
      framework: [null, Validators.required],
      quantization: [null, Validators.required],
      inference: [true],
      finetuning: [false],
      numberOfUsers: [null, Validators.required],
      averageRequest: [null, Validators.required],
      averageToken: [null, Validators.required],
      totalTokenGenerate: [{ value: 0, disabled: true }]
    });
  
    this.inferenceService.getModels(this.model).subscribe({
      next: (data) => {
        this.models = data;
        this.modelOptions = Array.from(
          new Set(this.models.map(m => m.modelName))
        ).map(name => ({ label: name, value: name }));
        this.terminalsForm.get('model')?.valueChanges.subscribe((selectedModelName) => {
          const filtered = this.models.filter(m => m.modelName === selectedModelName);
          this.parameterOptions = Array.from(new Set(filtered.map(m => m.parameters)))
            .map(p => ({ label: p, value: p }));
          this.frameworkOptions = [];
          this.quantizationOptions = [];
          this.terminalsForm.patchValue({
            parameter: null,
            framework: null,
            quantization: null
          });
        });
  
        this.terminalsForm.get('parameter')?.valueChanges.subscribe((selectedParameter) => {
          const selectedModelName = this.terminalsForm.get('model')?.value;
          const filtered = this.models.filter(
            m => m.modelName === selectedModelName && m.parameters === selectedParameter
          );
          this.frameworkOptions = Array.from(new Set(filtered.map(m => m.framework)))
            .map(f => ({ label: f, value: f }));
          this.quantizationOptions = [];
          this.terminalsForm.patchValue({
            framework: null,
            quantization: null
          });
        });
  
        this.terminalsForm.get('framework')?.valueChanges.subscribe((selectedFramework) => {
          const selectedModelName = this.terminalsForm.get('model')?.value;
          const selectedParameter = this.terminalsForm.get('parameter')?.value;
          const filtered = this.models.filter(
            m => m.modelName === selectedModelName &&
                 m.parameters === selectedParameter &&
                 m.framework === selectedFramework
          );
  
          this.quantizationOptions = Array.from(new Set(filtered.map(m => m.quantization)))
            .map(q => ({ label: q, value: q }));
          this.terminalsForm.patchValue({
            quantization: null
          });
        });
      },
      error: (err) => {
        console.error('Erreur lors de la récupération des modèles IA:', err);
      }
    });
  
    const savedData = this.aiFormsStore.getParametersFormData();
    if (savedData) {
      this.terminalsForm.patchValue(savedData);
    }
    this.formSubscription = this.terminalsForm.valueChanges.subscribe(value => {
      this.aiFormsStore.setParametersFormData(value as AIParametersForm);
      this.updateTotalTokens();
    });
  }
  
  ngOnDestroy(): void {
    if (this.formSubscription) {
      this.formSubscription.unsubscribe();
    }
  }

  updateTotalTokens(): void {
    const u = this.terminalsForm.get('numberOfUsers')?.value || 0;
    const r = this.terminalsForm.get('averageRequest')?.value || 0;
    const t = this.terminalsForm.get('averageToken')?.value || 0;
    const total = u * r * t;
    this.terminalsForm.get('totalTokenGenerate')?.setValue(total);
  }

  submitFormData(): void {
    if (this.terminalsForm.invalid) {
      this.terminalsForm.markAllAsTouched();
      return;
    }
    const formValue: DigitalServiceParameterIa = {
      modelName: this.terminalsForm.value.model,
      nbParameters: this.terminalsForm.value.parameter,
      framework: this.terminalsForm.value.framework,
      quantization: this.terminalsForm.value.quantization,
      isInference: this.terminalsForm.value.inference,
      isFinetuning: this.terminalsForm.value.finetuning,
      numberUserYear: this.terminalsForm.value.numberOfUsers,
      averageNumberRequest: this.terminalsForm.value.averageRequest,
      averageNumberToken: this.terminalsForm.value.averageToken,
      totalGeneratedTokens: this.terminalsForm.getRawValue().totalTokenGenerate
    };
  }
}
