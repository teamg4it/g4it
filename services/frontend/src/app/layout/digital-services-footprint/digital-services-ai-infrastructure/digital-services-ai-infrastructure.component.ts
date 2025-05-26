import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { Subscription } from 'rxjs';
import { AIFormsStore, AIInfrastructureForm } from 'src/app/core/store/ai-forms.store';

@Component({
  selector: 'app-digital-services-ai-infrastructure',
  templateUrl: './digital-services-ai-infrastructure.component.html',
})
export class DigitalServicesAiInfrastructureComponent implements OnInit, OnDestroy {
  infrastructureForm!: FormGroup;
  private formSubscription: Subscription | undefined;

  locationOptions = [
    { label: 'Germany', value: 'germany' },
    { label: 'France', value: 'france' },
    { label: 'United Kingdom', value: 'uk' },
    { label: 'United States', value: 'usa' }
  ];

  constructor(
    private fb: FormBuilder,
    private messageService: MessageService,
    private aiFormsStore: AIFormsStore
  ) {}

  ngOnInit(): void {
    this.infrastructureForm = this.fb.group({
      type: ['server', Validators.required],
      cpuCores: [30, [Validators.required, Validators.min(0)]],
      gpuCount: [2, [Validators.required, Validators.min(0)]],
      gpuMemory: [32, [Validators.required, Validators.min(0)]],
      ramSize: [64, [Validators.required, Validators.min(0)]],
      datacenterPue: [1.5, [Validators.required, Validators.min(0)]],
      complementaryPue: [1.3, [Validators.required, Validators.min(0)]],
      location: ['germany', Validators.required]
    });

    // Restaurer les données sauvegardées si elles existent
    const savedData = this.aiFormsStore.getInfrastructureFormData();
    if (savedData) {
      this.infrastructureForm.patchValue(savedData);
    }

    // Sauvegarder les données à chaque changement
    this.formSubscription = this.infrastructureForm.valueChanges.subscribe(value => {
      this.aiFormsStore.setInfrastructureFormData(value as AIInfrastructureForm);
    });
  }

  ngOnDestroy(): void {
    if (this.formSubscription) {
      this.formSubscription.unsubscribe();
    }
  }

  submitFormData(): void {
    if (this.infrastructureForm.invalid) {
      this.infrastructureForm.markAllAsTouched();
      return;
    }

    const formValue = this.infrastructureForm.value;
    console.log('🏗️ Infrastructure AI - Données envoyées au backend :', JSON.stringify(formValue, null, 2));

    this.messageService.add({
      severity: 'success',
      summary: 'Succès',
      detail: 'Infrastructure sauvegardée avec succès.'
    });
  }
} 