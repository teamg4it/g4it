import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AiModelConfig, DigitalServiceParameterIa } from '../../interfaces/digital-service/parameter.interfaces';
import { EcomindaiParameterDataService } from '../data/ecomindaiParameteri-data-service';

@Injectable({ providedIn: 'root' })
export class ParameterService {

constructor(private ecomindaiparameterdataserviceprivate:EcomindaiParameterDataService) {}
  
  getModels(model:string): Observable<AiModelConfig[]> {
      return this.ecomindaiparameterdataserviceprivate.getModels(model);
  }
}
