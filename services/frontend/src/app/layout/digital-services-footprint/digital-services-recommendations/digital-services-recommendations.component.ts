import { Component, computed, DestroyRef, inject } from '@angular/core';
import { UserService } from 'src/app/core/service/business/user.service';
import { combineLatest, filter, of, switchMap, tap } from 'rxjs';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { GlobalStoreService } from 'src/app/core/store/global.store';
import { DigitalServiceStoreService } from 'src/app/core/store/digital-service.store';
import { RecommendationService, InstantiatedRecommendation } from 'src/app/core/service/data/recommendations-data-service';

@Component({
  selector: 'app-digital-services-recommendations',
  templateUrl: './digital-services-recommendations.component.html',

})

export class DigitalServicesRecommendationsComponent {
  
  protected readonly global = inject(GlobalStoreService);
  private userService = inject(UserService);
  private readonly destroyRef = inject(DestroyRef);
  private recommendationService = inject(RecommendationService);
  digitalServiceStore = inject(DigitalServiceStoreService);

  // toObservable must be called in injection context (field initializer is fine)
  private readonly digitalService$ = toObservable(this.digitalServiceStore.digitalService);

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

  combineLatest([
    this.userService.currentOrganization$,
    this.digitalService$,
  ])
    .pipe(
      takeUntilDestroyed(this.destroyRef),
      tap(([org, ds]) => console.log("LOG: Organisation reçue:", org, "| digitalService:", ds)),
      filter(([org, ds]) => {
        const valid = !!org?.id && !!ds?.uid;
        if (!valid) {
          console.warn("LOG: Organisation ou digitalService pas encore chargé:", org, ds);
        }
        return valid;
      }),
<<<<<<< HEAD
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
=======
      tap(([org, ds]) => console.log("LOG: Organisation valide, id =", org.id, "| dsVersionUid =", ds.uid)),
      switchMap(([org, ds]) => {
        console.log("LOG: Appel API instantiated recommendations avec orgId =", org.id, "dsVersionUid =", ds.uid);
        return this.recommendationService.getInstantiatedRecommendations(org.id, ds.uid);
>>>>>>> 9a44937e (adding proportions as the third criterion for TOPSIS)
      }),
      tap((data: InstantiatedRecommendation[]) => console.log("LOG: Données reçues du backend:", data))
    )
    .subscribe({
      next: (data: InstantiatedRecommendation[]) => {
        // Data is already sorted by priority desc by the backend (TOPSIS)
        this.recommendations = data.map((r: InstantiatedRecommendation) => ({
          ...r.recommendation,
          priority: +(r.priority * 100).toFixed(1),
          category: this.mapCategory(r.recommendation?.category ?? []),
          selected: false,
          implementationDifficulty: r.recommendation?.difficulty
            ? this.mapDifficulty(r.recommendation.difficulty)
            : "N/A"
        }));

        console.log("LOG: Recommendations transformées:", this.recommendations);
      },
      error: (err: any) => {
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

compareSelected() {
  const selected = this.recommendations.filter(r => r.selected);
  this.selectedRecommendationsForApply = selected;
  this.showApplyComponent = selected.length > 0;
}


}