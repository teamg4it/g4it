import { Component, computed, DestroyRef, inject } from '@angular/core';
import { UserService } from 'src/app/core/service/business/user.service';
import { filter, of, switchMap, tap } from 'rxjs';
import { GlobalStoreService } from 'src/app/core/store/global.store';
import { DigitalServiceStoreService } from 'src/app/core/store/digital-service.store';
import { RecommendationService } from 'src/app/core/service/data/recommendations-data-service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-digital-services-recommendations',
  templateUrl: './digital-services-recommendations.component.html',

})

export class DigitalServicesRecommendationsComponent {
  
  protected readonly global = inject(GlobalStoreService);
  private userService = inject(UserService);
  private readonly destroyRef = inject(DestroyRef);
  private recommendationService = inject(RecommendationService);
  showApplyComponent = false;
  selectedRecommendationsForApply: any[] = [];
  isZoom125 = computed(() => this.global.zoomLevel() >= 125);

  headerFields = [
    'priority',
    'title',
    'description',
    'category',
    'implementationDifficulty',
    
  ];

  public selectAll = false;

public onSelectAllChange(selectAll: boolean) {
  this.selectAll = selectAll;
  this.compareSelected();
}
recommendations: any[] = [];

ngOnInit() {
  console.log("LOG: Component init");


  this.userService.currentOrganization$
    .pipe(
      takeUntilDestroyed(this.destroyRef),
      tap(org => console.log("LOG: Organisation reçue:", org)),
      filter(org => {
        const valid = !!org?.id;
        if (!valid) {
          console.warn("LOG: Organisation invalide ou sans id:", org);
        }
        return valid;
      }),
      tap(org => console.log("LOG: Organisation valide, id =", org.id)),
      switchMap(org => {
<<<<<<< HEAD
        const workspace = org.workspaces?.[0]?.id; // ou celui sélectionné
        return this.recommendationService.getByOrganisation(org.name, workspace);
=======
        const dsVersionUid = this.digitalServiceStore.digitalService().uid;
        console.log("LOG: Appel API instantiated recommendations avec orgId =", org.id, "dsVersionUid =", dsVersionUid);
        return this.recommendationService.getInstantiatedRecommendations(org.id, dsVersionUid);
>>>>>>> c2168b96 (Starting TOPSIS implementation with a static approach : only difficulty and baseImpact are currently used to compute priority)
      }),
      tap(data => console.log("LOG: Données reçues du backend:", data))
    )
    .subscribe({
      next: (data) => {
        this.recommendations = data.map(r => ({
          ...r.recommendation,
          priority: +(r.priority * 100).toFixed(1),
          category: this.mapCategory(r.recommendation?.category ?? []),
          selected: false,
          implementationDifficulty: r.recommendation?.difficulty ? this.mapDifficulty(r.recommendation.difficulty) : "N/A"
        }));

        console.log("LOG: Recommendations transformées:", this.recommendations);
      },
      error: (err) => {
        console.error("LOG: Erreur lors de la récupération des recommandations:", err);
      }
    });
}

private mapCategory(category: string[]): string[] {
  const mapping: any = {
    PUBLIC_CLOUD: "Clouds Publics - IaaS",
    PRIVATE_INFRASTRUCTURE: "Infrastructure Privée",
    NETWORK: "Réseaux",
    TERMINAL: "Terminaux"
  };

  return category?.map(c => mapping[c] || c);
}


private mapDifficulty(difficulty: string): string {
  const mapping: any = {
    HARD: "Difficile",
    MEDIUM: "Moyenne",
    EASY: "Facile",
  };
  return mapping[difficulty] ?? difficulty;
}

  // recommendations = [
  //   { 
  //     title: 'Utiliser une architecture adaptée', 
  //     priority:1,
  //     category: ['Clouds Publics - IaaS'], 
  //     implementationDifficulty: "Facile", 
  //     description: 'L’objectif est d’éviter une architecture surdimensionnée et de privilégier une architecture capable d\'ajuster dynamiquement la quantité de ressources utilisées en fonction de la demande du service, et passer à l’échelle. Cela contribue à optimiser l\'efficacité énergétique et à éviter le gaspillage de ressources inutiles.', 
  //     selected: false 
  //   },
  //   { 
  //     title: 'Limiter le poids et le nombre de requêtes par écran', 
  //     priority:2,
  //     category:  ['Réseaux'], 
  //     implementationDifficulty: "Facile", 
  //     description: 'Réduire ou limiter les données téléchargées.', 
  //     selected: false 
  //   },
  //   { 
  //     title: 'Minimiser le PUE de l\'hébergement', 
  //     priority:3,
  //     category: ['Infrastructure Privée'], 
  //     implementationDifficulty: "Moyen", 
  //     description: 'Il s’agit de connaître le PUE de son hébergement et favoriser la réduction de la consommation d’énergie nécessaire au bon fonctionnement et au refroidissement des serveurs nécessaires à l’hébergement.', 
  //     selected: false 
  //   },
  //   { 
  //     title: 'Choisir un hébergement cloud géographiquement cohérent ', 
  //     priority:4,
  //     category: ['Clouds Publics - IaaS', 'Infrastructure Privée'], 
  //     implementationDifficulty: "Difficile", 
  //     description: 'L’objectif est d’abord de privilégier un hébergement dans le pays où l’intensité carbone est peu élevée, et secondairement, dans une région où se situent la majorité des clients, afin de réduire la distance parcourue par les données et donc réduire l’infrastructure réseau mobilisée et son empreinte environnementale.', 
  //     selected: false 
  //   },
  //     { 
  //   title: 'Optimiser le parcours utilisateur', 
  //   priority:5,
  //   category: ['Terminaux'], 
  //   implementationDifficulty: "Facile", 
  //   description: 'Le service numérique doit s’assurer que chaque fonctionnalité principale est accessible et utilisable de manière fluide, afin de réduire les frictions pour l’utilisateur, améliorer l’expérience globale et limiter les surcharges inutiles sur le terminal.', 
  //   selected: false 
  // },
  // ];
compareSelected() {
  const selected = this.recommendations.filter(r => r.selected);
  this.selectedRecommendationsForApply = selected;
  this.showApplyComponent = selected.length > 0;
}
   digitalServiceStore = inject(DigitalServiceStoreService);


}