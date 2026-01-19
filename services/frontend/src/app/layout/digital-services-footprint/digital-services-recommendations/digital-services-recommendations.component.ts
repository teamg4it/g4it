import { Component } from '@angular/core';
import { UserService } from 'src/app/core/service/business/user.service';
import { of } from 'rxjs';

@Component({
  selector: 'app-digital-services-recommendations',
  templateUrl: './digital-services-recommendations.component.html',
  providers: [
    {
      provide: UserService,
      useValue: {
        isAllowedDigitalServiceWrite$: of(false), 
      },
    },
  ],
})

export class DigitalServicesRecommendationsComponent {

  headerFields = [
    'title',
    'category',
    'description',
    'globalReduction',
    
  ];

  recommendations = [
    { 
      title: 'Titre de la recommendation', 
      category: 'Terminaux', 
      globalReduction: "00%", 
      description: 'Déscription de la recommendation', 
      selected: false 
    },
    { 
      title: 'Titre de la recommendation', 
      category: 'Réseaux', 
      globalReduction: "00%", 
      description: 'Déscription de la recommendation', 
      selected: false 
    },
    { 
      title: 'Titre de la recommendation', 
      category: 'Infrastructures Privées', 
      globalReduction: "00%", 
      description: 'Déscription de la recommendation', 
      selected: false 
    },
    { 
      title: 'Titre de la recommendation', 
      category: 'Clouds Publics - IaaS', 
      globalReduction: "00%", 
      description: 'Déscription de la recommendation', 
      selected: false 
    },
    { 
      title: 'Titre de la recommendation', 
      category: 'Infrastructures Privées', 
      globalReduction: "00%", 
      description: 'Déscription de la recommendation', 
      selected: false 
    },
  ];
compareSelected() {
  const selectedRecommendations = this.recommendations.filter(r => r.selected);
  console.log('Selected for comparison:', selectedRecommendations);
}


}