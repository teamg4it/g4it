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
      title: 'Utiliser une architecture adaptée à la quantité de ressources utilisées à la consommation du service', 
      category: 'Clouds Publics - IaaS', 
      globalReduction: "00%", 
      description: 'L’objectif est d’éviter une architecture surdimensionnée et de privilégier une architecture capable d\'ajuster dynamiquement la quantité de ressources utilisées en fonction de la demande du service, et passer à l’échelle. Cela contribue à optimiser l\'efficacité énergétique et à éviter le gaspillage de ressources inutiles.', 
      selected: false 
    },
    { 
      title: 'S\'astreindre à un poids maximum et une limite de requêtes par écran', 
      category: 'Réseaux', 
      globalReduction: "00%", 
      description: 'Réduire ou limiter les données téléchargées.', 
      selected: false 
    },
    { 
      title: 'Utiliser un hébergement dont le PUE (Power Usage Effectiveness) est minimisé', 
      category: 'Clouds Publics - IaaS', 
      globalReduction: "00%", 
      description: 'Il s’agit de connaître le PUE de son hébergement et favoriser la réduction de la consommation d’énergie nécessaire au bon fonctionnement et au refroidissement des serveurs nécessaires à l’hébergement.', 
      selected: false 
    },
    { 
      title: 'Utiliser un hébergement dont la localisation géographique est cohérente avec ses activités et qui minimise son empreinte environnementale ', 
      category: 'Clouds Publics - IaaS', 
      globalReduction: "00%", 
      description: 'L’objectif est d’abord de privilégier un hébergement dans le pays où l’intensité carbone est peu élevée, et secondairement, dans une région où se situent la majorité des clients, afin de réduire la distance parcourue par les données et donc réduire l’infrastructure réseau mobilisée et son empreinte environnementale.', 
      selected: false 
    },
  ];
compareSelected() {
  const selectedRecommendations = this.recommendations.filter(r => r.selected);
  console.log('Selected for comparison:', selectedRecommendations);
}


}