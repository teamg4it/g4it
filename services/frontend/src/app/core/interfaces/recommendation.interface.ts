export interface RecommendationDS {
    idRecommendation: number;
    title: string;
    description?: string;
    category: string[];
    affectedAttributes?: string;
    heuristicRange?: string;
    baseImpact?: number;
    organisationId?: number;
    difficulty?: string;

}
