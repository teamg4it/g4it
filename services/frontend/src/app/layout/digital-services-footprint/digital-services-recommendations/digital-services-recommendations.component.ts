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
    // 'select-all',
    
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
      tap(([org, ds]) => console.log("LOG: Organisation valide, id =", org.id, "| dsVersionUid =", ds.uid)),
      switchMap(([org, ds]) => {
        console.log("LOG: Appel API instantiated recommendations avec orgId =", org.id, "dsVersionUid =", ds.uid);
        return this.recommendationService.getInstantiatedRecommendations(org.id, ds.uid);
      }),
      tap((data: InstantiatedRecommendation[]) => console.log("LOG: Données reçues du backend:", data))
    )
    .subscribe({
      next: (data: InstantiatedRecommendation[]) => {
        this.recommendations = data.map((r: InstantiatedRecommendation, index: number) => {
          const rec = r.recommendation;
          const translationKey = this.getRecommendationKey(rec!.title);

          return {
            ...rec,
              title: `recommendations.${translationKey}.title`,
            description: `recommendations.${translationKey}.description`,
            priority: index + 1,
          category: this.mapCategory(r.recommendation?.category ?? []),
            selected: false,
              implementationDifficulty: rec?.difficulty
              ? `difficulty.${rec.difficulty}`
              : null,    
            translationKey: translationKey
          };
        });
      }
    });
}

// ==================== MAPPING FR -> CLÉ ====================
private getRecommendationKey(frenchTitle: string): string {
  const map: { [key: string]: string } = {
    "Utiliser une architecture adaptée": "ADAPT_ARCHITECTURE",
    "Limiter le poids et le nombre de requêtes par écran": "LIMIT_REQUESTS",
    "Minimiser le PUE de l'hébergement": "MINIMIZE_PUE",
    "Choisir un hébergement cloud géographiquement cohérent": "GEO_CLOUD",
    "Optimiser le parcours utilisateur": "OPTIMIZE_UX"
  };

  return map[frenchTitle] || 'UNKNOWN';
}

private mapCategory(category: string[]): string[] {
  const mapping: any = {
    PUBLIC_CLOUD: `categories.PUBLIC_CLOUD`,
    PRIVATE_INFRASTRUCTURE: "categories.PRIVATE_INFRASTRUCTURE",
    NETWORK: "categories.NETWORK",
    TERMINAL: "categories.TERMINAL"
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